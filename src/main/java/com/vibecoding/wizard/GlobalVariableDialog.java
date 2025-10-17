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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Dialog for adding or editing a global variable.
 */
public final class GlobalVariableDialog extends JDialog {
    private final JTextField nameField = new JTextField(30);
    private final JTextArea valueArea = new JTextArea(10, 30);
    private final List<GlobalVariable> existingVariables;
    private final List<ModuleVariable> moduleVariables;
    private final GlobalVariable originalVariable;
    private GlobalVariable result;

    /**
     * Creates a dialog for editing global variables with validation.
     * 
     * @param owner The parent frame
     * @param variable The variable to edit (null for new variable)
     * @param existingVariables List of existing global variables for duplicate checking
     * @param moduleVariables List of module variables for conflict checking
     */
    public GlobalVariableDialog(JFrame owner, GlobalVariable variable, List<GlobalVariable> existingVariables, List<ModuleVariable> moduleVariables) {
        super(owner, "Global Variable", true);
        this.originalVariable = variable;
        this.existingVariables = existingVariables;
        this.moduleVariables = moduleVariables != null ? moduleVariables : new ArrayList<>();
        setLayout(new BorderLayout(8, 8));
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Name field with help button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        namePanel.setOpaque(false);
        namePanel.add(new JLabel("Name:"));
        namePanel.add(UiUtils.createHelpButton(
            "Variable Name",
            "Specify a unique name for this global variable. Use a descriptive name that indicates the variable's purpose (e.g., 'API_URL', 'MAX_RETRIES', 'DEFAULT_TIMEOUT').",
            this));
        contentPanel.add(namePanel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        contentPanel.add(nameField, gbc);
        
        // Value field with help button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        valuePanel.setOpaque(false);
        valuePanel.add(new JLabel("Value:"));
        valuePanel.add(UiUtils.createHelpButton(
            "Variable Value",
            "Enter the value for this global variable. This can be any text value that will be accessible across all modules in the project. The value can be referenced in prompts and used throughout the codebase.",
            this));
        contentPanel.add(valuePanel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        valueArea.setLineWrap(true);
        valueArea.setWrapStyleWord(true);
        JScrollPane valueScroll = new JScrollPane(valueArea);
        contentPanel.add(valueScroll, gbc);
        
        // Load existing variable if provided
        if (variable != null) {
            nameField.setText(variable.getName());
            valueArea.setText(variable.getValue());
        }
        
        add(contentPanel, BorderLayout.CENTER);
        
        // OK/Cancel buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(event -> {
            String name = nameField.getText();
            if (name == null || name.isBlank()) {
                UiUtils.showWarning(this, "Please enter a name for the global variable.", "Missing Name");
                return;
            }
            
            String trimmedName = name.trim();
            
            // Check for duplicate names (excluding the original variable being edited)
            if (existingVariables != null) {
                for (GlobalVariable existingVar : existingVariables) {
                    if (originalVariable == null || !existingVar.getName().equals(originalVariable.getName())) {
                        if (existingVar.getName().equalsIgnoreCase(trimmedName)) {
                            UiUtils.showWarning(this, 
                                "A global variable with the name '" + trimmedName + "' already exists.\n" +
                                "Variable names must be unique.", 
                                "Duplicate Variable Name");
                            return;
                        }
                    }
                }
            }
            
            // Check for conflicts with module variables
            if (moduleVariables != null) {
                for (ModuleVariable moduleVar : moduleVariables) {
                    if (moduleVar.getName().equalsIgnoreCase(trimmedName)) {
                        UiUtils.showWarning(this, 
                            "The name '" + trimmedName + "' is already used as a module variable.\n" +
                            "Global variables cannot use the same names as module variables.", 
                            "Name Conflict");
                        return;
                    }
                }
            }
            
            result = new GlobalVariable(trimmedName, valueArea.getText().trim());
            dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setSize(500, 400);
        setLocationRelativeTo(owner);
    }

    public GlobalVariable getResult() {
        return result;
    }
}
