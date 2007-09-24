package org.pathvisio.gui.wikipathways;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.pathvisio.Engine;
import org.pathvisio.debug.Logger;
import org.pathvisio.gui.swing.GuiInit;
import org.pathvisio.gui.swing.SwingEngine;
import org.pathvisio.util.ProgressKeeper;
import org.pathvisio.util.RunnableWithProgress;
import org.pathvisio.util.ProgressKeeper.ProgressEvent;
import org.pathvisio.util.ProgressKeeper.ProgressListener;
import org.pathvisio.wikipathways.Parameter;
import org.pathvisio.wikipathways.UserInterfaceHandler;
import org.pathvisio.wikipathways.WikiPathways;

public class PathwayPageApplet extends JApplet {
	UserInterfaceHandler uiHandler;
	WikiPathways wiki;
	boolean isFirstApplet = true;
	
	public final void init() {
		//Check if other applets are present that already have an instance
		//of WikiPathways
		WikiPathways owiki = findExistingWikiPathways();
		if(owiki != null) {
			wiki = owiki;
			uiHandler = owiki.getUserInterfaceHandler();
			isFirstApplet = false;
		} else {
			uiHandler = new AppletUserInterfaceHandler(PathwayPageApplet.this);
			wiki = new WikiPathways(uiHandler);
		}
		
		if(isFirstApplet) {
			Engine engine = new Engine();
			Engine.setCurrent(engine);
			SwingEngine.setCurrent(new SwingEngine(engine));
			GuiInit.init();
		}
		
		System.out.println("INIT CALLED....");
		Logger.log.trace("INIT CALLED....");

		parseArguments();

		//Init with progress monitor
		final RunnableWithProgress<Void> r = new RunnableWithProgress<Void>() {
			public Void excecuteCode() {
				try {
					doInitWiki(getProgressKeeper(), getDocumentBase());
				} catch(Exception e) {
					Logger.log.error("Error while starting applet", e);
					JOptionPane.showMessageDialog(
							PathwayPageApplet.this, e.getClass() + ": See error logg for details", "Error while initializing editor", JOptionPane.ERROR_MESSAGE);
				};
				doInit();
				getProgressKeeper().finished();
				return null;
			}
		};
		r.getProgressKeeper().addListener(new ProgressListener() {
			public void progressEvent(ProgressEvent e) {
				if(e.getType() == ProgressEvent.FINISHED) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							Logger.log.trace("Creating GUI");
							createToolbar();
							createGui();
							validate();
						}
					});
				}
			}
		});
		
		uiHandler.runWithProgress(r, "", ProgressKeeper.PROGRESS_UNKNOWN, false, true);
	}
	
	protected final WikiPathways findExistingWikiPathways() {
		Enumeration<Applet> applets = getAppletContext().getApplets();
		while(applets.hasMoreElements()) {
			Applet a = applets.nextElement();
			if(a instanceof PathwayPageApplet) {
				return ((PathwayPageApplet)a).wiki;
			}
		}
		return null; //Nothing found
	}
	
	/**
	 * In this method the WikiPathways class is initiated, 
	 * by calling {@link WikiPathways#init(ProgressKeeper, URL)}
	 * @see {@link WikiPathways#init(ProgressKeeper, URL)}
	 * @param pk
	 * @param base
	 * @throws Exception
	 */
	protected void doInitWiki(ProgressKeeper pk, URL base) throws Exception {
		Logger.log.trace("PathwayPageApplet:doInitWiki");
		if(isFirstApplet) {
			wiki.init(pk, base);
		} else {
			Logger.log.trace("Adding initVPathway");
			wiki.initVPathway();
		}
	}
	
	protected void doInit() {
		//May be implemented by subclasses
	}
	
	protected void createGui() {
		//May be implemented by subclasses
	}
		
	protected void createToolbar() {
		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
		toolbar.setFloatable(false);
		toolbar.add(new Actions.ExitAction(wiki.getUserInterfaceHandler(), wiki, true));
		toolbar.add(new Actions.ExitAction(wiki.getUserInterfaceHandler(), wiki, false));
		getContentPane().add(toolbar, BorderLayout.WEST);
	}
	
	void parseArguments() {
		for(Parameter p : Parameter.values()) {
			p.setValue(getParameter(p.getName()));
		}
	}
}