/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Panel for editing a module's task data.
 * This is similar to MainDialogPanel but without the action buttons.
 */
public final class ModuleEditorPanel extends JPanel {
    private final IDEController controller;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardHolder = new JPanel(cardLayout);
    private final Map<TaskType, MainDialogPanel.TaskPanel> panels = new EnumMap<>(TaskType.class);
    private final ModuleVariablesSection moduleVariables;
    private TaskType currentTask;
    private InitialConfig currentConfig;
    private Module currentModule;

    public ModuleEditorPanel(IDEController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(ThemeManager.getBackgroundColor());
        setBorder(new EmptyBorder(8, 10, 10, 10));  // 8px top to match content inside panels
        setMinimumSize(new Dimension(400, 400));  // Ensure minimum size for split pane

        // Create task panels using JFrame variants for IDE usage
        JFrame parentFrame = controller.getFrame();
        panels.put(TaskType.GENERATE_APP_OR_SCRIPT, 
            MainDialogPanel.createGenerateAppForm(this::selectFile, parentFrame));
        panels.put(TaskType.FIX_CODING_ERRORS, 
            MainDialogPanel.createFixCodingErrorsForm());
        panels.put(TaskType.CREATE_MODULE, 
            MainDialogPanel.createCreateModuleForm(this::selectFile, parentFrame));
        panels.put(TaskType.CREATE_ALGORITHM, 
            MainDialogPanel.createCreateAlgorithmForm());
        panels.put(TaskType.MODIFY_EXISTING_SOFTWARE, 
            MainDialogPanel.createModifySoftwareForm(this::selectFile, parentFrame));

        panels.forEach((taskType, panel) -> cardHolder.add(panel.getComponent(), taskType.name()));
        cardHolder.setOpaque(false);

        // Create module variables section
        moduleVariables = new ModuleVariablesSection(parentFrame);

        // Create a container with the module variables below the cards
        // The cardHolder already has its own padding from the forms
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(cardHolder, BorderLayout.CENTER);
        
        // Wrap module variables with same padding as form content (8, 24, 8, 24)
        JPanel variablesWrapper = new JPanel(new BorderLayout());
        variablesWrapper.setOpaque(false);
        variablesWrapper.setBorder(new EmptyBorder(0, 24, 8, 24));
        variablesWrapper.add(moduleVariables.getComponent(), BorderLayout.CENTER);
        
        contentPanel.add(variablesWrapper, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
        
        showEmptyState();
    }

    /**
     * Loads a module for editing.
     */
    public void loadModule(Module module, InitialConfig config) {
        if (module == null || config == null) {
            showEmptyState();
            return;
        }

        this.currentModule = module;
        this.currentTask = module.getTaskType();
        this.currentConfig = config;

        MainDialogPanel.TaskPanel panel = panels.get(currentTask);
        if (panel == null) {
            throw new IllegalStateException("Unsupported task type: " + currentTask);
        }

        panel.load(config, module.getTaskData());
        
        // Load module variables
        moduleVariables.setVariables(module.getModuleVariables());
        
        // Load global variables for conflict checking
        if (controller.getCurrentProject() != null) {
            moduleVariables.setGlobalVariables(controller.getCurrentProject().getProjectSettings().getGlobalVariables());
        }
        
        // Show module variables section
        moduleVariables.getComponent().setVisible(true);
        
        cardLayout.show(cardHolder, currentTask.name());
    }

    /**
     * Saves the current module data.
     */
    public MainTaskData saveModule(InitialConfig config) {
        if (currentTask == null || config == null || currentModule == null) {
            return null;
        }

        MainDialogPanel.TaskPanel panel = panels.get(currentTask);
        if (panel == null) {
            return null;
        }

        MainTaskData data = panel.save(config);
        // Sync main window name from task data to module
        if (data != null) {
            currentModule.setMainWindowName(data.getMainWindowName());
        }
        
        // Save module variables
        currentModule.setModuleVariables(moduleVariables.getVariables());
        
        return data;
    }

    /**
     * Clears the editor and shows empty state.
     */
    public void clear() {
        currentModule = null;
        currentTask = null;
        currentConfig = null;
        moduleVariables.setVariables(null);
        moduleVariables.getComponent().setVisible(false);
        showEmptyState();
    }

    /**
     * Shows an empty state message.
     */
    private void showEmptyState() {
        // Show a welcome message when no module is selected
        cardHolder.removeAll();
        
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(true);
        welcomePanel.setBackground(ThemeManager.getBackgroundColor());
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Welcome to Vibe Coding IDE");
        titleLabel.setFont(titleLabel.getFont().deriveFont(24.0f).deriveFont(java.awt.Font.BOLD));
        titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Select a module from the tree to edit, or create a new project");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(14.0f));
        subtitleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(java.awt.Color.GRAY);
        
        centerPanel.add(javax.swing.Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(javax.swing.Box.createVerticalStrut(10));
        centerPanel.add(subtitleLabel);
        centerPanel.add(javax.swing.Box.createVerticalGlue());
        
        welcomePanel.add(centerPanel, BorderLayout.CENTER);
        cardHolder.add(welcomePanel);
        
        panels.forEach((taskType, panel) -> cardHolder.add(panel.getComponent(), taskType.name()));
        cardHolder.revalidate();
        cardHolder.repaint();
    }

    private Path selectFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().toPath();
        }
        return null;
    }
}
