/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

public final class DialogListSection {
    private final JPanel component;
    private final DefaultListModel<DialogDefinition> model = new DefaultListModel<>();
    private final JList<DialogDefinition> list = new JList<>(model);
    private String mainWindowName; // The name of the main window

    public DialogListSection(String caption) {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.setBorder(new LineBorder(new Color(0xDDE4ED)));
        list.setBackground(Color.WHITE);
        
        // Set custom cell renderer to show main window indicator
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                         int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DialogDefinition) {
                    DialogDefinition dialog = (DialogDefinition) value;
                    String displayName = dialog.getName();
                    // Add star indicator if this is the main window
                    if (dialog.getName().equals(mainWindowName)) {
                        displayName = "★ " + displayName;
                        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
                    }
                    label.setText(displayName);
                }
                return label;
            }
        });
        
        // Add context menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem setMainWindowItem = new JMenuItem("Set as Main Window");
        setMainWindowItem.addActionListener(e -> setSelectedAsMainWindow());
        contextMenu.add(setMainWindowItem);
        
        list.setComponentPopupMenu(contextMenu);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel listContainer = new JPanel(new BorderLayout());
        listContainer.setOpaque(false);
        listContainer.add(scrollPane, BorderLayout.CENTER);
        listContainer.add(createButtonBar(), BorderLayout.SOUTH);

        this.component = UiUtils.createFieldSection(
            caption,
            caption,
            "Maintain the dialogs or screens required for this project. Use Add to create new windows, Edit to refine details, and Remove to drop unused entries. Right-click to set the main window (shown on module load).",
            listContainer);
    }

    private JComponent createButtonBar() {
        JButton addButton = new JButton("Add");
        addButton.addActionListener(event -> openEditor(null));

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(event -> {
            DialogDefinition selected = list.getSelectedValue();
            if (selected != null) {
                openEditor(selected);
            }
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(event -> {
            int index = list.getSelectedIndex();
            if (index >= 0) {
                DialogDefinition toDelete = model.get(index);
                // Clear main window name if we're deleting the main window
                if (toDelete.getName().equals(mainWindowName)) {
                    mainWindowName = null;
                    // Auto-set new main window if there are other dialogs
                    if (model.size() > 1) {
                        // Set the first remaining dialog as main (after deletion)
                        int newMainIndex = (index == 0) ? 1 : 0;
                        mainWindowName = model.get(newMainIndex).getName();
                    }
                }
                model.remove(index);
                list.repaint(); // Refresh to show main window indicator
            }
        });

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttonBar.setOpaque(false);
        buttonBar.add(addButton);
        buttonBar.add(editButton);
        buttonBar.add(deleteButton);
        return buttonBar;
    }

    private void openEditor(DialogDefinition existing) {
        System.out.println("openEditor called - existing: " + (existing != null ? existing.getName() : "null"));
        DialogDefinitionEditorDialog dialog = new DialogDefinitionEditorDialog(list, existing);
        dialog.setVisible(true);
        System.out.println("Dialog closed, checking result...");
        dialog.getResult().ifPresent(definition -> {
            System.out.println("Result present: " + definition.getName());
            if (existing != null) {
                int index = list.getSelectedIndex();
                if (index >= 0) {
                    // Update main window name if the edited dialog was the main window
                    if (existing.getName().equals(mainWindowName)) {
                        mainWindowName = definition.getName();
                    }
                    model.set(index, definition);
                    System.out.println("Updated existing dialog at index " + index);
                }
            } else {
                model.addElement(definition);
                System.out.println("Added new dialog to model, model size: " + model.size());
                // Auto-set as main window if this is the first dialog
                if (model.size() == 1) {
                    mainWindowName = definition.getName();
                    System.out.println("Set as main window: " + mainWindowName);
                }
            }
            list.repaint(); // Refresh to show main window indicator
        });
        if (!dialog.getResult().isPresent()) {
            System.out.println("No result present - user may have cancelled");
        }
    }

    public JPanel getComponent() {
        return component;
    }

    public void setDialogs(List<DialogDefinition> dialogs) {
        model.clear();
        if (dialogs != null) {
            dialogs.forEach(model::addElement);
            // Auto-set first window as main if no main window is set and list is not empty
            if (mainWindowName == null && !dialogs.isEmpty()) {
                mainWindowName = dialogs.get(0).getName();
            }
        }
        list.repaint(); // Refresh to show main window indicator
    }

    public List<DialogDefinition> getDialogs() {
        List<DialogDefinition> items = new ArrayList<>(model.size());
        for (int i = 0; i < model.size(); i++) {
            items.add(model.get(i));
        }
        return items;
    }

    public void setVisible(boolean visible) {
        component.setVisible(visible);
        component.revalidate();
        component.repaint();
    }
    
    public String getMainWindowName() {
        return mainWindowName;
    }
    
    public void setMainWindowName(String mainWindowName) {
        this.mainWindowName = mainWindowName;
        list.repaint(); // Refresh to show main window indicator
    }
    
    private void setSelectedAsMainWindow() {
        DialogDefinition selected = list.getSelectedValue();
        if (selected != null) {
            mainWindowName = selected.getName();
            list.repaint(); // Refresh to show the new main window indicator
        }
    }
}
