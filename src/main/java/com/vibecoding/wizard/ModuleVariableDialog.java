/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
 * Dialog for adding or editing a module variable.
 */
public final class ModuleVariableDialog extends JDialog {
    private final JTextField nameField = new JTextField(30);
    private final JTextArea valueArea = new JTextArea(10, 30);
    private final List<ModuleVariable> existingVariables;
    private final List<GlobalVariable> globalVariables;
    private final ModuleVariable originalVariable;
    private ModuleVariable result;

    /**
     * Creates a dialog for editing module variables with validation.
     * 
     * @param owner The parent frame
     * @param variable The variable to edit (null for new variable)
     * @param existingVariables List of existing module variables for duplicate checking
     * @param globalVariables List of global variables for conflict checking
     */
    public ModuleVariableDialog(JFrame owner, ModuleVariable variable, 
                                 List<ModuleVariable> existingVariables,
                                 List<GlobalVariable> globalVariables) {
        super(owner, "Module Variable", true);
        this.originalVariable = variable;
        this.existingVariables = existingVariables;
        this.globalVariables = globalVariables;
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
            "Specify a unique name for this module variable. Use a descriptive name that indicates the variable's purpose (e.g., 'MODULE_VERSION', 'DEFAULT_USER', 'CONFIG_PATH'). Module variables are scoped to this specific module.",
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
            "Enter the value for this module variable. This value is specific to this module and can be referenced in prompts. Module variables can override or supplement global variables for module-specific configurations.",
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
                UiUtils.showWarning(this, "Please enter a name for the module variable.", "Missing Name");
                return;
            }
            
            String trimmedName = name.trim();
            
            // Check for duplicate names within module (excluding the original variable being edited)
            if (existingVariables != null) {
                for (ModuleVariable existingVar : existingVariables) {
                    if (originalVariable == null || !existingVar.getName().equals(originalVariable.getName())) {
                        if (existingVar.getName().equalsIgnoreCase(trimmedName)) {
                            UiUtils.showWarning(this, 
                                "A module variable with the name '" + trimmedName + "' already exists in this module.\n" +
                                "Variable names must be unique within the module.", 
                                "Duplicate Variable Name");
                            return;
                        }
                    }
                }
            }
            
            // Check for conflicts with global variables
            if (globalVariables != null) {
                for (GlobalVariable globalVar : globalVariables) {
                    if (globalVar.getName().equalsIgnoreCase(trimmedName)) {
                        UiUtils.showWarning(this, 
                            "The name '" + trimmedName + "' is already used as a project-global variable.\n" +
                            "Module variables cannot use the same names as global variables.", 
                            "Name Conflict");
                        return;
                    }
                }
            }
            
            result = new ModuleVariable(trimmedName, valueArea.getText().trim());
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

    public ModuleVariable getResult() {
        return result;
    }
}
