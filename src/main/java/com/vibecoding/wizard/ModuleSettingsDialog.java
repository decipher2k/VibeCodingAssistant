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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Dialog for managing module-specific settings including module variables.
 */
public final class ModuleSettingsDialog extends JDialog {
    private final Module module;
    private final IDEController controller;
    
    // Module variables components
    private final DefaultListModel<ModuleVariable> variableModel = new DefaultListModel<>();
    private final JList<ModuleVariable> variableList = new JList<>(variableModel);
    
    private boolean saved = false;

    public ModuleSettingsDialog(JFrame owner, IDEController controller, Module module) {
        super(owner, "Module Settings - " + module.getName(), true);
        this.module = module;
        this.controller = controller;
        
        setLayout(new BorderLayout(8, 8));
        
        JPanel mainPanel = createVariablesPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        
        add(mainPanel, BorderLayout.CENTER);
        
        // OK/Cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(event -> {
            saveSettings();
            saved = true;
            dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load current settings
        loadSettings();
        
        setSize(600, 500);
        setLocationRelativeTo(owner);
    }

    /**
     * Creates the module variables panel.
     */
    private JPanel createVariablesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Label with help button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        labelPanel.setOpaque(false);
        labelPanel.add(new JLabel("Module Variables:"));
        labelPanel.add(UiUtils.createHelpButton(
            "Module Variables",
            "Define module-specific variables that can be referenced in prompts for this module. " +
            "Module variables are scoped to this module only and can override or supplement global variables. " +
            "They are name-value pairs that can be used to store module-specific configuration values or constants.",
            this));
        panel.add(labelPanel, gbc);
        
        // Variable list
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane listScroll = new JScrollPane(variableList);
        panel.add(listScroll, gbc);
        
        // CRUD buttons
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Variable");
        addButton.addActionListener(event -> addVariable());
        
        JButton editButton = new JButton("Edit Variable");
        editButton.addActionListener(event -> editVariable());
        
        JButton deleteButton = new JButton("Delete Variable");
        deleteButton.addActionListener(event -> deleteVariable());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, gbc);
        
        return panel;
    }

    /**
     * Loads the current settings into the UI.
     */
    private void loadSettings() {
        // Load module variables
        for (ModuleVariable variable : module.getModuleVariables()) {
            variableModel.addElement(variable);
        }
    }

    /**
     * Saves the settings from the UI back to the Module object.
     */
    private void saveSettings() {
        // Save module variables
        List<ModuleVariable> variables = new ArrayList<>();
        for (int i = 0; i < variableModel.size(); i++) {
            variables.add(variableModel.get(i));
        }
        module.setModuleVariables(variables);
        
        // Mark project as dirty
        if (controller != null) {
            controller.markDirty();
        }
    }

    /**
     * Adds a new module variable.
     */
    private void addVariable() {
        // Get existing module variables for validation
        List<ModuleVariable> existingVars = new ArrayList<>();
        for (int i = 0; i < variableModel.size(); i++) {
            existingVars.add(variableModel.get(i));
        }
        
        // Get global variables from project settings
        List<GlobalVariable> globalVars = controller.getCurrentProject().getProjectSettings().getGlobalVariables();
        
        ModuleVariableDialog dialog = new ModuleVariableDialog((JFrame) getOwner(), null, existingVars, globalVars);
        dialog.setVisible(true);
        ModuleVariable variable = dialog.getResult();
        if (variable != null) {
            variableModel.addElement(variable);
        }
    }

    /**
     * Edits the selected module variable.
     */
    private void editVariable() {
        int index = variableList.getSelectedIndex();
        if (index < 0) {
            UiUtils.showWarning(this, "Please select a variable to edit.", "No Selection");
            return;
        }
        
        ModuleVariable variable = variableModel.get(index);
        
        // Get existing module variables for validation
        List<ModuleVariable> existingVars = new ArrayList<>();
        for (int i = 0; i < variableModel.size(); i++) {
            existingVars.add(variableModel.get(i));
        }
        
        // Get global variables from project settings
        List<GlobalVariable> globalVars = controller.getCurrentProject().getProjectSettings().getGlobalVariables();
        
        ModuleVariableDialog dialog = new ModuleVariableDialog((JFrame) getOwner(), variable, existingVars, globalVars);
        dialog.setVisible(true);
        ModuleVariable updatedVariable = dialog.getResult();
        if (updatedVariable != null) {
            variableModel.set(index, updatedVariable);
        }
    }

    /**
     * Deletes the selected module variable.
     */
    private void deleteVariable() {
        int index = variableList.getSelectedIndex();
        if (index < 0) {
            UiUtils.showWarning(this, "Please select a variable to delete.", "No Selection");
            return;
        }
        
        variableModel.remove(index);
    }

    /**
     * Returns whether the user saved the settings.
     * 
     * @return true if saved, false if cancelled
     */
    public boolean isSaved() {
        return saved;
    }
}
