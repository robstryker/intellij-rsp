/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.rsp.ui.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jboss.tools.intellij.rsp.model.IRsp;
import org.jboss.tools.intellij.rsp.util.AlphanumComparator;
import org.jboss.tools.rsp.api.dao.DownloadRuntimeDescription;
import org.jboss.tools.rsp.api.dao.ListDownloadRuntimeResponse;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class SelectDownloadRuntimeDialog extends DialogWrapper implements ListSelectionListener {
    private final ListDownloadRuntimeResponse runtimeResponse;
    private final IRsp rsp;
    private HashMap<String, DownloadRuntimeDescription> dataMap;
    private JPanel contentPane;
    private JBList list;
    private DownloadRuntimeDescription selected = null;
    public SelectDownloadRuntimeDialog(IRsp rsp, ListDownloadRuntimeResponse runtimeResponse) {
        super((Project)null, true, IdeModalityType.IDE);
        this.rsp = rsp;
        this.runtimeResponse = runtimeResponse;
        dataMap = new HashMap<>();
        for( DownloadRuntimeDescription descriptor : runtimeResponse.getRuntimes() ) {
            dataMap.put(descriptor.getId(), descriptor);
        }
        setTitle("Download Server Runtime...");
        init();
        getButton(getOKAction()).setEnabled(list.getSelectedValue() != null);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        createLayout();
        return contentPane;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        Object o = list.getSelectedValue();
        if( o instanceof DownloadRuntimeDescription) {
            selected = (DownloadRuntimeDescription)o;
        }
        getButton(getOKAction()).setEnabled(o != null);
    }

    public DownloadRuntimeDescription getSelected() {
        return selected;
    }

    private class DownloadRuntimeCellRenderer extends AbstractRspCellRenderer {
        public DownloadRuntimeCellRenderer() {
            super();
        }

        @Override
        protected String getTextForValue(Object value) {
            if( value instanceof DownloadRuntimeDescription) {
                return ((DownloadRuntimeDescription)value).getName();
            }
            return null;
        }

        @Override
        protected Icon getIconForValue(Object value) {
            if( value instanceof DownloadRuntimeDescription) {
                String serverType = ((DownloadRuntimeDescription) value).getProperties().get("wtp-runtime-type");
                return serverType == null ? null : rsp.getRspType().getIcon(serverType);
            }
            return null;
        }
    }

    private void createLayout() {
        java.util.List<DownloadRuntimeDescription> dlrts = new ArrayList<>(dataMap.values());
        Collections.sort(dlrts, (o1,o2) -> {
            return AlphanumComparator.staticCompare(o1.getName(), o2.getName());
        });
        list = new JBList(dlrts);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DownloadRuntimeCellRenderer renderer = new DownloadRuntimeCellRenderer();
        list.setCellRenderer(renderer);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(15);
        list.addListSelectionListener(this);
        JScrollPane listScroller = new JBScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 250));
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JLabel instructions = new JLabel("Please select a runtime to download.");

        JPanel filterBox = new JPanel();
        filterBox.setLayout(new BoxLayout(filterBox, BoxLayout.X_AXIS));
        JLabel filterLabel = new JLabel("Filter: ");
        JTextField filterField = new JTextField();
        filterBox.add(filterLabel);
        filterBox.add(filterField);
        filterField.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
            public void changed() {
                String val = filterField.getText();
                if( val != null && !val.isEmpty()) {
                    java.util.List<DownloadRuntimeDescription> dlrtsMatching = new ArrayList<>(dlrts);
                    Iterator<DownloadRuntimeDescription> it = dlrtsMatching.iterator();
                    while(it.hasNext()) {
                        if( !it.next().getName().toLowerCase().contains(val.toLowerCase())) {
                            it.remove();
                        }
                    }
                    list.setListData(dlrtsMatching.toArray());
                } else if( val != null && val.isEmpty()) {
                    list.setListData(dlrts.toArray());
                }
            }
        });

        contentPane.add(instructions);
        contentPane.add(filterBox);
        contentPane.add(listScroller);
    }

}
