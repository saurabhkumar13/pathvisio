// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//

package org.pathvisio.gui.swing;

import javax.swing.JOptionPane;

import org.jdesktop.swingworker.SwingWorker;
import org.pathvisio.ApplicationEvent;
import org.pathvisio.Engine.ApplicationEventListener;
import org.pathvisio.data.DataException;
import org.pathvisio.data.GdbEvent;
import org.pathvisio.data.GdbManager.GdbEventListener;
import org.pathvisio.data.GexManager;
import org.pathvisio.data.SimpleGex;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.progress.ProgressDialog;
import org.pathvisio.model.Pathway;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.visualization.VisualizationManager;
import org.pathvisio.visualization.VisualizationMethodRegistry;

/**
 * StandaloneEngine is a singleton that ties together several
 * other important singletons and provides access to them for
 * the entire swing standalone application (not SWT).
 * StandaloneEngine provides functionality for the Standalone application
 * such as data visualization and access to gene expression data. It
 * is also a contact point for Plugins, and makes sure
 * Gex data is cached when a suitable pgdb, pgex and gpml are loaded.
 * 
 * StandaloneEngine is a singleton: There should be always exactly 
 * one instance of it.
 * 
 * //TODO: this class will probably be renamed in the future  
 */
public class StandaloneEngine implements ApplicationEventListener, GdbEventListener
{
	private final VisualizationManager visualizationManager;
	private final GexManager gexManager;
	private final SwingEngine swingEngine;

	/**
	 * During construction, visualizationManager and gexManager will be initialized.
	 * SwingEngine needs to have been initialized already.
	 */
	public StandaloneEngine(SwingEngine swingEngine)
	{
		if (swingEngine == null) throw new NullPointerException();
		this.swingEngine = swingEngine;
		swingEngine.getEngine().addApplicationEventListener(this);
		swingEngine.getGdbManager().addGdbEventListener(this);
		gexManager = GexManager.getCurrent();
		visualizationManager = new VisualizationManager(
				VisualizationMethodRegistry.getCurrent(),
				swingEngine.getEngine(), gexManager);
	}

	/**
	 * Return the global visualizationManager instance.
	 */
	public VisualizationManager getVisualizationManager() 
	{
		return visualizationManager;
	}
	
	public VisualizationMethodRegistry getVisualizationMethodRegistry()
	{
		return VisualizationMethodRegistry.getCurrent();
	}	

	/**
	 * returns the global gexManager instance.
	 */
	public GexManager getGexManager()
	{
		return gexManager;
	}

	/**
	 * returns the global swingEngine instance.
	 */
	public SwingEngine getSwingEngine()
	{
		return swingEngine;
	}
	
	/**
	 * Load the Gex cache for the current pathway. Only starts loading
	 * when an expression dataset is available and a pathway is open.
	 */
	public void loadGexCache() {
		final SimpleGex gex = gexManager.getCurrentGex();
		final Pathway p = swingEngine.getEngine().getActivePathway();
		if(p != null && gex != null && swingEngine.getGdbManager().isConnected()) {
			final ProgressKeeper pk = new ProgressKeeper(
					(int)1E5
			);
			final ProgressDialog d = new ProgressDialog(
					JOptionPane.getFrameForComponent(swingEngine.getApplicationPanel()), 
					"", pk, false, true
			);
					
			SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
				protected Void doInBackground() {
					pk.setTaskName("Loading expression data");
					try
					{	
						gex.cacheData(p.getDataNodeXrefs(), pk, swingEngine.getGdbManager().getCurrentGdb());
					}
					catch (DataException e)
					{
						Logger.log.error ("Exception while caching expression data ", e);
					}
					pk.finished();
					return null;
				}
				
				@Override
				protected void done()
				{
					swingEngine.getEngine().getActiveVPathway().redraw();
				}
			};
			
			sw.execute();
			d.setVisible(true);
		}
	}
	

	/**
	 * Update Gex cache in response to opening pathways.
	 */
	public void applicationEvent(ApplicationEvent e) 
	{
		if(e.getType() == ApplicationEvent.PATHWAY_OPENED) 
		{
			loadGexCache();
		}
	}

	/**
	 * Update gex cache in response to opening / closing gene databases
	 */
	public void gdbEvent(GdbEvent e) 
	{
		loadGexCache();			
	}

}
