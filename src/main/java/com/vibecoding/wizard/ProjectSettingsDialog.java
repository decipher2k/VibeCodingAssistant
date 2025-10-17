/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Dialog for managing project-wide settings including workflows,
 * global variables, and database description.
 */
public final class ProjectSettingsDialog extends JDialog {
    private final IDEController ideController;
    private final WizardController wizardController;
    private final ProjectSettings settings;
    
    // Workflows tab components
    private final DefaultListModel<WorkflowItem> workflowModel = new DefaultListModel<>();
    private final JList<WorkflowItem> workflowList = new JList<>(workflowModel);
    
    // Global variables tab components
    private final DefaultListModel<GlobalVariable> variableModel = new DefaultListModel<>();
    private final JList<GlobalVariable> variableList = new JList<>(variableModel);
    
    // Database tab components
    private final JTextArea databaseArea = new JTextArea(15, 50);
    private final DatabaseFileSection databaseFileSection;
    
    // Project info components
    private final JTextField projectNameField = new JTextField(30);
    private final JTextField projectPathField = new JTextField(30);
    
    private boolean saved = false;

    public ProjectSettingsDialog(JFrame owner, IDEController controller) {
        super(owner, "Project Settings", true);
        this.ideController = controller;
        this.wizardController = null;
        this.settings = controller.getCurrentProject().getProjectSettings();
        this.databaseFileSection = new DatabaseFileSection(this::chooseDatabaseFile);
        
        initializeDialog(owner);
    }
    
    /**
     * Constructor for WizardController.
     * 
     * @param owner The parent frame
     * @param controller The WizardController
     */
    public ProjectSettingsDialog(JFrame owner, WizardController controller) {
        super(owner, "Project Settings", true);
        this.ideController = null;
        this.wizardController = controller;
        this.settings = controller.getProjectSettings();
        this.databaseFileSection = new DatabaseFileSection(this::chooseDatabaseFile);
        
        initializeDialog(owner);
    }
    
    private Path chooseDatabaseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Database Files (*.sql, *.csv)", "sql", "csv"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().toPath();
        }
        return null;
    }
    
    private void initializeDialog(JFrame owner) {
        setLayout(new BorderLayout(8, 8));
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        
        // Add tabs
        tabbedPane.addTab("Project Info", createProjectInfoTab());
        tabbedPane.addTab("Workflows", createWorkflowsTab());
        tabbedPane.addTab("Global Data", createGlobalDataTab());
        tabbedPane.addTab("Database", createDatabaseTab());
        
        add(tabbedPane, BorderLayout.CENTER);
        
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
        
        setSize(900, 700);
        setLocationRelativeTo(owner);
    }

    /**
     * Creates the project info tab panel.
     */
    private JPanel createProjectInfoTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Project Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        namePanel.setOpaque(false);
        namePanel.add(new JLabel("Project Name:"));
        namePanel.add(UiUtils.createHelpButton(
            "Project Name",
            "The name of your project. This is used for identification and display purposes.",
            this));
        panel.add(namePanel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(projectNameField, gbc);
        
        // Project Path
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pathPanel.setOpaque(false);
        pathPanel.add(new JLabel("Project Path:"));
        pathPanel.add(UiUtils.createHelpButton(
            "Project Path",
            "The file system path where your project is located. This can be an absolute or relative path.",
            this));
        panel.add(pathPanel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(projectPathField, gbc);
        
        // Add empty space to push content to top
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }

    /**
     * Creates the workflows tab panel.
     */
    private JPanel createWorkflowsTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Label with help button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        labelPanel.setOpaque(false);
        labelPanel.add(new JLabel("Project-wide Workflows:"));
        labelPanel.add(UiUtils.createHelpButton(
            "Project Workflows",
            "Define workflows that apply across the entire project. These workflows work the same way as module workflows " +
            "but are available to all modules. Use this for common user journeys or system processes that span multiple modules.",
            this));
        panel.add(labelPanel, gbc);
        
        // Workflow list
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane listScroll = new JScrollPane(workflowList);
        panel.add(listScroll, gbc);
        
        // CRUD buttons
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("Add Workflow");
        addButton.addActionListener(event -> addWorkflow());
        
        JButton editButton = new JButton("Edit Workflow");
        editButton.addActionListener(event -> editWorkflow());
        
        JButton deleteButton = new JButton("Delete Workflow");
        deleteButton.addActionListener(event -> deleteWorkflow());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, gbc);
        
        return panel;
    }

    /**
     * Creates the global data tab panel.
     */
    private JPanel createGlobalDataTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Label with help button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        labelPanel.setOpaque(false);
        labelPanel.add(new JLabel("Global Variables:"));
        labelPanel.add(UiUtils.createHelpButton(
            "Global Variables",
            "Define global variables that can be referenced in prompts across all modules. " +
            "These are name-value pairs that can be used to store project-wide configuration values, " +
            "constants, or any other data that should be accessible throughout the project.",
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
     * Creates the database tab panel.
     */
    private JPanel createDatabaseTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Database description label with help button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        labelPanel.setOpaque(false);
        labelPanel.add(new JLabel("Database System Description:"));
        labelPanel.add(UiUtils.createHelpButton(
            "Database Description",
            "Describe the database system being used for this project (e.g., MySQL, PostgreSQL, SQLite, MongoDB). " +
            "Include version information and any relevant configuration details. " +
            "Note: This is NOT for describing the database structure/schema - only the database system itself.",
            this));
        panel.add(labelPanel, gbc);
        
        // Database text area
        gbc.gridy = 1;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        databaseArea.setLineWrap(true);
        databaseArea.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(databaseArea);
        panel.add(textScroll, gbc);
        
        // Database definition file section
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(databaseFileSection.getComponent(), gbc);
        
        return panel;
    }

    /**
     * Loads the current settings into the UI.
     */
    private void loadSettings() {
        // Load project info
        projectNameField.setText(settings.getProjectName());
        projectPathField.setText(settings.getProjectPath());
        
        // Load workflows
        for (WorkflowItem workflow : settings.getProjectWorkflows()) {
            workflowModel.addElement(workflow);
        }
        
        // Load global variables
        for (GlobalVariable variable : settings.getGlobalVariables()) {
            variableModel.addElement(variable);
        }
        
        // Load database description
        databaseArea.setText(settings.getDatabaseDescription());
        
        // Load database definition file
        databaseFileSection.setPath(settings.getDatabaseDefinitionFile());
    }

    /**
     * Saves the settings from the UI back to the ProjectSettings object.
     */
    private void saveSettings() {
        // Save project info
        settings.setProjectName(projectNameField.getText().trim());
        settings.setProjectPath(projectPathField.getText().trim());
        
        // Save workflows
        List<WorkflowItem> workflows = new ArrayList<>();
        for (int i = 0; i < workflowModel.size(); i++) {
            workflows.add(workflowModel.get(i));
        }
        settings.setProjectWorkflows(workflows);
        
        // Save global variables
        List<GlobalVariable> variables = new ArrayList<>();
        for (int i = 0; i < variableModel.size(); i++) {
            variables.add(variableModel.get(i));
        }
        settings.setGlobalVariables(variables);
        
        // Save database description
        settings.setDatabaseDescription(databaseArea.getText().trim());
        
        // Save database definition file
        settings.setDatabaseDefinitionFile(databaseFileSection.getPath());
        
        // Mark project as dirty
        if (ideController != null) {
            ideController.markDirty();
        } else if (wizardController != null) {
            wizardController.markDirty();
        }
    }

    /**
     * Adds a new workflow.
     */
    private void addWorkflow() {
        // Get dialogs from all modules for the workflow dialog
        List<DialogDefinition> allDialogs = new ArrayList<>();
        if (ideController != null && ideController.getCurrentProject() != null) {
            for (Module module : ideController.getCurrentProject().getAllModules()) {
                if (module.getTaskData() != null && module.getTaskData().getDialogs() != null) {
                    allDialogs.addAll(module.getTaskData().getDialogs());
                }
            }
        }
        // For WizardController, dialogs come from mainTaskData
        else if (wizardController != null && wizardController.getMainTaskData() != null) {
            if (wizardController.getMainTaskData().getDialogs() != null) {
                allDialogs.addAll(wizardController.getMainTaskData().getDialogs());
            }
        }
        
        WorkflowItemDialog dialog = new WorkflowItemDialog((JFrame) getOwner(), null, allDialogs);
        dialog.setVisible(true);
        WorkflowItem workflow = dialog.getResult();
        if (workflow != null) {
            workflowModel.addElement(workflow);
        }
    }

    /**
     * Edits the selected workflow.
     */
    private void editWorkflow() {
        int index = workflowList.getSelectedIndex();
        if (index < 0) {
            UiUtils.showWarning(this, "Please select a workflow to edit.", "No Selection");
            return;
        }
        
        WorkflowItem workflow = workflowModel.get(index);
        
        // Get dialogs from all modules
        List<DialogDefinition> allDialogs = new ArrayList<>();
        if (ideController != null && ideController.getCurrentProject() != null) {
            for (Module module : ideController.getCurrentProject().getAllModules()) {
                if (module.getTaskData() != null && module.getTaskData().getDialogs() != null) {
                    allDialogs.addAll(module.getTaskData().getDialogs());
                }
            }
        }
        // For WizardController, dialogs come from mainTaskData
        else if (wizardController != null && wizardController.getMainTaskData() != null) {
            if (wizardController.getMainTaskData().getDialogs() != null) {
                allDialogs.addAll(wizardController.getMainTaskData().getDialogs());
            }
        }
        
        WorkflowItemDialog dialog = new WorkflowItemDialog((JFrame) getOwner(), workflow, allDialogs);
        dialog.setVisible(true);
        WorkflowItem updatedWorkflow = dialog.getResult();
        if (updatedWorkflow != null) {
            workflowModel.set(index, updatedWorkflow);
        }
    }

    /**
     * Deletes the selected workflow.
     */
    private void deleteWorkflow() {
        int index = workflowList.getSelectedIndex();
        if (index < 0) {
            UiUtils.showWarning(this, "Please select a workflow to delete.", "No Selection");
            return;
        }
        
        workflowModel.remove(index);
    }

    /**
     * Adds a new global variable.
     */
    private void addVariable() {
        // Get existing global variables for validation
        List<GlobalVariable> existingVars = new ArrayList<>();
        for (int i = 0; i < variableModel.size(); i++) {
            existingVars.add(variableModel.get(i));
        }
        
        // Get module variables for conflict checking
        List<ModuleVariable> allModuleVars = new ArrayList<>();

        if (ideController != null && ideController.getCurrentProject() != null) {
            for (Module module : ideController.getCurrentProject().getAllModules()) {
                if (module.getModuleVariables() != null) {
                    allModuleVars.addAll(module.getModuleVariables());
                }
            }
        }
        
        GlobalVariableDialog dialog = new GlobalVariableDialog((JFrame) getOwner(), null, existingVars, allModuleVars);
        dialog.setVisible(true);
        GlobalVariable variable = dialog.getResult();
        if (variable != null) {
            variableModel.addElement(variable);
        }
    }

    /**
     * Edits the selected global variable.
     */
    private void editVariable() {
        int index = variableList.getSelectedIndex();
        if (index < 0) {
            UiUtils.showWarning(this, "Please select a variable to edit.", "No Selection");
            return;
        }
        
        GlobalVariable variable = variableModel.get(index);
        
        // Get existing global variables for validation
        List<GlobalVariable> existingVars = new ArrayList<>();
        for (int i = 0; i < variableModel.size(); i++) {
            existingVars.add(variableModel.get(i));
        }
        
        // Get module variables for conflict checking
        List<ModuleVariable> allModuleVars = new ArrayList<>();
        if (ideController != null && ideController.getCurrentProject() != null) {
            for (Module module : ideController.getCurrentProject().getAllModules()) {
                if (module.getModuleVariables() != null) {
                    allModuleVars.addAll(module.getModuleVariables());
                }
            }
        }
        
        GlobalVariableDialog dialog = new GlobalVariableDialog((JFrame) getOwner(), variable, existingVars, allModuleVars);
        dialog.setVisible(true);
        GlobalVariable updatedVariable = dialog.getResult();
        if (updatedVariable != null) {
            variableModel.set(index, updatedVariable);
        }
    }

    /**
     * Deletes the selected global variable.
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
