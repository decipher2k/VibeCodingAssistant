/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public final class WorkflowItemDialog extends JDialog {
    private final JTextField nameField = new JTextField(30);
    private final JComboBox<String> windowCombo;
    private final JTextField triggerField = new JTextField(30);
    private final DefaultListModel<WorkflowStep> stepsModel = new DefaultListModel<>();
    private final JList<WorkflowStep> stepsList = new JList<>(stepsModel);
    private WorkflowItem result;

    public WorkflowItemDialog(JFrame owner, WorkflowItem item, List<DialogDefinition> dialogs) {
        super(owner, "Workflow Item", true);
        setLayout(new BorderLayout(8, 8));
        
        // Build window list from dialogs
        List<String> windowNames = new ArrayList<>();
        windowNames.add(""); // Empty option
        if (dialogs != null) {
            for (DialogDefinition dialog : dialogs) {
                windowNames.add(dialog.getWindowTitle());
            }
        }
        windowCombo = new JComboBox<>(windowNames.toArray(new String[0]));
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Name with help button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        namePanel.setOpaque(false);
        namePanel.add(new JLabel("Name:"));
        namePanel.add(UiUtils.createHelpButton(
            "Workflow Item Name",
            "Specify a descriptive name for this workflow item. This identifies the sequence of steps the user will perform.",
            this));
        contentPanel.add(namePanel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        contentPanel.add(nameField, gbc);
        
        // Window with help button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JPanel windowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        windowPanel.setOpaque(false);
        windowPanel.add(new JLabel("Window (optional):"));
        windowPanel.add(UiUtils.createHelpButton(
            "Window Affected",
            "Select the dialog window where this workflow takes place. Leave empty if it applies to the main window or is window-independent.",
            this));
        contentPanel.add(windowPanel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        contentPanel.add(windowCombo, gbc);
        
        // Trigger with help button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JPanel triggerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        triggerPanel.setOpaque(false);
        triggerPanel.add(new JLabel("User action to start:"));
        triggerPanel.add(UiUtils.createHelpButton(
            "Trigger Action",
            "Describe what user action initiates this workflow (e.g., 'clicks Save button', 'selects File > Open menu'). This helps document when the workflow begins.",
            this));
        contentPanel.add(triggerPanel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        contentPanel.add(triggerField, gbc);
        
        // Steps label with help button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        JPanel stepsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        stepsPanel.setOpaque(false);
        stepsPanel.add(new JLabel("Steps:"));
        stepsPanel.add(UiUtils.createHelpButton(
            "Workflow Steps",
            "Define the sequence of steps that make up this workflow. Each step describes what the system should do and what the expected outcome is. Use the buttons below to add, edit, or delete steps.",
            this));
        contentPanel.add(stepsPanel, gbc);
        
        // Steps list
        gbc.gridy = 4;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane stepsScroll = new JScrollPane(stepsList);
        contentPanel.add(stepsScroll, gbc);
        
        // Steps CRUD buttons
        gbc.gridy = 5;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel stepButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addStepButton = new JButton("Add Step");
        addStepButton.addActionListener(event -> addStep());
        
        JButton editStepButton = new JButton("Edit Step");
        editStepButton.addActionListener(event -> editStep());
        
        JButton deleteStepButton = new JButton("Delete Step");
        deleteStepButton.addActionListener(event -> deleteStep());
        
        stepButtonPanel.add(addStepButton);
        stepButtonPanel.add(editStepButton);
        stepButtonPanel.add(deleteStepButton);
        contentPanel.add(stepButtonPanel, gbc);
        
        // Load existing item
        if (item != null) {
            nameField.setText(item.getName());
            windowCombo.setSelectedItem(item.getWindowAffected());
            triggerField.setText(item.getTrigger());
            for (WorkflowStep step : item.getSteps()) {
                stepsModel.addElement(step);
            }
        }
        
        add(contentPanel, BorderLayout.CENTER);
        
        // OK/Cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(event -> {
            String name = nameField.getText();
            if (name == null || name.isBlank()) {
                UiUtils.showWarning(this, "Please enter a name for the workflow item.", "Missing Name");
                return;
            }
            
            List<WorkflowStep> steps = new ArrayList<>();
            for (int i = 0; i < stepsModel.size(); i++) {
                steps.add(stepsModel.get(i));
            }
            
            result = new WorkflowItem(
                name.trim(),
                (String) windowCombo.getSelectedItem(),
                triggerField.getText().trim(),
                steps
            );
            dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setSize(500, 500);
        setLocationRelativeTo(owner);
    }
    
    private void addStep() {
        WorkflowStepDialog dialog = new WorkflowStepDialog(this, null);
        dialog.setVisible(true);
        WorkflowStep step = dialog.getResult();
        if (step != null) {
            stepsModel.addElement(step);
        }
    }
    
    private void editStep() {
        int index = stepsList.getSelectedIndex();
        if (index < 0) {
            UiUtils.showWarning(this, "Please select a step to edit.", "No Selection");
            return;
        }
        WorkflowStep existing = stepsModel.get(index);
        WorkflowStepDialog dialog = new WorkflowStepDialog(this, existing);
        dialog.setVisible(true);
        WorkflowStep updated = dialog.getResult();
        if (updated != null) {
            stepsModel.set(index, updated);
        }
    }
    
    private void deleteStep() {
        int index = stepsList.getSelectedIndex();
        if (index < 0) {
            UiUtils.showWarning(this, "Please select a step to delete.", "No Selection");
            return;
        }
        stepsModel.remove(index);
    }
    
    public WorkflowItem getResult() {
        return result;
    }
}
