/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Controller for the IDE that manages the project state and coordinates
 * between the UI components.
 */
public final class IDEController {
    private final IDEMainFrame frame;
    private IDEProject currentProject;
    private Module selectedModule;
    private boolean isDirty;
    private Path currentProjectFile;
    private final CopilotCliService copilotCliService;
    private final TemplateManager templateManager;
    
    public IDEController() {
        this.currentProject = null;
        this.selectedModule = null;
        this.isDirty = false;
        this.currentProjectFile = null;
        this.copilotCliService = new CopilotCliService(Path.of("").toAbsolutePath());
        this.templateManager = new TemplateManager();
        this.frame = new IDEMainFrame(this);
    }
    
    public void show() {
        // First show the create project dialog without dirty check (fresh start)
        createNewProjectInternal(false);
        
        // Only show frame if a project was created
        if (currentProject != null) {
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
    
    public IDEProject getCurrentProject() {
        return currentProject;
    }
    
    public Module getSelectedModule() {
        return selectedModule;
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    public boolean isDirty() {
        return isDirty;
    }
    
    public void markDirty() {
        this.isDirty = true;
        frame.updateTitle();
    }
    
    private void markClean() {
        this.isDirty = false;
        frame.updateTitle();
    }
    
    public Path getCurrentProjectFile() {
        return currentProjectFile;
    }
    
    /**
     * Creates a new project by showing the initial configuration wizard.
     */
    public void createNewProject() {
        createNewProjectInternal(true);
    }
    
    /**
     * Internal method to create a new project.
     * 
     * @param checkDirty Whether to check if current project has unsaved changes
     */
    private void createNewProjectInternal(boolean checkDirty) {
        if (checkDirty && !confirmIfDirty("create a new project")) {
            return;
        }
        
        // Show the initial configuration dialog
        InitialConfigDialog dialog = new InitialConfigDialog(frame);
        dialog.setVisible(true);
        
        InitialConfig config = dialog.getResult();
        if (config != null) {
            // Check if Wizard mode was selected - if so, launch Wizard instead
            if (config.getIdeOrWizardMode() == ProjectMode.WIZARD) {
                JOptionPane.showMessageDialog(frame,
                    "Wizard mode selected. Launching Wizard mode...",
                    "Switching to Wizard Mode",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Hide the IDE frame
                frame.setVisible(false);
                frame.dispose();
                
                // Launch wizard controller with the config
                WizardController wizardController = new WizardController();
                wizardController.getFrame().setLocationRelativeTo(null);
                wizardController.getFrame().setVisible(true);
                wizardController.submitInitialConfig(config);
                return;
            }
            
            // Check if directory is not empty
            if (config.getProjectDirectory() != null) {
                Path projectDir = config.getProjectDirectory();
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
                                    JFileChooser fileChooser = new JFileChooser();
                                    fileChooser.setCurrentDirectory(projectDir.toFile());
                                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                                        "Vibe Coding Project (*.vcp)", "vcp"));
                                    
                                    int chooserResult = fileChooser.showOpenDialog(frame);
                                    if (chooserResult == JFileChooser.APPROVE_OPTION) {
                                        Path vcpFile = fileChooser.getSelectedFile().toPath();
                                        
                                        // Try to load the .vcp file
                                        if (java.nio.file.Files.exists(vcpFile) && vcpFile.toString().endsWith(".vcp")) {
                                            // Load the existing project instead
                                            IDEProject existingProject = ProjectSerializer.load(vcpFile);
                                            if (existingProject != null) {
                                                currentProject = existingProject;
                                                currentProjectFile = vcpFile;
                                                selectedModule = null;
                                                markClean();
                                                frame.refreshUI();
                                                return;
                                            } else {
                                                JOptionPane.showMessageDialog(frame,
                                                    "Failed to load the selected .vcp file.",
                                                    "Load Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                                return;
                                            }
                                        } else {
                                            JOptionPane.showMessageDialog(frame,
                                                "Invalid or non-existent .vcp file selected.\n" +
                                                "Project creation cancelled.",
                                                "Invalid File",
                                                JOptionPane.WARNING_MESSAGE);
                                            return;
                                        }
                                    } else {
                                        // User cancelled file selection
                                        JOptionPane.showMessageDialog(frame,
                                            "No .vcp file selected.\n" +
                                            "Project creation cancelled.",
                                            "No File Selected",
                                            JOptionPane.WARNING_MESSAGE);
                                        return;
                                    }
                                } else {
                                    // User chose not to select a .vcp file
                                    JOptionPane.showMessageDialog(frame,
                                        "Cannot proceed without a .vcp file for non-empty directory.\n" +
                                        "Project creation cancelled.",
                                        "Operation Cancelled",
                                        JOptionPane.WARNING_MESSAGE);
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
            
            // Create new project
            currentProject = new IDEProject(config);
            currentProjectFile = null;
            selectedModule = null;
            markClean();
            frame.refreshUI();
        }
    }
    
    /**
     * Saves the current project to a file.
     */
    public void saveProject() {
        if (currentProject == null) {
            return;
        }
        
        // Save current module data before saving the project
        frame.saveCurrentModuleData();
        
        if (currentProjectFile == null) {
            saveProjectAs();
        } else {
            ProjectSerializer.save(currentProject, currentProjectFile);
            markClean();
        }
    }
    
    /**
     * Saves the current project to a new file.
     */
    public void saveProjectAs() {
        if (currentProject == null) {
            return;
        }
        
        // Save current module data before saving the project
        frame.saveCurrentModuleData();
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Vibe Coding Project (*.vcp)", "vcp"));
        
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            Path path = fileChooser.getSelectedFile().toPath();
            if (!path.toString().endsWith(".vcp")) {
                path = Path.of(path.toString() + ".vcp");
            }
            
            currentProjectFile = path;
            ProjectSerializer.save(currentProject, currentProjectFile);
            markClean();
            frame.updateTitle();
        }
    }
    
    /**
     * Opens a project from a file.
     */
    public void openProject() {
        if (!confirmIfDirty("open another project")) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Vibe Coding Project (*.vcp)", "vcp"));
        
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            Path path = fileChooser.getSelectedFile().toPath();
            IDEProject project = ProjectSerializer.load(path);
            
            if (project != null) {
                // Prompt user to select the project directory
                Path projectDirectory = promptForProjectDirectory(project);
                if (projectDirectory == null) {
                    // User cancelled directory selection
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
                
                currentProject = project;
                currentProjectFile = path;
                selectedModule = null;
                markClean();
                frame.refreshUI();
            } else {
                JOptionPane.showMessageDialog(frame,
                    "Failed to load project file.",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Loads an existing project without showing a file chooser dialog.
     * Used when launching the app with a project file.
     * 
     * @param project The project to load
     * @param projectFile The file path of the project
     */
    public void loadExistingProject(IDEProject project, Path projectFile) {
        this.currentProject = project;
        this.currentProjectFile = projectFile;
        this.selectedModule = null;
        markClean();
        frame.refreshUI();
    }
    
    /**
     * Adds a new root module to the project.
     */
    public void addRootModule(String name) {
        if (currentProject == null) {
            System.err.println("ERROR: Cannot add module - currentProject is null");
            return;
        }
        
        if (name == null || name.trim().isEmpty()) {
            System.err.println("ERROR: Cannot add module - name is null or empty");
            return;
        }
        
        // Check if name is unique
        if (!currentProject.isModuleNameUnique(name.trim(), null)) {
            System.err.println("ERROR: Cannot add module - name '" + name.trim() + "' already exists");
            javax.swing.JOptionPane.showMessageDialog(frame,
                "A module with the name '" + name.trim() + "' already exists.\nPlease choose a different name.",
                "Duplicate Module Name",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Module module = new Module(name);
        currentProject.addRootModule(module);
        System.out.println("DEBUG: Added module '" + name + "' with ID: " + module.getId());
        System.out.println("DEBUG: Total root modules: " + currentProject.getRootModules().size());
        
        // Set as main module if this is the first module in the project
        if (currentProject.getMainModule() == null && currentProject.getRootModules().size() == 1) {
            currentProject.setMainModule(module);
            System.out.println("DEBUG: Set module '" + name + "' as main module");
        }
        
        markDirty();
        frame.refreshModuleTree();
        selectModule(module);
    }
    
    /**
     * Adds a submodule to a parent module.
     */
    public void addSubmodule(Module parent, String name) {
        if (currentProject == null || parent == null) {
            return;
        }
        
        // Check if name is unique
        if (!currentProject.isModuleNameUnique(name.trim(), null)) {
            System.err.println("ERROR: Cannot add submodule - name '" + name.trim() + "' already exists");
            javax.swing.JOptionPane.showMessageDialog(frame,
                "A module with the name '" + name.trim() + "' already exists.\nPlease choose a different name.",
                "Duplicate Module Name",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Module module = new Module(name);
        parent.addSubModule(module);
        markDirty();
        frame.refreshModuleTree();
        selectModule(module);
    }
    
    /**
     * Deletes a module from the project.
     */
    public void deleteModule(Module module) {
        if (currentProject == null || module == null) {
            return;
        }
        
        Module parent = module.getParent();
        if (parent != null) {
            parent.removeSubModule(module);
        } else {
            currentProject.removeRootModule(module);
        }
        
        if (selectedModule == module) {
            selectedModule = null;
            frame.clearModuleEditor();
        }
        
        markDirty();
        frame.refreshModuleTree();
    }
    
    /**
     * Renames a module.
     */
    public void renameModule(Module module, String newName) {
        if (module == null || newName == null) {
            return;
        }
        
        // Check if new name is unique (excluding the module being renamed)
        if (!currentProject.isModuleNameUnique(newName.trim(), module)) {
            System.err.println("ERROR: Cannot rename module - name '" + newName.trim() + "' already exists");
            javax.swing.JOptionPane.showMessageDialog(frame,
                "A module with the name '" + newName.trim() + "' already exists.\nPlease choose a different name.",
                "Duplicate Module Name",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        module.setName(newName);
        markDirty();
        frame.refreshModuleTree();
    }
    
    /**
     * Moves a module up in its parent's children list.
     */
    public void moveModuleUp(Module module) {
        if (module == null) {
            return;
        }
        
        List<Module> siblings;
        Module parent = module.getParent();
        
        if (parent != null) {
            siblings = parent.getSubModules();
        } else {
            siblings = currentProject.getRootModules();
        }
        
        int index = siblings.indexOf(module);
        if (index > 0) {
            if (parent != null) {
                parent.removeSubModule(module);
                parent.insertSubModule(index - 1, module);
            } else {
                currentProject.removeRootModule(module);
                currentProject.insertRootModule(index - 1, module);
            }
            markDirty();
            frame.refreshModuleTree();
            frame.selectModuleInTree(module);
        }
    }
    
    /**
     * Moves a module down in its parent's children list.
     */
    public void moveModuleDown(Module module) {
        if (module == null) {
            return;
        }
        
        List<Module> siblings;
        Module parent = module.getParent();
        
        if (parent != null) {
            siblings = parent.getSubModules();
        } else {
            siblings = currentProject.getRootModules();
        }
        
        int index = siblings.indexOf(module);
        if (index >= 0 && index < siblings.size() - 1) {
            if (parent != null) {
                parent.removeSubModule(module);
                parent.insertSubModule(index + 1, module);
            } else {
                currentProject.removeRootModule(module);
                currentProject.insertRootModule(index + 1, module);
            }
            markDirty();
            frame.refreshModuleTree();
            frame.selectModuleInTree(module);
        }
    }
    
    /**
     * Moves a module to be a root module.
     */
    public void moveModuleToRoot(Module module, int index) {
        if (module == null || currentProject == null) {
            return;
        }
        
        // Remove from current parent
        Module parent = module.getParent();
        if (parent != null) {
            parent.removeSubModule(module);
        } else {
            currentProject.removeRootModule(module);
        }
        
        // Add as root module
        if (index >= 0) {
            currentProject.insertRootModule(index, module);
        } else {
            currentProject.addRootModule(module);
        }
        
        markDirty();
        frame.refreshModuleTree();
        frame.selectModuleInTree(module);
    }
    
    /**
     * Moves a module to be a child of another module.
     */
    public void moveModuleToParent(Module module, Module newParent, int index) {
        if (module == null || newParent == null || currentProject == null) {
            return;
        }
        
        // Can't move to itself or a descendant
        if (module == newParent || module.isAncestorOf(newParent)) {
            return;
        }
        
        // Remove from current parent
        Module oldParent = module.getParent();
        if (oldParent != null) {
            oldParent.removeSubModule(module);
        } else {
            currentProject.removeRootModule(module);
        }
        
        // Add to new parent
        if (index >= 0) {
            newParent.insertSubModule(index, module);
        } else {
            newParent.addSubModule(module);
        }
        
        markDirty();
        frame.refreshModuleTree();
        frame.selectModuleInTree(module);
    }
    
    /**
     * Sets the main module for the project.
     */
    public void setMainModule(Module module) {
        if (currentProject == null) {
            return;
        }
        
        currentProject.setMainModule(module);
        markDirty();
        frame.refreshModuleTree();
    }
    
    /**
     * Selects a module and displays it in the editor.
     */
    public void selectModule(Module module) {
        // Save current module data before switching
        if (selectedModule != null) {
            frame.saveCurrentModuleData();
        }
        
        this.selectedModule = module;
        
        if (module != null) {
            frame.showModuleEditor(module);
        } else {
            frame.clearModuleEditor();
        }
    }
    
    /**
     * Updates the task data for the currently selected module.
     */
    public void updateModuleData(MainTaskData data) {
        if (selectedModule != null) {
            selectedModule.setTaskData(data);
            markDirty();
        }
    }
    
    /**
     * Saves a template for the selected module.
     */
    public void saveTemplate() {
        if (currentProject == null || selectedModule == null) {
            JOptionPane.showMessageDialog(frame,
                "Please select a module first.",
                "No Module Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        InitialConfig config = currentProject.getInitialConfig();
        if (config == null) {
            JOptionPane.showMessageDialog(frame,
                "Project configuration is missing.",
                "Configuration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Save current module data
        frame.saveCurrentModuleData();
        
        ProjectTemplate template = new ProjectTemplate(
            config.getProgrammingLanguage(),
            config.getProjectStyle(),
            config.getTargetOperatingSystems(),
            selectedModule.getTaskType(),
            selectedModule.getTaskData(),
            config.getProjectDirectory()
        );
        
        templateManager.saveTemplate(frame, template);
    }
    
    /**
     * Loads a template into the selected module.
     */
    public void loadTemplate() {
        if (currentProject == null || selectedModule == null) {
            JOptionPane.showMessageDialog(frame,
                "Please select a module first.",
                "No Module Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        InitialConfig config = currentProject.getInitialConfig();
        if (config == null) {
            JOptionPane.showMessageDialog(frame,
                "Project configuration is missing.",
                "Configuration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(frame,
            "Loading a template will replace the current module data.\n" +
            "Do you want to continue?",
            "Confirm Load Template",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        ProjectTemplate template = templateManager.loadTemplate(frame);
        if (template != null) {
            selectedModule.setTaskType(template.getTaskType());
            selectedModule.setTaskData(template.getTaskData());
            markDirty();
            frame.showModuleEditor(selectedModule);
        }
    }
    
    /**
     * Opens the project settings dialog.
     */
    public void openProjectSettings() {
        if (currentProject == null) {
            JOptionPane.showMessageDialog(frame,
                "No project is open.",
                "No Project",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        ProjectSettingsDialog dialog = new ProjectSettingsDialog(frame, this);
        dialog.setVisible(true);
    }
    
    /**
     * Performs the build task for the entire project.
     */
    public void performBuild() {
        if (currentProject == null) {
            JOptionPane.showMessageDialog(frame,
                "No project is open.",
                "No Project",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        InitialConfig config = currentProject.getInitialConfig();
        if (config == null) {
            JOptionPane.showMessageDialog(frame,
                "Project configuration is missing.",
                "Configuration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Save current module data
        frame.saveCurrentModuleData();
        
        // Show TaskExecutionDialog
        TaskExecutionDialog dialog = new TaskExecutionDialog(frame);
        BuildExecutionWorker worker = new BuildExecutionWorker(dialog, currentProject, copilotCliService);
        worker.execute();
        dialog.setVisible(true);
    }
    
    /**
     * Handles application exit.
     */
    public void handleExit() {
        if (!confirmIfDirty("exit")) {
            return;
        }
        System.exit(0);
    }
    
    /**
     * Confirms with user if there are unsaved changes.
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
     * Prompts the user to select the project directory for a loaded VCP file.
     * Shows the current directory from the project as a suggestion.
     * 
     * @param project The loaded project
     * @return The selected project directory, or null if cancelled
     */
    private Path promptForProjectDirectory(IDEProject project) {
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
        
        int result = JOptionPane.showConfirmDialog(frame,
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
        
        int chooserResult = fileChooser.showOpenDialog(frame);
        if (chooserResult == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().toPath();
        }
        
        return null;
    }
    
    /**
     * Worker class for executing multi-phase project builds.
     */
    private static final class BuildExecutionWorker extends SwingWorker<Boolean, String> {
        private final TaskExecutionDialog dialog;
        private final IDEProject project;
        private final CopilotCliService copilotService;
        private Exception failure;
        
        BuildExecutionWorker(TaskExecutionDialog dialog, IDEProject project, CopilotCliService copilotService) {
            this.dialog = dialog;
            this.project = project;
            this.copilotService = copilotService;
        }
        
        @Override
        protected Boolean doInBackground() {
            try {
                InitialConfig config = project.getInitialConfig();
                List<Module> modules = project.getAllModules();
                
                if (modules.isEmpty()) {
                    dialog.appendLog("No modules to build.");
                    return false;
                }
                
                // Ensure project directory exists
                Path projectDir = config.getProjectDirectory();
                if (projectDir == null) {
                    projectDir = Path.of("").toAbsolutePath();
                }
                
                dialog.appendLog("=== Combined Single-Run Build Started ===");
                dialog.appendLog("Project: " + projectDir.toAbsolutePath());
                dialog.appendLog("Modules: " + modules.size());
                dialog.appendLog("Approach: All phases (scaffolding, modules, build, test, run) in ONE agent run");
                dialog.appendLog("");
                
                // Create project directory if it doesn't exist
                if (!java.nio.file.Files.exists(projectDir)) {
                    dialog.appendLog("Creating project directory: " + projectDir.toAbsolutePath());
                    java.nio.file.Files.createDirectories(projectDir);
                } else {
                    dialog.appendLog("Project directory exists: " + projectDir.toAbsolutePath());
                }
                
                // Build the combined prompt that includes all phases
                dialog.setStatus("Generating combined prompt for all phases...");
                dialog.appendLog("Building combined prompt (scaffolding + all modules + build/test/run)...");
                String combinedPrompt = PromptBuilder.buildCombinedPrompt(config, modules, project.getProjectSettings());
                dialog.appendLog("Combined prompt generated successfully.");
                dialog.appendLog("Prompt length: " + combinedPrompt.length() + " characters");
                dialog.appendLog("");
                
                // Save the prompt to a file in the project directory
                dialog.setStatus("Saving prompt to file...");
                java.nio.file.Path promptFile = projectDir.resolve("prompt.txt");
                try {
                    java.nio.file.Files.writeString(promptFile, combinedPrompt, java.nio.charset.StandardCharsets.UTF_8);
                    dialog.appendLog("Prompt saved to: " + promptFile.toAbsolutePath());
                } catch (java.io.IOException ex) {
                    dialog.appendLog("Failed to save prompt to file: " + ex.getMessage());
                    throw ex;
                }
                
                // Use a simplified prompt that instructs Copilot to read from the file
                String fileReadPrompt = "Process the instructions in prompt.txt and execute them.";
                
                // Execute the combined prompt in a SINGLE Copilot CLI run
                dialog.setStatus("Executing combined build (all phases in one run)...");
                dialog.appendLog("=== Starting Single Combined Agent Run ===");
                dialog.appendLog("This will complete:");
                dialog.appendLog("  - Phase 1: Project scaffolding");
                dialog.appendLog("  - Phase 2: All " + modules.size() + " module implementations");
                dialog.appendLog("  - Phase 3: Build, test, and run");
                dialog.appendLog("");
                dialog.appendLog("Running Copilot CLI with file-based prompt...");
                
                CopilotCliService combinedService = new CopilotCliService(projectDir);
                ProcessResult result = combinedService.runPrimaryTask(
                    TaskType.GENERATE_APP_OR_SCRIPT,
                    fileReadPrompt,
                    line -> dialog.appendLog(line)
                );
                
                dialog.appendLog("");
                dialog.appendLog("=== Copilot CLI Execution Completed ===");
                dialog.appendLog("Exit code: " + result.getExitCode());
                
                if (!result.getStdout().isBlank()) {
                    dialog.appendLog("\nStandard Output:\n" + result.getStdout());
                }
                if (!result.getStderr().isBlank()) {
                    dialog.appendLog("\nStandard Error:\n" + result.getStderr());
                }
                
                if (!result.isSuccess()) {
                    dialog.appendLog("\n❌ Combined build failed!");
                    dialog.appendLog("The agent was unable to complete all phases successfully.");
                    return false;
                }
                
                dialog.appendLog("\n✓ Combined Build Completed Successfully!");
                dialog.appendLog("All phases (scaffolding, modules, build, test, run) completed in a single run.");
                return true;
                
            } catch (Exception ex) {
                failure = ex;
                dialog.appendLog("Build failed with exception: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }
        
        @Override
        protected void done() {
            try {
                Boolean success = get();
                if (success != null && success) {
                    dialog.setStatus("Build completed successfully");
                    dialog.markCompleted();
                    
                    // Open the output directory in file manager
                    InitialConfig config = project.getInitialConfig();
                    if (config != null && config.getProjectDirectory() != null) {
                        dialog.openOutputDirectory(
                            config.getProjectDirectory(),
                            config.getProgrammingLanguage()
                        );
                    }
                } else {
                    dialog.setStatus("Build failed");
                    dialog.markFailed();
                }
            } catch (Exception e) {
                dialog.setStatus("Build failed with error");
                dialog.markFailed();
                dialog.appendLog("Error in build completion: " + e.getMessage());
            }
        }
        
        private boolean executePhase1Scaffolding(InitialConfig config) {
            dialog.appendLog("Scaffolding project structure...");
            
            // Determine the project directory
            Path projectDir = config.getProjectDirectory();
            if (projectDir == null) {
                projectDir = Path.of("").toAbsolutePath();
            }
            
            try {
                // Create project directory if it doesn't exist
                if (!java.nio.file.Files.exists(projectDir)) {
                    dialog.appendLog("Creating project directory: " + projectDir.toAbsolutePath());
                    java.nio.file.Files.createDirectories(projectDir);
                } else {
                    dialog.appendLog("Project directory exists: " + projectDir.toAbsolutePath());
                }
                
                // Create a CopilotCliService instance for the project directory
                dialog.appendLog("Working directory: " + projectDir.toAbsolutePath());
                CopilotCliService scaffoldingService = new CopilotCliService(projectDir);
                
                String prompt = buildScaffoldingPrompt(config);
                dialog.appendLog("Scaffolding prompt generated.");
                
                dialog.appendLog("Running Copilot CLI for scaffolding...");
                ProcessResult result = scaffoldingService.runPrimaryTask(
                    TaskType.GENERATE_APP_OR_SCRIPT,
                    prompt,
                    line -> dialog.appendLog(line)
                );
                
                dialog.appendLog("Scaffolding completed with exit code: " + result.getExitCode());
                
                if (!result.getStdout().isBlank()) {
                    dialog.appendLog("Output:\n" + result.getStdout());
                }
                if (!result.getStderr().isBlank()) {
                    dialog.appendLog("Errors:\n" + result.getStderr());
                }
                
                if (!result.isSuccess()) {
                    dialog.appendLog("Scaffolding failed!");
                    return false;
                }
                
                dialog.appendLog("Scaffolding phase completed successfully.");
                return true;
                
            } catch (Exception e) {
                dialog.appendLog("Scaffolding error: " + e.getMessage());
                return false;
            }
        }
        
        private boolean executePhase2Modules(InitialConfig config, List<Module> modules) {
            dialog.appendLog("Generating " + modules.size() + " module(s)...");
            
            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);
                dialog.appendLog("\n--- Module " + (i + 1) + "/" + modules.size() + ": " + module.getName() + " ---");
                
                if (!generateModule(config, module)) {
                    return false;
                }
            }
            
            dialog.appendLog("\nAll modules generated successfully.");
            return true;
        }
        
        private boolean generateModule(InitialConfig config, Module module) {
            dialog.appendLog("Generating module: " + module.getName());
            dialog.appendLog("Task type: " + module.getTaskType());
            
            // Determine the module directory
            Path projectDir = config.getProjectDirectory();
            if (projectDir == null) {
                projectDir = Path.of("").toAbsolutePath();
            }
            
            // Create module directory structure: projectDir/modules/ModuleName
            Path modulesDir = projectDir.resolve("modules");
            Path moduleDir = modulesDir.resolve(module.getName());
            
            try {
                // Create module directory if it doesn't exist
                if (!java.nio.file.Files.exists(moduleDir)) {
                    dialog.appendLog("Creating module directory: " + moduleDir.toAbsolutePath());
                    java.nio.file.Files.createDirectories(moduleDir);
                } else {
                    dialog.appendLog("Module directory exists: " + moduleDir.toAbsolutePath());
                }
                
                // Create a separate CopilotCliService instance for this module
                dialog.appendLog("Working directory: " + moduleDir.toAbsolutePath());
                CopilotCliService moduleService = new CopilotCliService(moduleDir);
                
                String prompt = buildModulePrompt(config, module);
                dialog.appendLog("Module prompt generated.");
                
                dialog.appendLog("Running Copilot CLI for module...");
                ProcessResult result = moduleService.runPrimaryTask(
                    module.getTaskType(),
                    prompt,
                    line -> dialog.appendLog(line)
                );
                
                dialog.appendLog("Module generation completed with exit code: " + result.getExitCode());
                
                if (!result.getStdout().isBlank()) {
                    dialog.appendLog("Output:\n" + result.getStdout());
                }
                if (!result.getStderr().isBlank()) {
                    dialog.appendLog("Errors:\n" + result.getStderr());
                }
                
                if (!result.isSuccess()) {
                    dialog.appendLog("Module generation failed!");
                    return false;
                }
                
                dialog.appendLog("Module '" + module.getName() + "' generated successfully.");
                return true;
                
            } catch (Exception e) {
                dialog.appendLog("Module generation error: " + e.getMessage());
                return false;
            }
        }
        
        private boolean executePhase3Integration(InitialConfig config, List<Module> modules) {
            dialog.appendLog("Integrating modules and finalizing project...");
            
            // Determine the project directory
            Path projectDir = config.getProjectDirectory();
            if (projectDir == null) {
                projectDir = Path.of("").toAbsolutePath();
            }
            
            dialog.appendLog("Working directory: " + projectDir.toAbsolutePath());
            CopilotCliService integrationService = new CopilotCliService(projectDir);
            
            String prompt = buildIntegrationPrompt(config, modules);
            dialog.appendLog("Integration prompt generated.");
            
            try {
                dialog.appendLog("Running Copilot CLI for integration...");
                ProcessResult result = integrationService.runPrimaryTask(
                    TaskType.MODIFY_EXISTING_SOFTWARE,
                    prompt,
                    line -> dialog.appendLog(line)
                );
                
                dialog.appendLog("Integration completed with exit code: " + result.getExitCode());
                
                if (!result.getStdout().isBlank()) {
                    dialog.appendLog("Output:\n" + result.getStdout());
                }
                if (!result.getStderr().isBlank()) {
                    dialog.appendLog("Errors:\n" + result.getStderr());
                }
                
                if (!result.isSuccess()) {
                    dialog.appendLog("Integration failed!");
                    return false;
                }
                
                dialog.appendLog("Integration phase completed successfully.");
                return true;
                
            } catch (Exception e) {
                dialog.appendLog("Integration error: " + e.getMessage());
                return false;
            }
        }
        
        private String buildScaffoldingPrompt(InitialConfig config) {
            StringBuilder sb = new StringBuilder();
            
            sb.append("You are GitHub Copilot CLI acting as an expert software engineer.\n");
            sb.append("Your job is to scaffold a project structure for a modular application.\n\n");
            
            sb.append("## CRITICAL RULES\n");
            sb.append("- DO NOT create any descriptive or status markdown files (e.g., README.md, STATUS.md, CHANGES.md, TODO.md, etc.).\n");
            sb.append("- Focus ONLY on creating functional source code, configuration files, and test files.\n");
            sb.append("- If documentation is needed, include it as code comments, not separate markdown files.\n\n");
            
            sb.append("## Project Context\n");
            sb.append("- Programming language: ").append(config.getProgrammingLanguage()).append('\n');
            sb.append("- Project style: ").append(config.getProjectStyle()).append('\n');
            sb.append("- Target operating systems: ")
                .append(String.join(", ", config.getTargetOperatingSystems().stream()
                    .map(Object::toString).toList())).append("\n");
            
            if (config.getProjectDirectory() != null) {
                sb.append("- Project directory: ").append(config.getProjectDirectory().toAbsolutePath()).append("\n");
            }
            
            if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                sb.append("- IMPORTANT: Use .NET 9.0 (net9.0) as the target framework.\n");
            }
            
            sb.append("\n## Instructions\n");
            sb.append("1. Create the basic project structure and build configuration files\n");
            sb.append("2. Set up the main entry point\n");
            sb.append("3. Create a 'modules' or 'src/modules' directory for module implementations\n");
            sb.append("4. DO NOT implement any modules yet - just create the scaffolding\n");
            sb.append("5. Set up dependency management and build system\n");
            sb.append("6. Create a README with project structure documentation\n");
            
            sb.append("\n## Important Notes\n");
            sb.append("- DO NOT install any system packages using package managers (apt, dnf, pacman, brew, etc.)\n");
            sb.append("- DO NOT use sudo or any commands requiring root/administrator privileges\n");
            sb.append("- You may use language-specific package managers (npm, pip, cargo, etc.) for project dependencies\n");
            
            // Add build and run instructions with Wine support
            sb.append("\n## Build and Run Instructions\n");
            
            if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                String currentOs = System.getProperty("os.name").toLowerCase();
                boolean isWindows = currentOs.contains("win");
                boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                
                sb.append("- IMPORTANT: When building C# projects, ALWAYS change to the project directory before running dotnet commands.\n");
                sb.append("- If the project is in a subfolder, use 'cd <ProjectFolder>' first, then run 'dotnet build <ProjectName>.csproj'.\n");
                sb.append("- ALWAYS specify the .csproj file explicitly in dotnet commands to avoid ambiguity.\n");
                sb.append("- Use 'dotnet build <ProjectName>.csproj' instead of just 'dotnet build'.\n");
                sb.append("- Similarly, use 'dotnet run --project <ProjectName>.csproj' to run the project.\n");
                
                if (!isWindows && targetIncludesWindows) {
                    sb.append("- IMPORTANT: This is a .NET project targeting Windows on a non-Windows system.\n");
                    sb.append("- ALL .NET commands MUST be prefixed with 'wine':\n");
                    sb.append("  * Build: WINEDEBUG=-all wine dotnet build <ProjectName>.csproj\n");
                    sb.append("  * Run: WINEDEBUG=-all wine dotnet run --project <ProjectName>.csproj\n");
                    sb.append("  * Restore: WINEDEBUG=-all wine dotnet restore <ProjectName>.csproj\n");
                    sb.append("  * Any other dotnet command: WINEDEBUG=-all wine dotnet <command> <ProjectName>.csproj\n");
                }
            }
            
            sb.append("- After creating the scaffolding, build it to ensure compilation succeeds.\n");
            sb.append("- Execute the build command to verify the project structure is correct.\n");
            
            return sb.toString();
        }
        
        private String buildModulePrompt(InitialConfig config, Module module) {
            StringBuilder sb = new StringBuilder();
            
            sb.append("You are GitHub Copilot CLI acting as an expert software engineer.\n");
            sb.append("Your job is to generate a module for an existing project.\n\n");
            
            sb.append("## CRITICAL RULES\n");
            sb.append("- DO NOT create any descriptive or status markdown files (e.g., README.md, STATUS.md, CHANGES.md, TODO.md, etc.).\n");
            sb.append("- Focus ONLY on creating functional source code, configuration files, and test files.\n");
            sb.append("- If documentation is needed, include it as code comments, not separate markdown files.\n\n");
            
            sb.append("## Project Context\n");
            sb.append("- Programming language: ").append(config.getProgrammingLanguage()).append('\n');
            sb.append("- Project style: ").append(config.getProjectStyle()).append('\n');
            sb.append("- Module name: ").append(module.getName()).append('\n');
            sb.append("- Task type: ").append(module.getTaskType()).append('\n');
            
            if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                sb.append("- IMPORTANT: Use .NET 9.0 (net9.0) as the target framework.\n");
            }
            
            // Add project-wide variables if available
            if (project != null && project.getProjectSettings() != null) {
                ProjectSettings settings = project.getProjectSettings();
                if (settings.getGlobalVariables() != null && !settings.getGlobalVariables().isEmpty()) {
                    sb.append("\n## Global Variables (Project-Wide)\n");
                    sb.append("The following global variables are defined for this project.\n");
                    sb.append("Reference these variables using the format [VARIABLENAME].\n\n");
                    for (GlobalVariable var : settings.getGlobalVariables()) {
                        sb.append("- **[").append(var.getName()).append("]**: ");
                        if (var.getValue() != null && !var.getValue().isBlank()) {
                            sb.append(var.getValue());
                        } else {
                            sb.append("(no value)");
                        }
                        sb.append("\n");
                    }
                }
            }
            
            // Add module-specific variables
            if (module.getModuleVariables() != null && !module.getModuleVariables().isEmpty()) {
                sb.append("\n## Module Variables\n");
                sb.append("The following module-specific variables are defined for this module.\n");
                sb.append("Reference these variables using the format [VARIABLENAME].\n\n");
                for (ModuleVariable var : module.getModuleVariables()) {
                    sb.append("- **[").append(var.getName()).append("]**: ");
                    if (var.getValue() != null && !var.getValue().isBlank()) {
                        sb.append(var.getValue());
                    } else {
                        sb.append("(no value)");
                    }
                    sb.append("\n");
                }
            }
            
            sb.append("\n## Module Requirements\n");
            
            // Add module-specific data
            MainTaskData data = module.getTaskData();
            if (data != null && data.getProjectOverview() != null && !data.getProjectOverview().isEmpty()) {
                sb.append("Overview: ").append(data.getProjectOverview()).append('\n');
            }
            
            sb.append("\n## Instructions\n");
            sb.append("1. Generate the module implementation in the current directory\n");
            sb.append("2. Create all necessary source files for this module\n");
            sb.append("3. Follow the project's coding standards and structure\n");
            sb.append("4. Ensure the module can be integrated with the main project\n");
            sb.append("5. DO NOT modify files outside this module directory\n");
            
            // Add build and run instructions with Wine support
            sb.append("\n## Build and Run Instructions\n");
            
            if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                String currentOs = System.getProperty("os.name").toLowerCase();
                boolean isWindows = currentOs.contains("win");
                boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                
                sb.append("- IMPORTANT: When building C# projects, ALWAYS change to the project directory before running dotnet commands.\n");
                sb.append("- If the project is in a subfolder, use 'cd <ProjectFolder>' first, then run 'dotnet build <ProjectName>.csproj'.\n");
                sb.append("- ALWAYS specify the .csproj file explicitly in dotnet commands to avoid ambiguity.\n");
                sb.append("- Use 'dotnet build <ProjectName>.csproj' instead of just 'dotnet build'.\n");
                sb.append("- Similarly, use 'dotnet run --project <ProjectName>.csproj' to run the project.\n");
                
                if (!isWindows && targetIncludesWindows) {
                    sb.append("- IMPORTANT: This is a .NET project targeting Windows on a non-Windows system.\n");
                    sb.append("- ALL .NET commands MUST be prefixed with 'wine':\n");
                    sb.append("  * Build: WINEDEBUG=-all wine dotnet build <ProjectName>.csproj\n");
                    sb.append("  * Run: WINEDEBUG=-all wine dotnet run --project <ProjectName>.csproj\n");
                    sb.append("  * Restore: WINEDEBUG=-all wine dotnet restore <ProjectName>.csproj\n");
                    sb.append("  * Any other dotnet command: WINEDEBUG=-all wine dotnet <command> <ProjectName>.csproj\n");
                }
            }
            
            sb.append("- After creating the module, build it to ensure compilation succeeds.\n");
            sb.append("- Execute the build command to verify the module compiles correctly.\n");
            sb.append("- Run all tests to verify the module's functionality.\n");
            
            return sb.toString();
        }
        
        private String buildIntegrationPrompt(InitialConfig config, List<Module> modules) {
            StringBuilder sb = new StringBuilder();
            
            sb.append("You are GitHub Copilot CLI acting as an expert software engineer.\n");
            sb.append("Your job is to integrate all modules and finalize the project.\n\n");
            
            sb.append("## CRITICAL RULES\n");
            sb.append("- DO NOT create any descriptive or status markdown files (e.g., README.md, STATUS.md, CHANGES.md, TODO.md, etc.).\n");
            sb.append("- Focus ONLY on creating functional source code, configuration files, and test files.\n");
            sb.append("- If documentation is needed, include it as code comments, not separate markdown files.\n\n");
            
            sb.append("## Project Context\n");
            sb.append("- Programming language: ").append(config.getProgrammingLanguage()).append('\n');
            sb.append("- Project style: ").append(config.getProjectStyle()).append('\n');
            
            Module mainModule = project.getMainModule();
            if (mainModule != null) {
                sb.append("- Main module: ").append(mainModule.getName()).append('\n');
            }
            
            sb.append("\n## Modules to Integrate\n");
            for (Module module : modules) {
                sb.append("- ").append(module.getName());
                if (module.equals(mainModule)) {
                    sb.append(" (MAIN)");
                }
                sb.append('\n');
            }
            
            sb.append("\n## Instructions\n");
            sb.append("1. Integrate all modules into the main project\n");
            sb.append("2. Wire up the main entry point to load the main module on startup\n");
            sb.append("3. Create comprehensive test cases for the integrated system\n");
            sb.append("4. Run all tests and fix any failures\n");
            sb.append("5. Compile/build the entire project\n");
            sb.append("6. Fix any compilation or build errors\n");
            sb.append("7. Generate final documentation\n");
            sb.append("8. Ensure the application is ready to run\n");
            
            // Add build and run instructions with Wine support
            sb.append("\n## Build and Run Instructions\n");
            
            if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                String currentOs = System.getProperty("os.name").toLowerCase();
                boolean isWindows = currentOs.contains("win");
                boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                
                sb.append("- IMPORTANT: When building C# projects, ALWAYS change to the project directory before running dotnet commands.\n");
                sb.append("- If the project is in a subfolder, use 'cd <ProjectFolder>' first, then run 'dotnet build <ProjectName>.csproj'.\n");
                sb.append("- ALWAYS specify the .csproj file explicitly in dotnet commands to avoid ambiguity.\n");
                sb.append("- Use 'dotnet build <ProjectName>.csproj' instead of just 'dotnet build'.\n");
                sb.append("- Similarly, use 'dotnet run --project <ProjectName>.csproj' to run the project.\n");
                
                if (!isWindows && targetIncludesWindows) {
                    sb.append("- IMPORTANT: This is a .NET project targeting Windows on a non-Windows system.\n");
                    sb.append("- ALL .NET commands MUST be prefixed with 'wine':\n");
                    sb.append("  * Build: WINEDEBUG=-all wine dotnet build <ProjectName>.csproj\n");
                    sb.append("  * Run: WINEDEBUG=-all wine dotnet run --project <ProjectName>.csproj\n");
                    sb.append("  * Restore: WINEDEBUG=-all wine dotnet restore <ProjectName>.csproj\n");
                    sb.append("  * Test: WINEDEBUG=-all wine dotnet test <ProjectName>.csproj\n");
                    sb.append("  * Any other dotnet command: WINEDEBUG=-all wine dotnet <command> <ProjectName>.csproj\n");
                }
            }
            
            sb.append("- After integration, build the entire project to ensure everything compiles.\n");
            sb.append("- Run all tests (unit tests and integration tests) to verify the integrated system.\n");
            sb.append("- Fix any compilation or test failures.\n");
            sb.append("- Once all tests pass, run the application to verify it works correctly.\n");
            
            return sb.toString();
        }
    }
}
