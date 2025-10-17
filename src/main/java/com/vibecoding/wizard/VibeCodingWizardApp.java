/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class VibeCodingWizardApp {

    private VibeCodingWizardApp() {
    }

    public static void main(String[] args) {
        ThemeManager.apply();
        if (Boolean.getBoolean("vibecodingwizard.skipUi")) {
            System.out.println("Vibe Coding Wizard UI launch skipped via system property.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            // Perform system checks first (unless explicitly skipped)
            if (!Boolean.getBoolean("vibecodingwizard.skipSystemCheck")) {
                boolean checksOk = StartupChecker.performChecks(null);
                if (!checksOk) {
                    System.exit(1);
                }
            }
            
            // Show task selection dialog
            TaskSelectionDialog taskDialog = new TaskSelectionDialog(null);
            taskDialog.setVisible(true);
            
            // Check if user wants to load an existing project
            if (taskDialog.isLoadExistingProject()) {
                java.nio.file.Path vcpFile = taskDialog.getExistingProjectFile();
                if (vcpFile != null) {
                    System.out.println("=== Vibe Coding Assistant Launch ===");
                    System.out.println("Loading existing project: " + vcpFile);
                    
                    // Load the project
                    IDEProject project = ProjectSerializer.load(vcpFile);
                    if (project == null) {
                        JOptionPane.showMessageDialog(null,
                            "Failed to load the project file.",
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                        return;
                    }
                    
                    // Prompt user to select the project directory
                    java.nio.file.Path projectDirectory = promptForProjectDirectory(project);
                    if (projectDirectory == null) {
                        // User cancelled directory selection
                        System.exit(0);
                        return;
                    }
                    
                    // Update the project with the selected directory
                    if (project.getInitialConfig() != null) {
                        // Create a new InitialConfig with the updated directory
                        InitialConfig oldConfig = project.getInitialConfig();
                        InitialConfig newConfig = new InitialConfig(
                            oldConfig.getProgrammingLanguage(),
                            oldConfig.getProjectStyle(),
                            oldConfig.getTargetOperatingSystems(),
                            projectDirectory,
                            oldConfig.getProgramMode(),
                            oldConfig.getProjectName(),
                            oldConfig.getIdeOrWizardMode()
                        );
                        project.setInitialConfig(newConfig);
                    }
                    
                    // Update project settings with the new directory
                    if (project.getProjectSettings() != null) {
                        project.getProjectSettings().setProjectPath(projectDirectory.toString());
                    }
                    
                    // Determine the mode from the project's InitialConfig
                    ProjectMode mode = ProjectMode.IDE; // Default to IDE
                    if (project.getInitialConfig() != null && project.getInitialConfig().getIdeOrWizardMode() != null) {
                        mode = project.getInitialConfig().getIdeOrWizardMode();
                    }
                    
                    System.out.println("Mode: " + mode);
                    System.out.println("=====================================");
                    
                    if (mode == ProjectMode.IDE) {
                        // Launch IDE and load the project
                        IDEController ideController = new IDEController();
                        ideController.loadExistingProject(project, vcpFile);
                        ideController.getFrame().setLocationRelativeTo(null);
                        ideController.getFrame().setVisible(true);
                    } else {
                        // Launch Wizard and load the project
                        WizardController controller = new WizardController();
                        controller.loadExistingProject(project);
                        controller.getFrame().setLocationRelativeTo(null);
                        controller.getFrame().setVisible(true);
                    }
                }
                return;
            }
            
            TaskType selectedTask = taskDialog.getResult();
            if (selectedTask == null) {
                // User cancelled
                System.out.println("Task selection cancelled. Exiting.");
                System.exit(0);
                return;
            }
            
            System.out.println("=== Vibe Coding Assistant Launch ===");
            System.out.println("Selected Task: " + selectedTask);
            
            // Route based on selected task
            if (selectedTask == TaskType.GENERATE_APP_OR_SCRIPT) {
                // Launch IDE mode for new app generation
                System.out.println("Mode: IDE");
                System.out.println("=====================================");
                IDEController ideController = new IDEController();
                ideController.show();
            } else {
                // Launch classic wizard mode for other tasks
                System.out.println("Mode: CLASSIC WIZARD");
                System.out.println("=====================================");
                WizardController controller = new WizardController();
                controller.showWithTask(selectedTask);
            }
        });
    }
    
    /**
     * Prompts the user to select the project directory for a loaded VCP file.
     * Shows the current directory from the project as a suggestion.
     * 
     * @param project The loaded project
     * @return The selected project directory, or null if cancelled
     */
    private static java.nio.file.Path promptForProjectDirectory(IDEProject project) {
        // Get the suggested directory from the project
        String suggestedPath = null;
        if (project.getInitialConfig() != null && project.getInitialConfig().getProjectDirectory() != null) {
            suggestedPath = project.getInitialConfig().getProjectDirectory().toString();
        } else if (project.getProjectSettings() != null && project.getProjectSettings().getProjectPath() != null) {
            suggestedPath = project.getProjectSettings().getProjectPath();
        }
        
        // Show dialog to user
        String message = "Please select the project directory for this VCP file.";
        if (suggestedPath != null) {
            message += "\n\nPrevious directory: " + suggestedPath;
        }
        
        int result = JOptionPane.showConfirmDialog(null,
            message,
            "Select Project Directory",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        
        // Show directory chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Project Directory");
        
        // Set initial directory to the suggested path if it exists
        if (suggestedPath != null) {
            java.io.File suggestedDir = new java.io.File(suggestedPath);
            if (suggestedDir.exists() && suggestedDir.isDirectory()) {
                fileChooser.setCurrentDirectory(suggestedDir);
            }
        }
        
        int chooserResult = fileChooser.showOpenDialog(null);
        if (chooserResult == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().toPath();
        }
        
        return null;
    }
}
