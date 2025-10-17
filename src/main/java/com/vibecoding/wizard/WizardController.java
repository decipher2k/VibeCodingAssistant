/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.nio.file.Path;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class WizardController {
    private final WizardFrame frame;
    private InitialConfig initialConfig;
    private TaskType taskType;
    private boolean taskPreSelected = false;  // Track if task was pre-selected at startup
    private final MainTaskData mainTaskData = new MainTaskData();
    private final CopilotCliService copilotCliService = new CopilotCliService(Path.of("").toAbsolutePath());
    private final TemplateManager templateManager = new TemplateManager();
    private boolean isDirty = false;
    private Path currentProjectFile = null;
    private final ProjectSettings projectSettings = new ProjectSettings();

    public WizardController() {
        this.frame = new WizardFrame(this);
    }

    public void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.showInitialPanel();
    }
    
    /**
     * Shows the wizard with a pre-selected task type.
     * This skips task selection and goes straight to initial config, then to the main dialog.
     * 
     * @param taskType The task type to use
     */
    public void showWithTask(TaskType taskType) {
        this.taskType = taskType;
        this.taskPreSelected = true;  // Mark that task was pre-selected
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.showInitialPanel();
    }

    void submitInitialConfig(InitialConfig config) {
        this.initialConfig = config;
        markDirty();
        
        // If taskType is already set, skip task selection and go straight to main dialog
        if (taskType != null) {
            frame.showMainDialog();
        } else {
            frame.showTaskSelection();
        }
    }

    void goBackToInitial() {
        // Load the current config into the initial panel before showing it
        if (initialConfig != null) {
            frame.loadInitialPanelConfig(initialConfig);
        }
        frame.showInitialPanel();
    }

    void submitTaskType(TaskType taskType) {
        this.taskType = taskType;
        markDirty();
        frame.showMainDialog();
    }

    void goBackToTaskSelection() {
        // If task was pre-selected at startup, go back to initial config instead
        // (since task selection was already done in the startup dialog)
        if (taskPreSelected) {
            goBackToInitial();
        } else {
            frame.showTaskSelection();
        }
    }

    InitialConfig getInitialConfig() {
        return initialConfig;
    }

    TaskType getTaskType() {
        return taskType;
    }

    MainTaskData getMainTaskData() {
        return mainTaskData;
    }

    JFrame getFrame() {
        return frame;
    }
    
    /**
     * Gets the project settings.
     * 
     * @return The project settings
     */
    ProjectSettings getProjectSettings() {
        return projectSettings;
    }
    
    /**
     * Marks the project as having unsaved changes.
     */
    void markDirty() {
        this.isDirty = true;
    }
    
    /**
     * Marks the project as saved (no unsaved changes).
     */
    private void markClean() {
        this.isDirty = false;
    }
    
    /**
     * Checks if there are unsaved changes and confirms with user before proceeding.
     * 
     * @param actionName The name of the action being performed (e.g., "create new project", "load project")
     * @return true if we should proceed, false if user cancelled
     */
    private boolean confirmIfDirty(String actionName) {
        if (!isDirty) {
            return true;
        }
        
        int result = JOptionPane.showConfirmDialog(frame,
            "You have unsaved changes. Are you sure you want to " + actionName + "?\n" +
            "All unsaved changes will be lost.",
            "Unsaved Changes",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Saves a project template using the template manager.
     * 
     * @param template The template to save
     */
    void saveTemplate(ProjectTemplate template) {
        templateManager.saveTemplate(frame, template);
    }

    /**
     * Loads a project template using the template manager.
     * 
     * @return The loaded template, or null if loading was cancelled or failed
     */
    ProjectTemplate loadTemplate() {
        return templateManager.loadTemplate(frame);
    }

    void prepareTaskExecution(MainTaskData data) {
        if (initialConfig == null || taskType == null) {
            JOptionPane.showMessageDialog(frame,
                "Please finish the earlier steps before performing the task.",
                "Wizard Incomplete",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if directory is not empty for new project tasks
        if (taskType == TaskType.GENERATE_APP_OR_SCRIPT && initialConfig.getProjectDirectory() != null) {
            Path projectDir = initialConfig.getProjectDirectory();
            try {
                if (java.nio.file.Files.exists(projectDir)) {
                    try (java.util.stream.Stream<Path> entries = java.nio.file.Files.list(projectDir)) {
                        if (entries.findAny().isPresent()) {
                            // Directory is not empty - ask user to select corresponding .vcp file
                            int result = JOptionPane.showConfirmDialog(frame,
                                "The selected project directory is not empty:\n" + projectDir.toAbsolutePath() + 
                                "\n\nYou need to select the corresponding .vcp project file to continue.\n\n" +
                                "Do you want to select the .vcp file now?",
                                "Directory Not Empty",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                            
                            if (result == JOptionPane.YES_OPTION) {
                                // Show file chooser for .vcp file
                                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                                fileChooser.setCurrentDirectory(projectDir.toFile());
                                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                                    "Vibe Coding Project (*.vcp)", "vcp"));
                                
                                int chooserResult = fileChooser.showOpenDialog(frame);
                                if (chooserResult == javax.swing.JFileChooser.APPROVE_OPTION) {
                                    Path vcpFile = fileChooser.getSelectedFile().toPath();
                                    
                                    // Try to load the .vcp file
                                    if (java.nio.file.Files.exists(vcpFile) && vcpFile.toString().endsWith(".vcp")) {
                                        // Valid .vcp file selected - we can proceed
                                        // (The actual loading would be handled separately in the execution)
                                        currentProjectFile = vcpFile;
                                    } else {
                                        JOptionPane.showMessageDialog(frame,
                                            "Invalid or non-existent .vcp file selected.\n" +
                                            "Returning to task selection.",
                                            "Invalid File",
                                            JOptionPane.WARNING_MESSAGE);
                                        frame.showTaskSelection();
                                        return;
                                    }
                                } else {
                                    // User cancelled file selection
                                    JOptionPane.showMessageDialog(frame,
                                        "No .vcp file selected.\n" +
                                        "Returning to task selection.",
                                        "No File Selected",
                                        JOptionPane.WARNING_MESSAGE);
                                    frame.showTaskSelection();
                                    return;
                                }
                            } else {
                                // User chose not to select a .vcp file
                                JOptionPane.showMessageDialog(frame,
                                    "Cannot proceed without a .vcp file for non-empty directory.\n" +
                                    "Returning to task selection.",
                                    "Operation Cancelled",
                                    JOptionPane.WARNING_MESSAGE);
                                frame.showTaskSelection();
                                return;
                            }
                        }
                    }
                }
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(frame,
                    "Could not check project directory: " + ex.getMessage(),
                    "Directory Check Failed",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        mainTaskData.copyFrom(data);
        markDirty();

        if (Boolean.getBoolean("vibecodingwizard.skipExecution")) {
            return;
        }

        TaskExecutionDialog dialog = new TaskExecutionDialog(frame);
        TaskExecutionWorker worker = new TaskExecutionWorker(dialog);
        worker.execute();
        dialog.setVisible(true);
    }
    
    /**
     * Handles the File > New menu action.
     * Creates a new project after confirming with user if there are unsaved changes.
     */
    void handleNew() {
        if (!confirmIfDirty("create a new project")) {
            return;
        }
        
        // Reset all state
        initialConfig = null;
        taskType = null;
        mainTaskData.clear();
        currentProjectFile = null;
        markClean();
        
        // Return to initial panel
        frame.showInitialPanel();
    }
    
    /**
     * Handles the File > Load menu action.
     * Loads a project template after confirming with user if there are unsaved changes.
     */
    void handleLoad() {
        // Only confirm if dirty when we already have an initial config
        // If we don't have a config yet, loading is safe
        if (initialConfig != null && !confirmIfDirty("load a project")) {
            return;
        }
        
        // Load template
        ProjectTemplate template = templateManager.loadTemplate(frame);
        if (template == null) {
            // User cancelled or load failed
            return;
        }
        
        // If we don't have an initial config yet, we need to prompt for project directory
        if (initialConfig == null && template.getTaskType() == TaskType.GENERATE_APP_OR_SCRIPT) {
            JOptionPane.showMessageDialog(frame,
                "The loaded template will be applied.\n" +
                "You will need to select a project directory in the next step.",
                "Set Project Directory",
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Apply the loaded template
        applyTemplate(template);
        currentProjectFile = null; // Template loaded but not associated with a specific file yet
        markClean();
        
        JOptionPane.showMessageDialog(frame,
            "Project loaded successfully.\n" +
            "Template data has been loaded.",
            "Load Complete",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Handles the File > Save menu action.
     * Saves the current project, with confirmation if file already exists.
     */
    void handleSave() {
        if (initialConfig == null || taskType == null) {
            JOptionPane.showMessageDialog(frame,
                "Please complete the initial configuration and select a task before saving.",
                "Cannot Save",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get the current data from the UI
        MainTaskData currentData = frame.getCurrentTaskData();
        if (currentData == null) {
            JOptionPane.showMessageDialog(frame,
                "Unable to save: no task data available.",
                "Cannot Save",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create template with current data and project settings
        ProjectTemplate template = new ProjectTemplate(
            initialConfig.getProgrammingLanguage(),
            initialConfig.getProjectStyle(),
            initialConfig.getTargetOperatingSystems(),
            taskType,
            currentData,
            initialConfig.getProjectDirectory(),
            projectSettings
        );
        
        // Save using template manager
        boolean saved = templateManager.saveTemplateWithConfirmation(frame, template);
        if (saved) {
            // Update mainTaskData with the saved data
            mainTaskData.copyFrom(currentData);
            markClean();
            JOptionPane.showMessageDialog(frame,
                "Project saved successfully.",
                "Save Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Opens the project settings dialog.
     */
    void handleProjectSettings() {
        ProjectSettingsDialog dialog = new ProjectSettingsDialog(frame, this);
        dialog.setVisible(true);
    }
    
    /**
     * Handles the File > Exit menu action and window close events.
     * Exits the application after confirming with user if there are unsaved changes.
     */
    void handleExit() {
        if (!confirmIfDirty("exit the application")) {
            return;
        }
        
        System.exit(0);
    }
    
    /**
     * Applies a loaded template to the current state.
     */
    private void applyTemplate(ProjectTemplate template) {
        // Use the project directory from the template if available,
        // otherwise try to keep the current one
        Path projectDir = template.getProjectDirectory();
        if (projectDir == null && initialConfig != null) {
            projectDir = initialConfig.getProjectDirectory();
        }
        
        // Create initial config from template with the loaded project directory
        initialConfig = new InitialConfig(
            template.getProgrammingLanguage(),
            template.getProjectStyle(),
            template.getTargetOperatingSystems(),
            projectDir
        );
        
        taskType = template.getTaskType();
        
        // Clear and copy the task data
        mainTaskData.clear();
        if (template.getTaskData() != null) {
            mainTaskData.copyFrom(template.getTaskData());
        }
        
        // Load project settings from template
        if (template.getProjectSettings() != null) {
            projectSettings.setGlobalVariables(template.getProjectSettings().getGlobalVariables());
            projectSettings.setProjectWorkflows(template.getProjectSettings().getProjectWorkflows());
            projectSettings.setDatabaseDescription(template.getProjectSettings().getDatabaseDescription());
        } else {
            // Clear project settings if template doesn't have them
            projectSettings.clear();
        }
        
        // Update the initial panel with the loaded configuration
        // so if the user goes back, they see the correct values
        frame.loadInitialPanelConfig(initialConfig);
        
        // Navigate to main dialog to show the loaded data
        frame.showMainDialog();
    }
    
    /**
     * Loads an existing project from an IDEProject (typically from a .vcp file).
     * This is used when loading a VCP file that was created in wizard mode.
     * 
     * @param project The IDE project to load
     */
    public void loadExistingProject(IDEProject project) {
        // Extract data from the IDE project
        initialConfig = project.getInitialConfig();
        
        // Get the main module's task type and data
        Module mainModule = project.getMainModule();
        if (mainModule != null) {
            taskType = mainModule.getTaskType();
            mainTaskData.clear();
            if (mainModule.getTaskData() != null) {
                mainTaskData.copyFrom(mainModule.getTaskData());
            }
        }
        
        // Load project settings
        if (project.getProjectSettings() != null) {
            projectSettings.setGlobalVariables(project.getProjectSettings().getGlobalVariables());
            projectSettings.setProjectWorkflows(project.getProjectSettings().getProjectWorkflows());
            projectSettings.setDatabaseDescription(project.getProjectSettings().getDatabaseDescription());
            projectSettings.setProjectName(project.getProjectSettings().getProjectName());
            projectSettings.setProjectPath(project.getProjectSettings().getProjectPath());
        }
        
        // Mark as clean since we just loaded
        markClean();
        
        // Update the initial panel with the loaded configuration
        if (initialConfig != null) {
            frame.loadInitialPanelConfig(initialConfig);
        }
        
        // Navigate to main dialog if we have both config and task type
        if (initialConfig != null && taskType != null) {
            frame.showMainDialog();
        } else {
            // Otherwise, show initial panel
            frame.showInitialPanel();
        }
    }

    private final class TaskExecutionWorker extends SwingWorker<Boolean, String> {
        private final TaskExecutionDialog dialog;
        private String finalCompileErrors = "";
        private Exception failure;

        private TaskExecutionWorker(TaskExecutionDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        protected Boolean doInBackground() {
            try {
                // Ensure project directory exists before calling Copilot CLI
                if (initialConfig.getProjectDirectory() != null) {
                    dialog.setStatus("Preparing project directory...");
                    Path projectDir = initialConfig.getProjectDirectory();
                    if (!java.nio.file.Files.exists(projectDir)) {
                        dialog.appendLog("Creating project directory: " + projectDir.toAbsolutePath());
                        try {
                            java.nio.file.Files.createDirectories(projectDir);
                            dialog.appendLog("Project directory created successfully.");
                        } catch (java.io.IOException ex) {
                            dialog.appendLog("Failed to create project directory: " + ex.getMessage());
                            throw ex;
                        }
                    } else {
                        dialog.appendLog("Project directory exists: " + projectDir.toAbsolutePath());
                    }
                }
                
                dialog.setStatus("Generating prompt...");
                String prompt = PromptBuilder.buildPrimaryPrompt(taskType, initialConfig, mainTaskData, projectSettings);
                dialog.appendLog("Primary prompt generated.");

                // Save the prompt to a file in the project directory
                dialog.setStatus("Saving prompt to file...");
                Path projectDir = initialConfig.getProjectDirectory();
                Path promptFile = projectDir.resolve("prompt.txt");
                try {
                    java.nio.file.Files.writeString(promptFile, prompt, java.nio.charset.StandardCharsets.UTF_8);
                    dialog.appendLog("Prompt saved to: " + promptFile.toAbsolutePath());
                    dialog.appendLog("Prompt length: " + prompt.length() + " characters");
                } catch (java.io.IOException ex) {
                    dialog.appendLog("Failed to save prompt to file: " + ex.getMessage());
                    throw ex;
                }

                // Use a simplified prompt that instructs Copilot to read from the file
                String fileReadPrompt = "Process the instructions in prompt.txt and execute them.";
                
                dialog.setStatus("Calling GitHub Copilot CLI (interactive mode)...");
                dialog.appendLog("--- Copilot CLI running interactively ---");
                dialog.appendLog("You can provide input (e.g., sudo password) in the input field below.");
                dialog.appendLog("");
                
                ProcessAndResult processAndResult = copilotCliService.runPrimaryTaskInteractive(
                    taskType, fileReadPrompt, line -> dialog.appendLog(line));
                
                // Connect the process input stream to the dialog
                dialog.setProcessInputStream(processAndResult.getProcessInput());
                
                // Wait for completion
                ProcessResult primaryResult = processAndResult.waitForCompletion();
                dialog.closeProcessInputStream();
                
                dialog.appendLog("--- Copilot CLI completed ---");
                dialog.appendLog("Copilot CLI exit code: " + primaryResult.getExitCode());
                if (!primaryResult.isSuccess()) {
                    dialog.appendLog("Copilot CLI failed with exit code: " + primaryResult.getExitCode());
                    return false;
                }

                BuildCommandPlanner.BuildPlan plan = BuildCommandPlanner.plan(
                    initialConfig.getProgrammingLanguage(),
                    initialConfig.getProjectStyle(),
                    initialConfig);

                dialog.appendLog("Build plan: " + plan.getDescription());

                ProcessResult buildResult = runBuildCommands(plan.getCommands());
                if (buildResult.isSuccess()) {
                    dialog.appendLog("Build succeeded on first attempt.");
                    return true;
                }

                finalCompileErrors = mergeOutput(buildResult);
                dialog.appendLog("Initial build failed. Entering auto-fix loop.");

                for (int attempt = 1; attempt <= 10; attempt++) {
                    dialog.setStatus("Fix attempt " + attempt + " via Copilot CLI...");
                    String fixPrompt = PromptBuilder.buildFixPrompt(taskType, initialConfig, mainTaskData,
                        finalCompileErrors, attempt);
                    dialog.appendLog("--- Copilot CLI fix attempt " + attempt + " (interactive mode) ---");
                    
                    ProcessAndResult fixProcessAndResult = copilotCliService.runFixAttemptInteractive(
                        fixPrompt, attempt, line -> dialog.appendLog(line));
                    
                    dialog.setProcessInputStream(fixProcessAndResult.getProcessInput());
                    ProcessResult fixResult = fixProcessAndResult.waitForCompletion();
                    dialog.closeProcessInputStream();
                    
                    dialog.appendLog("--- Copilot CLI fix attempt " + attempt + " completed ---");
                    dialog.appendLog("Fix attempt " + attempt + " exit code: " + fixResult.getExitCode());

                    if (!fixResult.isSuccess()) {
                        dialog.appendLog("Fix attempt " + attempt + " failed to execute successfully.");
                        continue;
                    }

                    dialog.setStatus("Re-running build (attempt " + attempt + ")...");
                    buildResult = runBuildCommands(plan.getCommands());
                    if (buildResult.isSuccess()) {
                        dialog.appendLog("Build succeeded after fix attempt " + attempt + '.');
                        finalCompileErrors = "";
                        return true;
                    }
                    finalCompileErrors = mergeOutput(buildResult);
                }

                dialog.appendLog("All automated fix attempts exhausted.");
                return false;
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                failure = interrupted;
                return false;
            } catch (Exception ex) {
                failure = ex;
                dialog.appendLog("Unexpected failure: " + ex.getMessage());
                return false;
            }
        }

        private ProcessResult runBuildCommands(List<List<String>> commands) throws Exception {
            ProcessResult lastResult = new ProcessResult(0, "", "");
            // Use the project directory if specified, otherwise use current working directory
            Path workingDirectory = initialConfig.getProjectDirectory() != null 
                ? initialConfig.getProjectDirectory() 
                : Path.of("").toAbsolutePath();
            
            for (List<String> command : commands) {
                dialog.setStatus("Running: " + String.join(" ", command));
                dialog.appendLog("Working directory: " + workingDirectory.toAbsolutePath());
                // Stream output to dialog for real-time feedback
                ProcessResult result = ProcessRunner.runWithStreaming(command, workingDirectory, null, 
                    line -> dialog.appendLog(line));
                logProcess("Build command", result);
                lastResult = result;
                if (!result.isSuccess()) {
                    break;
                }
            }
            return lastResult;
        }

        private void logProcess(String label, ProcessResult result) {
            dialog.appendLog(label + " exit code: " + result.getExitCode());
            if (!result.getStdout().isBlank()) {
                dialog.appendLog(label + " stdout:\n" + result.getStdout());
            }
            if (!result.getStderr().isBlank()) {
                dialog.appendLog(label + " stderr:\n" + result.getStderr());
            }
        }

        private String mergeOutput(ProcessResult result) {
            String stdout = result.getStdout();
            String stderr = result.getStderr();
            if (stdout.isBlank()) {
                return stderr;
            }
            if (stderr.isBlank()) {
                return stdout;
            }
            return stdout + System.lineSeparator() + stderr;
        }

        @Override
        protected void done() {
            boolean success = false;
            try {
                success = get();
            } catch (Exception ex) {
                failure = ex;
            }

            if (success) {
                dialog.appendLog("Task completed successfully.");
                dialog.markCompleted();
                
                // Open the output directory in file manager
                if (initialConfig.getProjectDirectory() != null) {
                    dialog.openOutputDirectory(
                        initialConfig.getProjectDirectory(), 
                        initialConfig.getProgrammingLanguage()
                    );
                }
                
                // Enable finetuning
                dialog.enableFinetuning(taskType, initialConfig, copilotCliService);
                
                // Show completion notification
                JOptionPane.showMessageDialog(frame,
                    "GitHub Copilot has finished processing your request.\n" +
                    "The task completed successfully.\n\n" +
                    "You can now use the Finetuning feature to make additional modifications.",
                    "Task Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Show algorithm result for CREATE_ALGORITHM tasks
                if (taskType == TaskType.CREATE_ALGORITHM) {
                    AlgorithmResultDialog algorithmDialog = new AlgorithmResultDialog(
                        frame, 
                        initialConfig.getProjectDirectory()
                    );
                    algorithmDialog.setVisible(true);
                }
            } else {
                if (failure != null) {
                    dialog.appendLog("Task failed: " + failure.getMessage());
                }
                dialog.markFailed();
                if (finalCompileErrors != null && !finalCompileErrors.isBlank()) {
                    showCompilationErrors(finalCompileErrors);
                }
                
                // Show failure notification
                JOptionPane.showMessageDialog(frame,
                    "GitHub Copilot has finished processing your request.\n" +
                    "However, the task encountered errors. Please check the log for details.",
                    "Task Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showCompilationErrors(String errors) {
            JFrame owner = frame;
            CompilationErrorDialog errorDialog = new CompilationErrorDialog(owner, errors);
            errorDialog.setVisible(true);
        }
    }
}
