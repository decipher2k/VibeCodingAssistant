/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog for selecting the task type at startup.
 * This shows task selection before routing to IDE or classic wizard.
 */
public final class TaskSelectionDialog extends JDialog {
    private TaskType selectedTask;
    private final Map<TaskType, JRadioButton> taskButtons = new HashMap<>();
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private boolean loadExistingProject;
    private Path existingProjectFile;
    
    public TaskSelectionDialog(JFrame parent) {
        super(parent, "Select Task", true);
        this.selectedTask = null;
        this.loadExistingProject = false;
        this.existingProjectFile = null;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Create the main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        mainPanel.setOpaque(true);
        mainPanel.setBackground(ThemeManager.getBackgroundColor());
        
        // Header
        JLabel headerLabel = new JLabel("Select Task");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 16f));
        JButton helpButton = UiUtils.createHelpButton(
            "Task Types",
            "Choose the automation you would like GitHub Copilot CLI to perform. Each task exposes a tailored workflow:\n\n" +
            "• Generate app or script - Opens the IDE for full project development\n" +
            "• Other tasks - Opens the classic wizard for specific automation tasks",
            this);
        
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(headerLabel);
        headerPanel.add(helpButton);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Task buttons
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        taskPanel.setOpaque(false);
        
        for (TaskType task : TaskType.values()) {
            JRadioButton button = new JRadioButton(task.toString());
            button.setOpaque(false);
            button.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            taskButtons.put(task, button);
            buttonGroup.add(button);
            taskPanel.add(button);
        }
        
        // Pre-select the first task (Generate app)
        taskButtons.get(TaskType.GENERATE_APP_OR_SCRIPT).setSelected(true);
        
        JScrollPane scrollPane = new JScrollPane(taskPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Footer with buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        
        JButton loadProjectButton = new JButton("Load Existing VCP Project");
        loadProjectButton.addActionListener(e -> handleLoadExistingProject());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            selectedTask = null;
            loadExistingProject = false;
            dispose();
        });
        
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> {
            selectedTask = getSelectedTask();
            if (selectedTask == null) {
                JOptionPane.showMessageDialog(this,
                    "Please select a task to continue.",
                    "Task Required",
                    JOptionPane.WARNING_MESSAGE);
            } else {
                dispose();
            }
        });
        
        footer.add(loadProjectButton);
        footer.add(cancelButton);
        footer.add(continueButton);
        mainPanel.add(footer, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void handleLoadExistingProject() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Vibe Coding Project (*.vcp)", "vcp"));
        fileChooser.setDialogTitle("Load Existing VCP Project");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path vcpFile = fileChooser.getSelectedFile().toPath();
            
            if (java.nio.file.Files.exists(vcpFile) && vcpFile.toString().endsWith(".vcp")) {
                loadExistingProject = true;
                existingProjectFile = vcpFile;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid or non-existent .vcp file selected.",
                    "Invalid File",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private TaskType getSelectedTask() {
        return taskButtons.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Gets the selected task after the dialog is closed.
     * 
     * @return The selected task type, or null if cancelled or loading existing project
     */
    public TaskType getResult() {
        return selectedTask;
    }
    
    /**
     * Returns whether the user chose to load an existing project.
     * 
     * @return true if loading existing project, false otherwise
     */
    public boolean isLoadExistingProject() {
        return loadExistingProject;
    }
    
    /**
     * Gets the path to the existing project file to load.
     * 
     * @return The path to the .vcp file, or null if not loading
     */
    public Path getExistingProjectFile() {
        return existingProjectFile;
    }
}
