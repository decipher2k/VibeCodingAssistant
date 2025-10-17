/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import javax.swing.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes a multi-phase build for an IDE project:
 * 1. Scaffold the project structure
 * 2. Generate each module in a subfolder
 * 3. Integrate modules and create tests
 */
public final class MultiPhaseBuildExecutor {
    private final IDEProject project;
    private final CopilotCliService copilotService;
    private final JFrame parentFrame;
    
    public MultiPhaseBuildExecutor(IDEProject project, CopilotCliService copilotService, JFrame parentFrame) {
        this.project = project;
        this.copilotService = copilotService;
        this.parentFrame = parentFrame;
    }
    
    /**
     * Executes the multi-phase build.
     */
    public void execute() {
        InitialConfig config = project.getInitialConfig();
        if (config == null) {
            JOptionPane.showMessageDialog(parentFrame,
                "Project configuration is missing.",
                "Configuration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<Module> modules = project.getAllModules();
        if (modules.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame,
                "Project has no modules to build.",
                "No Modules",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Execute in background
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Phase 1: Scaffolding project structure...");
                    executePhase1Scaffolding(config);
                    
                    publish("Phase 2: Generating modules...");
                    executePhase2Modules(config, modules);
                    
                    publish("Phase 3: Integrating and testing...");
                    executePhase3Integration(config, modules);
                    
                    publish("Build completed successfully!");
                } catch (Exception e) {
                    publish("Build failed: " + e.getMessage());
                    throw e;
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                // Show progress messages
                for (String message : chunks) {
                    System.out.println(message);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(parentFrame,
                        "Build completed successfully!",
                        "Build Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(parentFrame,
                        "Build failed: " + e.getMessage(),
                        "Build Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Phase 1: Scaffold the project structure.
     */
    private void executePhase1Scaffolding(InitialConfig config) {
        String prompt = buildScaffoldingPrompt(config);
        
        // Execute with copilot CLI using a generic task type
        try {
            ProcessResult result = copilotService.runPrimaryTask(
                TaskType.GENERATE_APP_OR_SCRIPT,
                prompt
            );
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Scaffolding failed: " + result.getStdout() + "\n" + result.getStderr());
            }
        } catch (Exception e) {
            throw new RuntimeException("Scaffolding execution failed", e);
        }
    }
    
    /**
     * Phase 2: Generate each module.
     */
    private void executePhase2Modules(InitialConfig config, List<Module> modules) {
        for (Module module : modules) {
            String prompt = buildModulePrompt(config, module);
            
            try {
                // Change working directory to module directory
                Path moduleDir = getModuleDirectory(config, module);
                CopilotCliService moduleService = new CopilotCliService(moduleDir);
                
                ProcessResult result = moduleService.runPrimaryTask(
                    module.getTaskType(),
                    prompt
                );
                
                if (!result.isSuccess()) {
                    throw new RuntimeException("Module generation failed for " + module.getName() + 
                        ": " + result.getStdout() + "\n" + result.getStderr());
                }
            } catch (Exception e) {
                throw new RuntimeException("Module generation execution failed for " + module.getName(), e);
            }
        }
    }
    
    /**
     * Phase 3: Integrate modules and create tests.
     */
    private void executePhase3Integration(InitialConfig config, List<Module> modules) {
        String prompt = buildIntegrationPrompt(config, modules);
        
        try {
            ProcessResult result = copilotService.runPrimaryTask(
                TaskType.MODIFY_EXISTING_SOFTWARE,
                prompt
            );
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Integration failed: " + result.getStdout() + "\n" + result.getStderr());
            }
        } catch (Exception e) {
            throw new RuntimeException("Integration execution failed", e);
        }
    }
    
    /**
     * Builds the scaffolding prompt.
     */
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
        
        return sb.toString();
    }
    
    /**
     * Builds the module generation prompt.
     */
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
        if (data != null) {
            appendModuleData(sb, module.getTaskType(), data);
        }
        
        sb.append("\n## Instructions\n");
        sb.append("1. Generate the module implementation in the current directory\n");
        sb.append("2. Create all necessary source files for this module\n");
        sb.append("3. Follow the project's coding standards and structure\n");
        sb.append("4. Ensure the module can be integrated with the main project\n");
        sb.append("5. DO NOT modify files outside this module directory\n");
        
        return sb.toString();
    }
    
    /**
     * Builds the integration prompt.
     */
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
        
        return sb.toString();
    }
    
    /**
     * Appends module-specific data to the prompt.
     */
    private void appendModuleData(StringBuilder sb, TaskType taskType, MainTaskData data) {
        switch (taskType) {
            case GENERATE_APP_OR_SCRIPT:
            case CREATE_MODULE:
                if (data.getProjectOverview() != null && !data.getProjectOverview().isEmpty()) {
                    sb.append("Overview: ").append(data.getProjectOverview()).append('\n');
                }
                if (data.getThemeDescription() != null && !data.getThemeDescription().isEmpty()) {
                    sb.append("Theme: ").append(data.getThemeDescription()).append('\n');
                }
                break;
            case FIX_CODING_ERRORS:
                if (data.getExpectedBehavior() != null && !data.getExpectedBehavior().isEmpty()) {
                    sb.append("Expected behavior: ").append(data.getExpectedBehavior()).append('\n');
                }
                if (data.getActualBehavior() != null && !data.getActualBehavior().isEmpty()) {
                    sb.append("Actual behavior: ").append(data.getActualBehavior()).append('\n');
                }
                break;
            case CREATE_ALGORITHM:
                if (data.getAlgorithmDescription() != null && !data.getAlgorithmDescription().isEmpty()) {
                    sb.append("Algorithm: ").append(data.getAlgorithmDescription()).append('\n');
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * Gets the directory for a module.
     */
    private Path getModuleDirectory(InitialConfig config, Module module) {
        Path baseDir = config.getProjectDirectory();
        if (baseDir == null) {
            baseDir = Path.of("").toAbsolutePath();
        }
        
        // Create path based on module hierarchy
        List<String> pathParts = new ArrayList<>();
        pathParts.add("src");
        pathParts.add("modules");
        
        // Add parent module names
        Module current = module;
        List<String> modulePath = new ArrayList<>();
        while (current != null) {
            modulePath.add(0, sanitizeModuleName(current.getName()));
            current = current.getParent();
        }
        pathParts.addAll(modulePath);
        
        Path result = baseDir;
        for (String part : pathParts) {
            result = result.resolve(part);
        }
        
        return result;
    }
    
    /**
     * Sanitizes a module name for use as a directory name.
     */
    private String sanitizeModuleName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }
}
