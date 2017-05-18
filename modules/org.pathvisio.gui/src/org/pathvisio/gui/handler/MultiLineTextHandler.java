// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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

package org.pathvisio.gui.handler;

import org.pathvisio.core.model.PropertyType;
import org.pathvisio.core.model.StaticPropertyType;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * This class knows how to handle text with newlines.
 *..
 * @author Saurabh Kumar
 */
public class MultiLineTextHandler extends DefaultCellEditor implements TableCellRenderer, TypeHandler{

    private TableCellRenderer cellRenderer = new DefaultTableCellRenderer();
    JTextArea textArea;
    JScrollPane scrollPane;

    /**
     * Constructor.
     * ..
     */
    public MultiLineTextHandler() {
        super( new JTextField() );

        textArea = new JTextArea();
        scrollPane = new JScrollPane();
        scrollPane.setViewportView( textArea );
        editorComponent = scrollPane;

    }

    //-- TableCellRenderer methods --//

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column ) {
        this.setValue( value );
        scrollPane.setBorder( new LineBorder( Color.black ) );
        return scrollPane;
    }


    public void setValue( Object value ) {
        textArea.setText( ( value != null ) ? value.toString() : "" );
    }


    public Object getCellEditorValue() {
        return textArea.getText();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        return cellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    //-- TypeHandler methods --//

    public PropertyType getType() {
        return StaticPropertyType.STRING;
    }

    public TableCellRenderer getLabelRenderer() {
        return null;
    }

    public TableCellRenderer getValueRenderer() {
        return this;
    }

    public TableCellEditor getValueEditor() {
        return this;
    }


}
