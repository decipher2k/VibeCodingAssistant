/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class PromptBuilder {
    private PromptBuilder() {
    }
    
    /**
     * Checks if a directory is empty (has no files or subdirectories).
     * Returns true if directory is empty or doesn't exist, false if it contains any files.
     */
    private static boolean isDirectoryEmpty(Path directory) {
        if (directory == null) {
            return true;
        }
        
        try {
            if (!java.nio.file.Files.exists(directory)) {
                return true;
            }
            
            try (java.util.stream.Stream<java.nio.file.Path> entries = java.nio.file.Files.list(directory)) {
                return !entries.findAny().isPresent();
            }
        } catch (java.io.IOException e) {
            // If we can't check, assume empty
            return true;
        }
    }
    
    /**
     * Validates directory state against task requirements.
     * Returns an error message if validation fails, null if validation passes.
     */
    private static String validateDirectoryForTask(TaskType taskType, Path directory) {
        boolean isEmpty = isDirectoryEmpty(directory);
        
        switch (taskType) {
            case FIX_CODING_ERRORS:
            case CREATE_MODULE:
            case MODIFY_EXISTING_SOFTWARE:
                // These tasks require a non-empty directory
                if (isEmpty) {
                    return "ERROR: The project directory is empty. Task '" + taskType + 
                           "' requires an existing codebase to work with.\n" +
                           "Please select a directory with existing source code or choose a different task type.";
                }
                break;
                
            case CREATE_ALGORITHM:
                // This task requires an empty directory
                if (!isEmpty) {
                    return "ERROR: The project directory is not empty. Task '" + taskType + 
                           "' requires an empty directory to create a standalone algorithm.\n" +
                           "Please select an empty directory or clear the current directory.";
                }
                break;
                
            case GENERATE_APP_OR_SCRIPT:
                // This task can work with either empty or non-empty directories
                // No validation needed
                break;
                
            default:
                // No validation for other tasks
                break;
        }
        
        return null; // Validation passed
    }

    public static String buildPrimaryPrompt(TaskType taskType, InitialConfig config, MainTaskData data) {
        return buildPrimaryPrompt(taskType, config, data, null);
    }

    public static String buildPrimaryPrompt(TaskType taskType, InitialConfig config, MainTaskData data, ProjectSettings projectSettings) {
        // Validate directory state for the task type
        String validationError = validateDirectoryForTask(taskType, config.getProjectDirectory());
        if (validationError != null) {
            // Return error message instead of building prompt
            return validationError;
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append("You are GitHub Copilot CLI acting as an expert software engineer.\n");
        builder.append("Your job is to perform the following task: ").append(taskType.toString()).append(".\n\n");
        
        builder.append("## CRITICAL RULES\n");
        builder.append("- DO NOT create any descriptive or status markdown files (e.g., README.md, STATUS.md, CHANGES.md, TODO.md, etc.).\n");
        builder.append("- Focus ONLY on creating functional source code, configuration files, and test files.\n");
        builder.append("- If documentation is needed, include it as code comments, not separate markdown files.\n\n");

        builder.append("## Project Context\n");
        builder.append("- Programming language: ").append(config.getProgrammingLanguage()).append('\n');
        builder.append("- Project style: ").append(config.getProjectStyle()).append('\n');
        
        // Add project name if available
        if (config.getProjectName() != null && !config.getProjectName().trim().isEmpty()) {
            builder.append("- Project name: ").append(config.getProjectName()).append('\n');
        }
        
        // Add program mode for GUI applications
        if (config.getProjectStyle() == ProjectStyle.GUI && config.getProgramMode() != null) {
            builder.append("- Program mode: ").append(config.getProgramMode()).append('\n');
            if (config.getProgramMode() == ProgramMode.MDI) {
                builder.append("  * **MDI Mode Requirements:**\n");
                builder.append("    ========================================\n");
                builder.append("    CRITICAL: DO NOT SHOW THE MAIN WINDOW AUTOMATICALLY ON STARTUP!\n");
                builder.append("    ========================================\n");
                builder.append("    - The application should start with an MDI (Multiple Document Interface) parent/container window.\n");
                builder.append("    - The MDI parent window should show ONLY:\n");
                builder.append("      * A menu bar at the top\n");
                builder.append("      * An empty workspace/client area (NO child windows initially)\n");
                builder.append("    - DO NOT automatically open, show, or load the main window on startup\n");
                builder.append("    - DO NOT automatically open, show, or load ANY window on startup\n");
                builder.append("\n");
                builder.append("    **Menu Structure (CRITICAL - Follow This Hierarchy Exactly):**\n");
                builder.append("    - Create a main menu bar in the MDI parent window.\n");
                builder.append("    - ONLY top-level modules (modules without a parent) should appear as top-level menu entries.\n");
                builder.append("    - For each TOP-LEVEL module:\n");
                builder.append("      * Create a top-level menu with the module name\n");
                builder.append("      * Add menu items for each of the module's dialogs/windows\n");
                builder.append("      * If the module has submodules, add them as SUBMENUS (not top-level menus):\n");
                builder.append("        - Each submodule appears as a submenu under its parent module's menu\n");
                builder.append("        - The submenu contains menu items for the submodule's dialogs/windows\n");
                builder.append("        - If a submodule has its own submodules, nest them further as sub-submenus\n");
                builder.append("    - IMPORTANT: Submodules must NOT appear as top-level menus - they must be nested under their parent\n");
                builder.append("    - Example hierarchy:\n");
                builder.append("      Menu Bar:\n");
                builder.append("        - Module A (top-level menu)\n");
                builder.append("          ├─ Window A1 (menu item - opens window)\n");
                builder.append("          ├─ Window A2 (menu item - opens window)\n");
                builder.append("          └─ Submodule A-1 (submenu, NOT a top-level menu)\n");
                builder.append("              ├─ Window A1-1 (menu item - opens window)\n");
                builder.append("              └─ Window A1-2 (menu item - opens window)\n");
                builder.append("        - Module B (top-level menu)\n");
                builder.append("          ├─ Window B1 (menu item - opens window)\n");
                builder.append("          └─ Submodule B-1 (submenu, NOT a top-level menu)\n");
                builder.append("              └─ Window B1-1 (menu item - opens window)\n");
                builder.append("\n");
                builder.append("    - Clicking a menu item (not submenu) should open the corresponding window inside the MDI parent.\n");
                builder.append("    - Multiple child windows can be open simultaneously within the MDI container.\n");
                builder.append("    - Each window should be independently closable, movable, and resizable within the MDI parent.\n");
                builder.append("    - The application startup behavior: Show MDI parent with menu, nothing else!\n");
                builder.append("    - Users launch windows via the menu system as needed.\n");
                builder.append("\n");
                builder.append("    **MDI Technical Implementation (CRITICAL):**\n");
                
                // Add language-specific MDI instructions
                switch (config.getProgrammingLanguage()) {
                    case JAVA:
                        builder.append("    - For Java/Swing: Use JDesktopPane as the MDI container in the main JFrame.\n");
                        builder.append("    - Each dialog/window must be implemented as a JInternalFrame (not JFrame or JDialog).\n");
                        builder.append("    - CRITICAL: When opening a child window from a menu, follow this exact sequence:\n");
                        builder.append("      1. Create the JInternalFrame instance\n");
                        builder.append("      2. Add it to the JDesktopPane: desktopPane.add(internalFrame)\n");
                        builder.append("      3. Make it visible: internalFrame.setVisible(true)\n");
                        builder.append("      4. Move to front: internalFrame.toFront()\n");
                        builder.append("      5. Request focus: try { internalFrame.setSelected(true); } catch (PropertyVetoException e) {}\n");
                        builder.append("    - This ensures child windows appear INSIDE of the MDI parent, not behind it.\n");
                        builder.append("    - DO NOT create standalone JFrame windows for child windows in MDI mode.\n");
                        builder.append("    - Example structure: JFrame (MDI parent) → JMenuBar + JDesktopPane → JInternalFrame instances\n");
                        break;
                    case CSHARP:
                        builder.append("    - For C#/WPF: Implement proper MDI using a TabControl or custom MDI container.\n");
                        builder.append("    - For C#/WinForms: Set the main form's IsMdiContainer property to true.\n");
                        builder.append("    - Child forms must set their MdiParent property to the main form before showing.\n");
                        builder.append("    - Example: childForm.MdiParent = this; childForm.Show();\n");
                        builder.append("    - DO NOT create independent forms for child windows in MDI mode.\n");
                        break;
                    case PYTHON:
                        builder.append("    - For Python/Tkinter: Create child Toplevel windows with proper parent reference.\n");
                        builder.append("    - For Python/PyQt: Use QMdiArea as the central widget and QMdiSubWindow for children.\n");
                        builder.append("    - Ensure child windows are properly parented to the MDI container.\n");
                        break;
                    default:
                        builder.append("    - Implement true MDI with child windows inside the parent container.\n");
                        builder.append("    - Child windows must be parented to the MDI container, not created as independent windows.\n");
                        break;
                }
                builder.append("\n");
            } else {
                builder.append("  * **Main Window Mode:**\n");
                builder.append("    - The application should start by automatically loading and displaying the main window of the main module.\n");
                builder.append("    - This is the traditional desktop application startup behavior.\n");
            }
        }
        
        builder.append("- Target operating systems: ")
            .append(joinTargets(config.getTargetOperatingSystems())).append("\n");
        
        if (config.getProjectDirectory() != null) {
            builder.append("- Project directory: ").append(config.getProjectDirectory().toAbsolutePath()).append("\n");
        }        
        
        // Add .NET version requirement for C# projects
        if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
            builder.append("- IMPORTANT: Use .NET 9.0 (net9.0) as the target framework for all C# projects.\n");
            builder.append("- When creating .csproj files, use <TargetFramework>net9.0</TargetFramework> or <TargetFramework>net9.0-windows</TargetFramework> for GUI apps.\n");
        }
        
        builder.append('\n');
        
        // Add project-wide settings if available
        if (projectSettings != null) {
            // Build user prompt text for token parsing
            String userPromptText = buildUserPromptText(data);
            appendProjectSettings(builder, projectSettings, config.getProgrammingLanguage(), userPromptText);
        }

        // Task-specific content sections - only include what each task form actually provides
        switch (taskType) {
            case GENERATE_APP_OR_SCRIPT:
                appendIfPresent(builder, "Project overview", data.getProjectOverview());
                appendIfPresent(builder, "Theme & Appearance", data.getThemeDescription());
                appendDialogs(builder, config, data.getDialogs(), data.getMainWindowName());
                appendWorkflowItems(builder, data.getWorkflowItems());
                break;
            case FIX_CODING_ERRORS:
                // FIX_CODING_ERRORS form only has: overview, expected, actual, error details
                appendIfPresent(builder, "Project overview", data.getProjectOverview());
                appendIfPresent(builder, "Expected behaviour", data.getExpectedBehavior());
                appendIfPresent(builder, "Actual behaviour", data.getActualBehavior());
                appendIfPresent(builder, "Errors / logs", data.getErrorDetails());
                break;
            case CREATE_MODULE:
                // CREATE_MODULE form has: overview, theme, dialogs, workflow
                appendIfPresent(builder, "Project overview", data.getProjectOverview());
                appendIfPresent(builder, "Theme & Appearance", data.getThemeDescription());
                appendDialogs(builder, config, data.getDialogs(), data.getMainWindowName());
                appendWorkflowItems(builder, data.getWorkflowItems());
                break;
            case CREATE_ALGORITHM:
                // CREATE_ALGORITHM form only has: algorithm description (no overview, no theme)
                appendIfPresent(builder, "Algorithm description", data.getAlgorithmDescription());
                break;
            case MODIFY_EXISTING_SOFTWARE:
                // MODIFY_EXISTING_SOFTWARE form has: overview, theme, change description, involved files, dialogs, workflow
                appendIfPresent(builder, "Project overview", data.getProjectOverview());
                appendIfPresent(builder, "Theme & Appearance", data.getThemeDescription());
                appendIfPresent(builder, "Change description", data.getChangeDescription());
                appendIfPresent(builder, "Files / modules involved", data.getInvolvedFiles());
                appendDialogs(builder, config, data.getDialogs(), data.getMainWindowName());
                appendWorkflowItems(builder, data.getWorkflowItems());
                break;
            default:
                break;
        }

        builder.append("\n## Preparations\n");        
        builder.append("- Evaluate the current OS.\n");
        builder.append("- IMPORTANT: DO NOT install any system packages using package managers (apt, dnf, pacman, brew, etc.).\n");
        builder.append("- IMPORTANT: DO NOT use sudo or any commands requiring root/administrator privileges.\n");
        builder.append("- Assume all necessary system-level tools and compilers are already installed.\n");
        builder.append("- You may use language-specific package managers (npm, pip, cargo, etc.) for project dependencies only.\n");

        builder.append("\n## Instructions\n");
        
        // Task-specific instructions
        switch (taskType) {
            case GENERATE_APP_OR_SCRIPT:
                if (config.getProjectDirectory() != null) {
                    builder.append("- CREATE and WRITE all necessary project files (source code, configuration files, etc.) in the project directory: ")
                        .append(config.getProjectDirectory().toAbsolutePath()).append("\n");
                } else {
                    builder.append("- CREATE and WRITE all necessary project files (source code, configuration files, etc.) in the current working directory.\n");
                }
                builder.append("- Generate the complete project structure from scratch - do not assume any files exist.\n");
                builder.append("- Use the file creation tools available to you to write each file.\n");
                builder.append("- Produce clear, ready-to-run code answering the task.\n");
                builder.append("- Respect the selected project style when structuring files.\n");
                
                // Add main window architecture instructions for GUI apps
                if (config.getProjectStyle() == ProjectStyle.GUI) {
                    builder.append("\n### Main Window Architecture (GUI Applications Only)\n");
                    builder.append("- CRITICAL: DO NOT create a separate \"host\" or \"main application\" window that is shown to the user.\n");
                    builder.append("- CRITICAL: There should be NO visible container, launcher, or host window.\n");
                    builder.append("- CRITICAL: When the application starts, load the main module and show its MAIN WINDOW.\n");
                    builder.append("- The MAIN WINDOW of a module is the specific dialog that is marked as **[MAIN WINDOW]** in the Dialog Specifications section below.\n");
                    builder.append("- Architecture pattern:\n");
                    builder.append("  1. Initialize the application framework (but do NOT create or show any \"main application window\")\n");
                    builder.append("  2. Load the main module and identify which dialog is designated as its MAIN WINDOW (see Dialog Specifications)\n");
                    builder.append("  3. DIRECTLY show that specific main window dialog as the first and only visible window\n");
                    builder.append("  4. The main module's main window IS the application's primary (and only) user interface\n");
                    builder.append("  5. When the main module's main window closes, the application should exit\n");
                    builder.append("  6. NO intermediate windows, NO host windows, NO launcher windows - ONLY the designated main window\n");
                    builder.append("- The user should see ONLY the main module's main window (as specified in Dialog Specifications) when the application starts.\n\n");
                }
                break;
                
            case FIX_CODING_ERRORS:
                if (config.getProjectDirectory() != null) {
                    builder.append("- Analyze the existing project files in the project directory: ")
                        .append(config.getProjectDirectory().toAbsolutePath()).append("\n");
                } else {
                    builder.append("- Analyze the existing project files in the current working directory.\n");
                }
                builder.append("- CRITICAL: ONLY fix coding errors according to the project description provided above.\n");
                builder.append("- DO NOT create any new features, modules, or functionality.\n");
                builder.append("- DO NOT add any content beyond what is needed to fix the identified errors.\n");
                builder.append("- Identify the root cause of the errors described above.\n");
                builder.append("- MODIFY or FIX only the files that are causing the issues.\n");
                builder.append("- Use the file editing tools available to you to update existing files.\n");
                builder.append("- Ensure your fixes address the expected behavior and resolve the actual behavior discrepancy.\n");
                builder.append("- Preserve all existing functionality and code structure.\n");
                builder.append("- Test your changes to verify the errors are resolved.\n");
                break;
                
            case CREATE_MODULE:
                if (config.getProjectDirectory() != null) {
                    builder.append("- Work with the existing project in the project directory: ")
                        .append(config.getProjectDirectory().toAbsolutePath()).append("\n");
                } else {
                    builder.append("- Work with the existing project in the current working directory.\n");
                }
                builder.append("- CRITICAL: ONLY add new modules to the existing codebase.\n");
                builder.append("- DO NOT modify or restructure existing modules unless absolutely necessary for integration.\n");
                builder.append("- ANALYZE the existing codebase to understand:\n");
                builder.append("  * The current module structure and organization\n");
                builder.append("  * The existing theme, layout patterns, and design system\n");
                builder.append("  * How existing modules are structured and registered\n");
                builder.append("- IDENTIFY and REPLICATE the existing patterns:\n");
                builder.append("  * Module architecture and file organization\n");
                builder.append("  * UI/UX patterns including: color schemes, typography, spacing, component styles, and layout structures\n");
                builder.append("  * Naming conventions and coding style\n");
                builder.append("- CREATE the new module following the module structure specification above.\n");
                builder.append("- PRESERVE the existing module structure - add the new module in a way that maintains consistency.\n");
                builder.append("- Integrate the new module into the existing project architecture.\n");
                builder.append("- ENSURE the new module's UI components match the existing app's visual design and theme.\n");
                builder.append("- ADAPT the theme and appearance specified to fit the existing app's design language and style guidelines.\n");
                builder.append("- MODIFY only the minimal set of existing files needed to:\n");
                builder.append("  * Register or import the new module\n");
                builder.append("  * Connect dependencies between modules\n");
                builder.append("  * Update navigation or module loading systems\n");
                builder.append("- Ensure the module interfaces correctly with its dependencies and dependents.\n");
                builder.append("- Follow the project's existing code style and architectural patterns.\n");
                builder.append("- If dialogs or workflows are specified, implement them using the same styling and layout approach as existing dialogs.\n");
                builder.append("- Maintain visual consistency with existing components (buttons, forms, navigation, etc.).\n");
                
                // Add instructions for modules without windows
                builder.append("\n### Module Types and Main Window\n");
                builder.append("- **CRITICAL**: Each module has a MAIN WINDOW, which is the specific dialog marked as **[MAIN WINDOW]** in the Dialog Specifications section.\n");
                builder.append("- When a module is loaded/launched, show its MAIN WINDOW (not any other window, not a container).\n");
                builder.append("- **Modules with Windows**: If the module specification includes dialog definitions:\n");
                builder.append("  * Create all the specified dialogs for the module.\n");
                builder.append("  * Identify which dialog is marked as **[MAIN WINDOW]** in the Dialog Specifications.\n");
                builder.append("  * When the module is loaded, automatically show that specific main window dialog.\n");
                builder.append("  * Other dialogs in the module should only be shown when explicitly triggered by user actions or workflows.\n");
                builder.append("- **Functionality-Only Modules**: If the module has NO dialog definitions but HAS workflow items:\n");
                builder.append("  * The module provides functionality only (no GUI).\n");
                builder.append("  * Create the module as a service, utility class, or background component.\n");
                builder.append("  * When the module is called/invoked, EXECUTE its functionality directly (don't try to show a window).\n");
                builder.append("  * The workflow items describe the functionality to implement.\n");
                builder.append("  * Expose the module's functionality through public methods, API endpoints, or events.\n");
                builder.append("  * This module should integrate seamlessly with other modules that call its functionality.\n\n");
                break;
                
            case CREATE_ALGORITHM:
                if (config.getProjectDirectory() != null) {
                    builder.append("- Create the algorithm implementation in the project directory: ")
                        .append(config.getProjectDirectory().toAbsolutePath()).append("\n");
                } else {
                    builder.append("- Create the algorithm implementation in the current working directory.\n");
                }
                builder.append("- CRITICAL: This is a standalone algorithm project - the directory must be empty.\n");
                builder.append("- Create a clean, focused implementation without any existing code dependencies.\n");
                builder.append("- Implement the algorithm as described in the specification above.\n");
                builder.append("- Focus on correctness, efficiency, and handling edge cases.\n");
                builder.append("- Consider the performance constraints and requirements mentioned.\n");
                builder.append("- Include appropriate comments explaining the algorithm's logic.\n");
                builder.append("- Create a standalone, well-documented implementation.\n");
                builder.append("- Include example usage or test cases demonstrating the algorithm.\n");
                break;
                
            case MODIFY_EXISTING_SOFTWARE:
                if (config.getProjectDirectory() != null) {
                    builder.append("- Work with the existing software in the project directory: ")
                        .append(config.getProjectDirectory().toAbsolutePath()).append("\n");
                } else {
                    builder.append("- Work with the existing software in the current working directory.\n");
                }
                builder.append("- CRITICAL: ONLY enhance or modify the current code according to the project description.\n");
                builder.append("- DO NOT create entirely new features unless explicitly requested in the change description.\n");
                builder.append("- RESPECT the existing sourcecode structure, patterns, and architecture.\n");
                builder.append("- ANALYZE the existing codebase to understand:\n");
                builder.append("  * The current code organization and architecture\n");
                builder.append("  * The existing theme, layout patterns, and design system\n");
                builder.append("  * The project's coding conventions and patterns\n");
                builder.append("- PRESERVE the existing app's visual design, theme, and layout structure throughout all modifications.\n");
                builder.append("- MODIFY the files and modules listed in the 'Files / modules involved' section.\n");
                builder.append("- Apply ONLY the changes described in the 'Change description' section.\n");
                builder.append("- ENSURE all modified or new UI elements match the existing design language, color scheme, typography, and spacing.\n");
                builder.append("- ADAPT any new theme or appearance specifications to harmonize with the existing app's design while implementing requested changes.\n");
                builder.append("- Ensure modifications integrate smoothly with the existing codebase.\n");
                builder.append("- Preserve existing functionality unless explicitly instructed to change it.\n");
                builder.append("- Add or modify according to the project settings provided.\n");
                builder.append("- If dialogs or workflows are updated, maintain the same styling approach and visual consistency as existing dialogs.\n");
                builder.append("- Maintain code quality and follow the project's existing patterns.\n");
                builder.append("- Keep the overall user experience consistent with the current application design.\n");
                break;
                
            default:
                // Fallback to generic instructions
                if (config.getProjectDirectory() != null) {
                    builder.append("- Work in the project directory: ")
                        .append(config.getProjectDirectory().toAbsolutePath()).append("\n");
                } else {
                    builder.append("- Work in the current working directory.\n");
                }
                builder.append("- Complete the task as described.\n");
                break;
        }

        builder.append("\n## Testing Requirements\n");
        
        // Check if this is a .NET project targeting Windows on a non-Windows system
        if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
            String currentOs = System.getProperty("os.name").toLowerCase();
            boolean isWindows = currentOs.contains("win");
            boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
            
            if (!isWindows && targetIncludesWindows) {
                builder.append("- CRITICAL: DO NOT create unit tests or test projects for this .NET application.\n");
                builder.append("- REASON: This is a .NET application targeting Windows, being built on a non-Windows system using Wine.\n");
                builder.append("- Unit test frameworks (NUnit, xUnit, MSTest) do not work reliably under Wine.\n");
                builder.append("- Creating test projects would prevent successful project execution and compilation.\n");
                builder.append("- Focus ONLY on creating the application source code and configuration files.\n");
                builder.append("- DO NOT add test project references to the solution file.\n");
                builder.append("- DO NOT create any files with 'Test' in the name or in 'Tests' directories.\n");
                builder.append("- Skip all testing-related activities for this project.\n\n");
            } else {
                builder.append("- After creating or modifying the source code, CREATE comprehensive unit tests for all components.\n");
                builder.append("- Unit tests should:\n");
                builder.append("  * Test individual functions, methods, and classes in isolation.\n");
                builder.append("  * Cover normal use cases, edge cases, and error conditions.\n");
                builder.append("  * Use appropriate testing frameworks for the selected programming language (e.g., JUnit for Java, NUnit/xUnit for C#, pytest for Python, Jest for JavaScript).\n");
                builder.append("  * Follow naming conventions: test methods should clearly describe what they test.\n");
                builder.append("  * Include assertions to verify expected behavior.\n");
                builder.append("  * Aim for high code coverage (target: 80%+ of critical code paths).\n");
                builder.append("- Where applicable and feasible, CREATE integration tests that:\n");
                builder.append("  * Test interactions between multiple components or modules.\n");
                builder.append("  * Verify end-to-end workflows and user scenarios.\n");
                builder.append("  * Test database operations, file I/O, or external API calls (use mocking/stubbing where appropriate).\n");
                builder.append("  * Validate that integrated components work together correctly.\n");
                builder.append("  * Test configuration and initialization sequences.\n");
                builder.append("- Organize test files according to the project's test directory structure (e.g., src/test/java, tests/, __tests__).\n");
                builder.append("- IMPORTANT: Ensure tests are runnable with standard test commands for the language/framework:\n");
                builder.append("  * Java/Gradle: 'gradle test' or './gradlew test'\n");
                builder.append("  * Java/Maven: 'mvn test'\n");
                builder.append("  * C#/.NET: 'dotnet test'\n");
                builder.append("  * Python: 'pytest' or 'python -m pytest'\n");
                builder.append("  * JavaScript/Node: 'npm test'\n");
                builder.append("- For GUI applications, focus unit tests on business logic, data models, and non-UI components.\n");
                builder.append("- For algorithms, include tests with known inputs/outputs, boundary conditions, and performance benchmarks where relevant.\n");
                builder.append("- Document any test setup requirements or prerequisites in comments within the test files.\n");
            }
        } else {
            builder.append("- After creating or modifying the source code, CREATE comprehensive unit tests for all components.\n");
            builder.append("- Unit tests should:\n");
            builder.append("  * Test individual functions, methods, and classes in isolation.\n");
            builder.append("  * Cover normal use cases, edge cases, and error conditions.\n");
            builder.append("  * Use appropriate testing frameworks for the selected programming language (e.g., JUnit for Java, NUnit/xUnit for C#, pytest for Python, Jest for JavaScript).\n");
            builder.append("  * Follow naming conventions: test methods should clearly describe what they test.\n");
            builder.append("  * Include assertions to verify expected behavior.\n");
            builder.append("  * Aim for high code coverage (target: 80%+ of critical code paths).\n");
            builder.append("- Where applicable and feasible, CREATE integration tests that:\n");
            builder.append("  * Test interactions between multiple components or modules.\n");
            builder.append("  * Verify end-to-end workflows and user scenarios.\n");
            builder.append("  * Test database operations, file I/O, or external API calls (use mocking/stubbing where appropriate).\n");
            builder.append("  * Validate that integrated components work together correctly.\n");
            builder.append("  * Test configuration and initialization sequences.\n");
            builder.append("- Organize test files according to the project's test directory structure (e.g., src/test/java, tests/, __tests__).\n");
            builder.append("- IMPORTANT: Ensure tests are runnable with standard test commands for the language/framework:\n");
            builder.append("  * Java/Gradle: 'gradle test' or './gradlew test'\n");
            builder.append("  * Java/Maven: 'mvn test'\n");
            builder.append("  * C#/.NET: 'dotnet test'\n");
            builder.append("  * Python: 'pytest' or 'python -m pytest'\n");
            builder.append("  * JavaScript/Node: 'npm test'\n");
            builder.append("- For GUI applications, focus unit tests on business logic, data models, and non-UI components.\n");
            builder.append("- For algorithms, include tests with known inputs/outputs, boundary conditions, and performance benchmarks where relevant.\n");
            builder.append("- Document any test setup requirements or prerequisites in comments within the test files.\n");
        }

        builder.append("\n## Build and Run Instructions\n");
        
        // Task-specific build and run instructions
        // Get project name for use in commands (with fallback)
        String projectName = (config.getProjectName() != null && !config.getProjectName().trim().isEmpty()) 
            ? config.getProjectName() 
            : "MyProject";
        
        switch (taskType) {
            case GENERATE_APP_OR_SCRIPT:
                builder.append("- After creating all files and ensuring dependencies are installed, the build command will be executed automatically.\n");
                
                // Add Wine instructions for .NET projects when building on non-Windows with Windows target
                if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                    String currentOs = System.getProperty("os.name").toLowerCase();
                    boolean isWindows = currentOs.contains("win");
                    boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                    builder.append("- IMPORTANT: When building C# projects, ALWAYS change to the project directory before running dotnet commands.\n");
                    builder.append("- If the project is in a subfolder, use 'cd <ProjectFolder>' first, then run 'dotnet build ").append(projectName).append(".csproj'.\n");
                    builder.append("- ALWAYS specify the .csproj file explicitly in dotnet commands to avoid ambiguity.\n");
                    builder.append("- Use 'dotnet build ").append(projectName).append(".csproj' instead of just 'dotnet build'.\n");
                    builder.append("- Similarly, use 'dotnet run --project ").append(projectName).append(".csproj' to run the project.\n");
                    if (!isWindows && targetIncludesWindows) {
                        builder.append("- IMPORTANT: This is a .NET project targeting Windows on a non-Windows system.\n");
                        builder.append("- ALL .NET commands MUST be prefixed with 'wine':\n");
                        builder.append("  * Build: WINEDEBUG=-all wine dotnet build ").append(projectName).append(".csproj\n");
                        builder.append("  * Run: WINEDEBUG=-all wine dotnet run --project ").append(projectName).append(".csproj\n");
                        builder.append("  * Restore: WINEDEBUG=-all wine dotnet restore ").append(projectName).append(".csproj\n");
                        builder.append("  * Any other dotnet command: WINEDEBUG=-all wine dotnet <command> ").append(projectName).append(".csproj\n");
                        builder.append("- CRITICAL: DO NOT run 'dotnet test' - tests are not supported under Wine for this configuration.\n");
                        builder.append("- After build succeeds, directly run the application without running tests.\n");
                    }
                }
                
                // Conditional test execution based on Wine/Windows configuration
                boolean skipTests = false;
                if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                    String currentOs = System.getProperty("os.name").toLowerCase();
                    boolean isWindows = currentOs.contains("win");
                    boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                    skipTests = !isWindows && targetIncludesWindows;
                }
                
                if (!skipTests) {
                    builder.append("- After the build succeeds, EXECUTE all tests:\n");
                    builder.append("  * Run unit tests first to verify individual components.\n");
                    builder.append("  * Then run integration tests if they exist.\n");
                    builder.append("  * Ensure all tests pass before proceeding.\n");
                    builder.append("  * If tests fail, fix the issues and rebuild/retest.\n");
                    builder.append("- CRITICAL: Once all tests pass, you MUST AUTOMATICALLY START/RUN the compiled program.\n");
                } else {
                    builder.append("- CRITICAL: After the build succeeds, you MUST AUTOMATICALLY START/RUN the compiled program.\n");
                }
                builder.append("- DO NOT wait for user input - automatically launch the application after successful ")
                    .append(skipTests ? "build" : "tests").append(".\n");
                builder.append("- For applications with GUI:\n");
                builder.append("  * Load the main module\n");
                builder.append("  * Show ONLY the main module's MAIN WINDOW - the specific dialog designated as **[MAIN WINDOW]** in the Dialog Specifications");
                if (data.getMainWindowName() != null && !data.getMainWindowName().isBlank()) {
                    builder.append(" (which is the '").append(data.getMainWindowName()).append("' dialog)");
                }
                builder.append("\n");
                builder.append("  * Do NOT show any host, container, or launcher window - ONLY the designated main window dialog\n");
                builder.append("- CRITICAL: The main window of a module is the specific dialog marked as **[MAIN WINDOW]** in Dialog Specifications - show that exact window.\n");
                builder.append("- For CLI tools or scripts, execute them with appropriate default parameters or show usage help.\n");
                builder.append("- For web applications, start the web server and provide the URL to access it.\n");
                builder.append("- Ensure the program runs in the foreground so users can interact with it immediately.\n");
                break;
                
            case FIX_CODING_ERRORS:
                builder.append("- After fixing the errors, rebuild the project to verify the fixes.\n");
                builder.append("- Execute the build command to ensure compilation succeeds.\n");
                
                // Check if this is a .NET Windows project on non-Windows (Wine scenario)
                boolean skipTestsForFix = false;
                if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                    String currentOs = System.getProperty("os.name").toLowerCase();
                    boolean isWindows = currentOs.contains("win");
                    boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                    skipTestsForFix = !isWindows && targetIncludesWindows;
                    builder.append("- IMPORTANT: Change to the project directory before running dotnet commands.\n");
                    builder.append("- Always specify the .csproj file explicitly (e.g., 'dotnet build ").append(projectName).append(".csproj').\n");
                    if (skipTestsForFix) {
                        builder.append("- IMPORTANT: Use 'wine dotnet' prefix for all .NET commands on this non-Windows system targeting Windows.\n");
                        builder.append("- CRITICAL: DO NOT run 'dotnet test' - tests are not supported under Wine for this configuration.\n");
                    }
                }
                
                if (!skipTestsForFix) {
                    builder.append("- Run all tests to verify the fixes haven't broken existing functionality.\n");
                    builder.append("- Ensure all unit tests and integration tests pass.\n");
                }
                
                builder.append("- Run the program to verify it exhibits the expected behavior.\n");
                builder.append("- Confirm that the actual behavior now matches the expected behavior.\n");
                builder.append("- Check that the errors/logs mentioned are no longer present.\n");
                break;
                
            case CREATE_MODULE:
                builder.append("- After creating the new module and updating integration points, rebuild the project.\n");
                builder.append("- Execute the build command to ensure the module compiles correctly.\n");
                
                // Check if this is a .NET Windows project on non-Windows (Wine scenario)
                boolean skipTestsForModule = false;
                if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                    String currentOs = System.getProperty("os.name").toLowerCase();
                    boolean isWindows = currentOs.contains("win");
                    boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                    skipTestsForModule = !isWindows && targetIncludesWindows;
                    builder.append("- IMPORTANT: Change to the project directory before running dotnet commands.\n");
                    builder.append("- Always specify the .csproj file explicitly (e.g., 'dotnet build ").append(projectName).append(".csproj').\n");
                    if (skipTestsForModule) {
                        builder.append("- IMPORTANT: Use 'wine dotnet' prefix for all .NET commands on this non-Windows system targeting Windows.\n");
                        builder.append("- CRITICAL: DO NOT run 'dotnet test' - tests are not supported under Wine for this configuration.\n");
                    }
                }
                
                if (!skipTestsForModule) {
                    builder.append("- Run all tests including the new tests for the module.\n");
                    builder.append("- Verify all unit tests and integration tests pass.\n");
                }
                
                builder.append("- Verify that the module integrates correctly with dependent modules.\n");
                builder.append("- Test the module's functionality according to its specification.\n");
                builder.append("- Ensure modules depending on this new module can access it correctly.\n");
                break;
                
            case CREATE_ALGORITHM:
                builder.append("- After implementing the algorithm, compile or prepare it for execution.\n");
                
                // Check if this is a .NET Windows project on non-Windows (Wine scenario)
                boolean skipTestsForAlgo = false;
                if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                    String currentOs = System.getProperty("os.name").toLowerCase();
                    boolean isWindows = currentOs.contains("win");
                    boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                    skipTestsForAlgo = !isWindows && targetIncludesWindows;
                    builder.append("- IMPORTANT: Change to the project directory before running dotnet commands.\n");
                    builder.append("- Always specify the .csproj file explicitly (e.g., 'dotnet build ").append(projectName).append(".csproj').\n");
                    if (skipTestsForAlgo) {
                        builder.append("- IMPORTANT: Use 'wine dotnet' prefix for all .NET commands on this non-Windows system targeting Windows.\n");
                        builder.append("- CRITICAL: DO NOT run 'dotnet test' - tests are not supported under Wine for this configuration.\n");
                    }
                }
                
                if (!skipTestsForAlgo) {
                    builder.append("- Run the algorithm tests to verify correctness and performance.\n");
                    builder.append("- Ensure all test cases pass, including edge cases and boundary conditions.\n");
                }
                
                builder.append("- Run the algorithm with test cases to verify correctness.\n");
                builder.append("- Demonstrate the algorithm's performance and handling of edge cases.\n");
                builder.append("- Show example outputs validating the implementation.\n");
                break;
                
            case MODIFY_EXISTING_SOFTWARE:
                builder.append("- After modifying the files, rebuild the project to verify the changes.\n");
                builder.append("- Execute the build command to ensure all modifications compile successfully.\n");
                
                // Check if this is a .NET Windows project on non-Windows (Wine scenario)
                boolean skipTestsForModify = false;
                if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
                    String currentOs = System.getProperty("os.name").toLowerCase();
                    boolean isWindows = currentOs.contains("win");
                    boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
                    skipTestsForModify = !isWindows && targetIncludesWindows;
                    builder.append("- IMPORTANT: Change to the project directory before running dotnet commands.\n");
                    builder.append("- Always specify the .csproj file explicitly (e.g., 'dotnet build ").append(projectName).append(".csproj').\n");
                    if (skipTestsForModify) {
                        builder.append("- IMPORTANT: Use 'wine dotnet' prefix for all .NET commands on this non-Windows system targeting Windows.\n");
                        builder.append("- CRITICAL: DO NOT run 'dotnet test' - tests are not supported under Wine for this configuration.\n");
                    }
                }
                
                if (!skipTestsForModify) {
                    builder.append("- Run all existing tests to ensure modifications haven't broken existing functionality.\n");
                    builder.append("- Run any new tests created for the modifications.\n");
                    builder.append("- Verify all unit tests and integration tests pass.\n");
                }
                
                builder.append("- Run the modified software to test the changes.\n");
                builder.append("- Verify that the modifications work as described in the change description.\n");
                builder.append("- Ensure existing functionality remains intact unless explicitly changed.\n");
                builder.append("- Test affected workflows and features to confirm proper integration.\n");
                break;
                
            default:
                builder.append("- Build and test the implementation as appropriate for the task.\n");
                break;
        }

        return builder.toString();
    }

    public static String buildFixPrompt(TaskType taskType, InitialConfig config, MainTaskData data,
                                        String compilerOutput, int attemptNumber) {
        StringBuilder builder = new StringBuilder();
        builder.append("Attempt ").append(attemptNumber).append(" to fix compile errors.\n");
        builder.append("The previous compile failed with the following output:\n\n");
        builder.append(compilerOutput.trim()).append("\n\n");
        builder.append("Re-run the task \"").append(taskType).append("\" while fixing the compilation issues.\n\n");
        
        builder.append("## CRITICAL RULES\n");
        builder.append("- DO NOT create any descriptive or status markdown files (e.g., README.md, STATUS.md, CHANGES.md, TODO.md, etc.).\n");
        builder.append("- Focus ONLY on fixing the code errors and creating functional source code, configuration files, and test files.\n");
        builder.append("- If documentation is needed, include it as code comments, not separate markdown files.\n\n");
        builder.append("Here is the current project context for reference:\n\n");
        builder.append(buildPrimaryPrompt(taskType, config, data));
        return builder.toString();
    }

    public static String buildFinetuningPrompt(TaskType taskType, InitialConfig config, String userPrompt) {
        StringBuilder builder = new StringBuilder();
        
        // Get project name for use in commands (with fallback)
        String projectName = (config.getProjectName() != null && !config.getProjectName().trim().isEmpty()) 
            ? config.getProjectName() 
            : "MyProject";
        
        builder.append("You are GitHub Copilot CLI acting as an expert software engineer.\n");
        builder.append("The user has requested the following modifications to the previously generated code:\n\n");
        builder.append(userPrompt.trim()).append("\n\n");
        
        builder.append("## CRITICAL RULES\n");
        builder.append("- DO NOT create any descriptive or status markdown files (e.g., README.md, STATUS.md, CHANGES.md, TODO.md, etc.).\n");
        builder.append("- Focus ONLY on creating functional source code, configuration files, and test files.\n");
        builder.append("- If documentation is needed, include it as code comments, not separate markdown files.\n\n");
        
        builder.append("## Project Context\n");
        builder.append("- Programming language: ").append(config.getProgrammingLanguage()).append('\n');
        builder.append("- Project style: ").append(config.getProjectStyle()).append('\n');
        
        // Add project name if available
        if (config.getProjectName() != null && !config.getProjectName().trim().isEmpty()) {
            builder.append("- Project name: ").append(config.getProjectName()).append('\n');
        }
        
        builder.append("- Target operating systems: ")
            .append(joinTargets(config.getTargetOperatingSystems())).append("\n");
        
        if (config.getProjectDirectory() != null) {
            builder.append("- Project directory: ").append(config.getProjectDirectory().toAbsolutePath()).append("\n");
        }
        
        // Add .NET version requirement for C# projects
        if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
            builder.append("- IMPORTANT: Use .NET 9.0 (net9.0) as the target framework for all C# projects.\n");
            builder.append("- When creating .csproj files, use <TargetFramework>net9.0</TargetFramework> or <TargetFramework>net9.0-windows</TargetFramework> for GUI apps.\n");
        }
        
        builder.append("\n## Instructions\n");
        builder.append("- IMPORTANT: DO NOT install any system packages using package managers (apt, dnf, pacman, brew, etc.).\n");
        builder.append("- IMPORTANT: DO NOT use sudo or any commands requiring root/administrator privileges.\n");
        builder.append("- Analyze the existing code in the project directory.\n");
        builder.append("- Make the requested modifications while preserving existing functionality unless explicitly asked to change it.\n");
        builder.append("- Use the file editing tools available to you to update existing files.\n");
        builder.append("- Ensure your changes integrate smoothly with the existing codebase.\n");
        builder.append("- Maintain code quality and follow the project's existing patterns.\n");
        
        builder.append("\n## Testing Requirements\n");
        builder.append("- IMPORTANT: You MUST update or create unit tests for any modified or new components.\n");
        builder.append("- IMPORTANT: You MUST update integration tests if the modifications affect component interactions.\n");
        builder.append("- Follow the same testing framework and patterns used in the existing test suite.\n");
        builder.append("- Add new test cases if the modifications introduce new functionality.\n");
        builder.append("- Ensure test coverage for both normal cases and edge cases of the modified functionality.\n");
        builder.append("- IMPORTANT: After creating/updating tests, you MUST RUN all tests:\n");
        builder.append("  * Java/Gradle: Run 'gradle test' or './gradlew test'\n");
        builder.append("  * Java/Maven: Run 'mvn test'\n");
        builder.append("  * C#/.NET: Run 'dotnet test'\n");
        builder.append("  * Python: Run 'pytest' or 'python -m pytest'\n");
        builder.append("  * JavaScript/Node: Run 'npm test'\n");
        builder.append("- Display the test execution output to verify all tests pass.\n");
        builder.append("- If any tests fail, analyze the failures and fix the issues before proceeding.\n");
        builder.append("- Continue running tests until ALL tests pass successfully.\n");
        builder.append("- DO NOT consider the modifications complete until all tests pass.\n");
        
        // Append build and run instructions
        appendBuildAndRunInstructions(builder, taskType, config, projectName);
        
        return builder.toString();
    }

    private static void appendBuildAndRunInstructions(StringBuilder builder, TaskType taskType, InitialConfig config, String projectName) {
        builder.append("\n## Build and Run Instructions\n");
        
        // Add Wine instructions for .NET projects when building on non-Windows with Windows target
        if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
            String currentOs = System.getProperty("os.name").toLowerCase();
            boolean isWindows = currentOs.contains("win");
            boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
            builder.append("- IMPORTANT: When building C# projects, ALWAYS change to the project directory before running dotnet commands.\n");
            builder.append("- If the project is in a subfolder, use 'cd <ProjectFolder>' first, then run 'dotnet build ").append(projectName).append(".csproj'.\n");
            builder.append("- ALWAYS specify the .csproj file explicitly in dotnet commands to avoid ambiguity.\n");
            builder.append("- Use 'dotnet build ").append(projectName).append(".csproj' instead of just 'dotnet build'.\n");
            builder.append("- Similarly, use 'dotnet run --project ").append(projectName).append(".csproj' to run the project.\n");
            if (!isWindows && targetIncludesWindows) {
                builder.append("- IMPORTANT: This is a .NET project targeting Windows on a non-Windows system.\n");
                builder.append("- ALL .NET commands MUST be prefixed with 'wine':\n");
                builder.append("  * Build: WINEDEBUG=-all wine dotnet build ").append(projectName).append(".csproj\n");
                builder.append("  * Run: WINEDEBUG=-all wine dotnet run --project ").append(projectName).append(".csproj\n");
                builder.append("  * Restore: WINEDEBUG=-all wine dotnet restore ").append(projectName).append(".csproj\n");
                builder.append("  * Any other dotnet command: WINEDEBUG=-all wine dotnet <command> ").append(projectName).append(".csproj\n");
            }
        }
        
        builder.append("- After making the requested modifications, rebuild the project to verify the changes.\n");
        builder.append("- Execute the build command to ensure all modifications compile successfully.\n");
        builder.append("- IMPORTANT: RUN ALL TESTS (unit tests and integration tests) to verify the changes:\n");
        builder.append("  * Execute the appropriate test command for the project (gradle test, mvn test, dotnet test, pytest, npm test).\n");
        builder.append("  * Display the full test execution output.\n");
        builder.append("  * Verify that ALL tests pass successfully.\n");
        builder.append("  * If any test fails, fix the issue and re-run tests until all pass.\n");
        builder.append("- DO NOT proceed to running the program until all tests pass.\n");
        builder.append("- CRITICAL: Once all tests pass, you MUST AUTOMATICALLY RUN the modified program.\n");
        builder.append("- DO NOT wait for user input - automatically launch the application after successful tests.\n");
        builder.append("- Verify that the modifications work as requested.\n");
        builder.append("- Ensure existing functionality remains intact unless explicitly changed.\n");
    }

    private static void appendIfPresent(StringBuilder builder, String title, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        builder.append("## ").append(title).append("\n");
        builder.append(value.trim()).append("\n\n");
    }

    private static void appendDialogs(StringBuilder builder, InitialConfig config, List<DialogDefinition> dialogs, String mainWindowName) {
        if (dialogs == null || dialogs.isEmpty()) {
            return;
        }
        builder.append("## Dialog Specifications\n");
        
        // Check if we're in MDI mode
        boolean isMdiMode = config.getProjectStyle() == ProjectStyle.GUI && 
                           config.getProgramMode() == ProgramMode.MDI;
        
        // Add main window information at the beginning
        if (isMdiMode) {
            builder.append("**MDI MODE - DO NOT AUTO-SHOW MAIN WINDOW**\n");
            builder.append("CRITICAL: Since the application is running in MDI mode, DO NOT automatically show the main window on startup.\n");
            builder.append("The MDI parent window should show ONLY the menu bar and an empty workspace.\n");
            builder.append("Windows should ONLY be opened when the user selects them from the menu.\n");
            builder.append("The main window designation below is for reference but should NOT trigger automatic display.\n");
            builder.append("\n**MDI MENU FILTERING**: Some windows are marked as \"Show in MDI Menu = false\".\n");
            builder.append("These windows should NOT appear in the MDI parent's menu system.\n");
            builder.append("They are typically subwindows that are embedded within other windows (e.g., using WindowHost controls).\n");
            builder.append("Only windows with \"Show in MDI Menu = true\" should appear in the menu.\n\n");
        } else if (mainWindowName != null && !mainWindowName.isBlank()) {
            builder.append("**MAIN WINDOW DESIGNATION**: The '").append(mainWindowName)
                .append("' dialog is designated as the MAIN WINDOW for this module.\n");
            builder.append("CRITICAL: This specific window MUST be displayed automatically when the module is loaded/launched.\n");
            builder.append("CRITICAL: When you see a dialog marked with **[MAIN WINDOW]** below, that is the window to show on module startup.\n\n");
        } else if (!dialogs.isEmpty()) {
            // If no main window is explicitly set, the first dialog is the main window
            builder.append("**MAIN WINDOW DESIGNATION**: The first dialog ('").append(dialogs.get(0).getName())
                .append("') is designated as the MAIN WINDOW for this module.\n");
            builder.append("CRITICAL: This specific window MUST be displayed automatically when the module is loaded/launched.\n");
            builder.append("CRITICAL: When you see a dialog marked with **[MAIN WINDOW]** below, that is the window to show on module startup.\n\n");
        }
        
        for (int i = 0; i < dialogs.size(); i++) {
            DialogDefinition dialog = dialogs.get(i);
            builder.append(i + 1).append('.').append(' ')
                .append(dialog.getName()).append(" (window title: ")
                .append(dialog.getWindowTitle()).append(")");
            
            // Mark if this is the main window
            if (dialog.getName().equals(mainWindowName) || 
                (mainWindowName == null && i == 0)) {
                builder.append(" **[MAIN WINDOW]**");
            }
            
            if (dialog.isModal()) {
                builder.append(" [MODAL]");
            }
            
            // Add MDI menu indicator if in MDI mode
            if (isMdiMode) {
                builder.append(" [Show in MDI Menu: ").append(dialog.isShowInMdiMenu() ? "YES" : "NO").append("]");
            }
            
            builder.append("\n");
            
            // Add emphasis for main window - but only if NOT in MDI mode
            if (!isMdiMode && (dialog.getName().equals(mainWindowName) || 
                (mainWindowName == null && i == 0))) {
                builder.append("*** THIS IS THE MAIN WINDOW - Show this specific dialog when the module is loaded/launched! ***\n");
            }
            
            if (dialog.getDescription() != null && !dialog.getDescription().isBlank()) {
                builder.append(dialog.getDescription().trim()).append("\n");
            }
            
            if (dialog.isModal()) {
                builder.append("This dialog should be implemented as a MODAL dialog - it must block user interaction with other windows until closed.\n");
            } else {
                builder.append("This dialog should be implemented as a NON-MODAL (modeless) dialog - it allows interaction with other windows while open.\n");
            }
            
            // Include form layout JSON if available
            String formLayoutJson = dialog.getFormLayoutJson();
            if (formLayoutJson != null && !formLayoutJson.trim().isEmpty()) {
                builder.append("\n**IMPORTANT: Use the following JSON layout definition to create this dialog:**\n");
                builder.append("The JSON below describes the exact layout, controls, and properties for this dialog.\n");
                builder.append("Parse this JSON to determine:\n");
                builder.append("- The form dimensions (width, height)\n");
                builder.append("- All GUI elements (buttons, text fields, labels, etc.) and their types\n");
                builder.append("- Element positions (x, y coordinates)\n");
                builder.append("- Element sizes (width, height)\n");
                builder.append("- Element properties (text, labels, colors, fonts, etc.)\n");
                builder.append("- **Element IDs**: Each element has a unique 'id' field - use this as the variable/control name in your code\n");
                builder.append("- **Events**: Custom event handlers defined for the form and its elements\n");
                builder.append("- **WindowHost Elements**: If the JSON contains elements of type 'WindowHost', these represent containers for child windows:\n");
                builder.append("  * Each WindowHost has an 'initialWindow' property specifying which dialog/window to display initially\n");
                builder.append("  * Create a container control at the specified position and size\n");
                builder.append("  * Load and display the specified dialog/window inside this container\n");
                builder.append("  * The child window should be embedded within the parent window's WindowHost area\n");
                builder.append("  * If 'initialWindow' is empty or not specified, create an empty container that can be populated later\n");
                builder.append("Create the dialog UI based on this layout specification:\n\n");
                builder.append("```json\n");
                builder.append(formLayoutJson.trim());
                builder.append("\n```\n\n");
                builder.append("Use this JSON data to generate the exact layout as designed. ");
                builder.append("Parse the JSON structure to extract element types, positions, sizes, and properties. ");
                builder.append("Each GUI element's 'id' field should be used as the variable or control name in your generated code. ");
                builder.append("When event descriptions reference element IDs (e.g., 'submitButton', 'nameTextField'), they refer to these JSON element IDs. ");
                builder.append("For WindowHost elements, implement container controls that can host child windows as specified by the 'initialWindow' property. ");
                builder.append("Generate code that recreates this layout faithfully.\n\n");
                
                // Parse and extract event information
                appendEventsFromJson(builder, formLayoutJson, dialog.getName());
            } else {
                builder.append("Note: No specific layout has been defined for this dialog. ");
                builder.append("Use the description above to design an appropriate layout.\n");
            }
            
            builder.append('\n');
        }
    }

    private static void appendWorkflowItems(StringBuilder builder, List<WorkflowItem> workflowItems) {
        if (workflowItems == null || workflowItems.isEmpty()) {
            return;
        }
        builder.append("## Workflow Specifications\n");
        builder.append("The following workflows describe the user journeys and system flows that must be implemented:\n\n");
        for (int i = 0; i < workflowItems.size(); i++) {
            WorkflowItem item = workflowItems.get(i);
            builder.append(i + 1).append(". **").append(item.getName()).append("**\n");
            
            if (item.getWindowAffected() != null && !item.getWindowAffected().isBlank()) {
                builder.append("   - Window: ").append(item.getWindowAffected()).append("\n");
            }
            
            if (item.getTrigger() != null && !item.getTrigger().isBlank()) {
                builder.append("   - Triggered by: ").append(item.getTrigger()).append("\n");
            }
            
            List<WorkflowStep> steps = item.getSteps();
            if (steps != null && !steps.isEmpty()) {
                builder.append("   - Steps:\n");
                boolean foundAsyncPoint = false;
                for (int j = 0; j < steps.size(); j++) {
                    WorkflowStep step = steps.get(j);
                    
                    // Check if this is the start of an async section
                    if (!foundAsyncPoint && step.isWaitForRequirement()) {
                        foundAsyncPoint = true;
                        builder.append("     [ASYNC START: The following steps (starting from step ")
                            .append(j + 1)
                            .append(") should be executed asynchronously in a separate process/thread/background task]\n");
                    }
                    
                    builder.append("     ").append(j + 1).append(". ")
                        .append(step.getDescription());
                    
                    // Add requirements if present
                    if (step.getRequirements() != null && !step.getRequirements().isBlank()) {
                        builder.append("\n        Requirements: ").append(step.getRequirements().trim());
                    }
                    
                    // Add wait for requirement flag if set
                    if (step.isWaitForRequirement()) {
                        builder.append("\n        [WAIT: Before continuing to the next step, wait until this requirement is met. ")
                            .append("Poll, monitor, or check the requirement condition periodically. ")
                            .append("Do not proceed to the next step until the requirement is satisfied.]");
                    }
                    
                    // Add stop flag if set
                    if (step.isStopIfRequirementNotMet()) {
                        builder.append("\n        [CRITICAL: If the requirements of THIS STEP are not met, stop the ENTIRE workflow '")
                            .append(item.getName())
                            .append("' immediately. Do not proceed to subsequent steps. Continue with the next workflow.]");
                    }
                    
                    builder.append("\n");
                }
            }
            builder.append('\n');
        }
    }

    private static void appendProjectSettings(StringBuilder builder, ProjectSettings settings) {
        appendProjectSettings(builder, settings, null, null);
    }

    private static void appendProjectSettings(StringBuilder builder, ProjectSettings settings, 
                                              ProgrammingLanguage language, String userPromptText) {
        if (settings == null) {
            return;
        }
        
        // Append global variables
        if (settings.getGlobalVariables() != null && !settings.getGlobalVariables().isEmpty()) {
            builder.append("## Global Variables (Project-Wide)\n");
            builder.append("The following global variables are defined for this project and can be referenced throughout.\n");
            builder.append("In your code and documentation, reference these variables using the format [VARIABLENAME].\n");
            builder.append("For example, if a variable is named 'API_URL', reference it as [API_URL].\n\n");
            for (GlobalVariable var : settings.getGlobalVariables()) {
                builder.append("- **[").append(var.getName()).append("]**: ");
                if (var.getValue() != null && !var.getValue().isBlank()) {
                    builder.append(var.getValue());
                } else {
                    builder.append("(no value)");
                }
                builder.append("\n");
            }
            builder.append("\n");
        }
        
        // Append project-wide workflows
        if (settings.getProjectWorkflows() != null && !settings.getProjectWorkflows().isEmpty()) {
            builder.append("## Project-Wide Workflow Specifications\n");
            builder.append("The following workflows apply across all modules and describe project-wide user journeys and system flows:\n\n");
            appendWorkflowItems(builder, settings.getProjectWorkflows());
        }
        
        // Append database description
        if (settings.getDatabaseDescription() != null && !settings.getDatabaseDescription().isBlank()) {
            builder.append("## Database System\n");
            builder.append("Database: ").append(settings.getDatabaseDescription()).append("\n");
            builder.append("Note: Use this database system for persistence layer implementation.\n\n");
        }
        
        // Append database definition file with full context
        if (settings.getDatabaseDefinitionFile() != null) {
            appendFile(builder, settings.getDatabaseDefinitionFile(), language, userPromptText);
        }
    }
    
    /**
     * Builds a combined text from MainTaskData fields for token parsing.
     */
    private static String buildUserPromptText(MainTaskData data) {
        if (data == null) {
            return "";
        }
        
        StringBuilder text = new StringBuilder();
        
        if (data.getProjectOverview() != null) {
            text.append(data.getProjectOverview()).append(" ");
        }
        if (data.getThemeDescription() != null) {
            text.append(data.getThemeDescription()).append(" ");
        }
        if (data.getAlgorithmDescription() != null) {
            text.append(data.getAlgorithmDescription()).append(" ");
        }
        if (data.getChangeDescription() != null) {
            text.append(data.getChangeDescription()).append(" ");
        }
        if (data.getInvolvedFiles() != null) {
            text.append(data.getInvolvedFiles()).append(" ");
        }
        
        return text.toString();
    }

    private static void appendModuleVariables(StringBuilder builder, com.vibecoding.wizard.Module module) {
        if (module == null || module.getModuleVariables() == null || module.getModuleVariables().isEmpty()) {
            return;
        }
        
        builder.append("**Module Variables:**\n");
        builder.append("The following module-specific variables are defined for this module.\n");
        builder.append("Reference these variables using the format [VARIABLENAME] in this module's context.\n\n");
        for (ModuleVariable var : module.getModuleVariables()) {
            builder.append("- **[").append(var.getName()).append("]**: ");
            if (var.getValue() != null && !var.getValue().isBlank()) {
                builder.append(var.getValue());
            } else {
                builder.append("(no value)");
            }
            builder.append("\n");
        }
        builder.append("\n");
    }

    private static void appendFile(StringBuilder builder, Path file) {
        appendFile(builder, file, null, null);
    }

    private static void appendFile(StringBuilder builder, Path file, ProgrammingLanguage language, String userPrompt) {
        if (file == null) {
            return;
        }
        
        try {
            // Load the schema file
            DatabaseSchemaLoader schemaLoader = DatabaseSchemaLoader.create(file);
            schemaLoader.load();
            
            // Parse tokens from user prompt if provided
            DatabaseTokenParser.ParsedTokens parsedTokens = null;
            if (userPrompt != null && !userPrompt.isEmpty()) {
                DatabaseTokenParser tokenParser = DatabaseTokenParser.create(schemaLoader);
                parsedTokens = tokenParser.parseTokens(userPrompt);
                
                // Check for validation errors
                if (!parsedTokens.areAllValid()) {
                    builder.append("## Database Token Validation Errors\n");
                    builder.append("WARNING: The following database tokens in your prompt have issues:\n\n");
                    for (String error : parsedTokens.getValidationErrors()) {
                        builder.append("- ").append(error).append("\n");
                    }
                    builder.append("\nPlease verify these tokens before proceeding.\n\n");
                }
            }
            
            // Create database context
            DatabaseSchemaContext context = DatabaseSchemaContext.create(schemaLoader, parsedTokens, language);
            
            // Append full schema content
            builder.append("## Database Schema Definition\n");
            builder.append("The following database schema is provided for reference and code generation.\n");
            builder.append("Use this schema to understand the database structure and generate appropriate code.\n\n");
            builder.append("```\n");
            builder.append(context.getFullSchemaContent());
            if (!context.getFullSchemaContent().endsWith("\n")) {
                builder.append("\n");
            }
            builder.append("```\n\n");
            
            // Append referenced tables/fields if any were found
            if (context.hasReferencedTables()) {
                builder.append("## Referenced Database Elements\n");
                builder.append(context.generateStructuredSummary());
                builder.append("\n");
            }
            
            // Append ORM and migration recommendations
            builder.append("## Database Implementation Guidelines\n");
            builder.append("**ORM (Object-Relational Mapping):**\n");
            builder.append(context.getOrmRecommendation()).append("\n\n");
            builder.append("**Migrations:**\n");
            builder.append(context.getMigrationRecommendation()).append("\n\n");
            builder.append("**Connection Configuration:**\n");
            builder.append("Use the 'Database Description' project setting for connection details (host, port, credentials, etc.).\n");
            builder.append("NEVER log or expose database credentials in code. Use environment variables or secure configuration.\n\n");
            
        } catch (IOException e) {
            builder.append("## Database Schema Error\n");
            builder.append("ERROR: Could not read database schema file: ").append(file.toAbsolutePath()).append("\n");
            builder.append("Reason: ").append(e.getMessage()).append("\n");
            builder.append("Please ensure the file exists and is readable.\n\n");
        } catch (IllegalStateException e) {
            builder.append("## Database Schema Error\n");
            builder.append("ERROR: ").append(e.getMessage()).append("\n");
            builder.append("Please check the database schema file configuration.\n\n");
        }
    }

    // Removed: appendDependencyInstructions method
    // This method contained system package installation instructions with sudo commands.
    // The system now assumes all necessary compilers and tools are pre-installed.

    private static void appendEventsFromJson(StringBuilder builder, String json, String dialogName) {
        try {
            // Simple JSON parsing to extract events without external dependencies
            // Look for "events" arrays in the JSON
            boolean hasFormEvents = json.contains("\"events\"");
            
            if (hasFormEvents) {
                builder.append("**Event Handlers:**\n");
                builder.append("The JSON layout includes custom event definitions. For each event defined:\n");
                builder.append("1. Create an event handler function/method with the specified name\n");
                builder.append("2. The 'triggerDescription' field describes when the event should be triggered\n");
                builder.append("3. The 'actionDescription' field describes what the handler should do\n");
                builder.append("4. Wire up the appropriate GUI event (button click, text change, etc.) to call the handler\n");
                builder.append("5. Implement the handler logic according to the action description\n\n");
                
                builder.append("**Important:** Parse the 'events' arrays in the JSON:\n");
                builder.append("- Form-level events in the 'form' object represent global dialog events\n");
                builder.append("- Element-level events in each 'elements' entry represent events for specific controls\n");
                builder.append("- Each event has: name, triggerDescription, and actionDescription\n");
                builder.append("- Generate appropriate event handler code based on these specifications\n\n");
                
                builder.append("**Referencing GUI Elements by ID:**\n");
                builder.append("- Each GUI element in the JSON has a unique 'id' field (e.g., \"submitButton\", \"nameTextField\")\n");
                builder.append("- When event descriptions mention an element ID, reference that specific GUI element in your code\n");
                builder.append("- Example: If triggerDescription says \"when the user clicks submitButton\", find the element with id=\"submitButton\" and attach the handler to its click event\n");
                builder.append("- Example: If actionDescription says \"validate the nameTextField and display error in errorLabel\", find elements with those IDs and manipulate them\n");
                builder.append("- Use the element IDs to create meaningful variable names in your generated code (e.g., submitButton, nameTextField)\n");
                builder.append("- The element ID serves as both the reference in event descriptions and the recommended variable/control name in code\n\n");
            }
        } catch (Exception e) {
            // If parsing fails, continue without event information
            System.err.println("Failed to parse events from JSON: " + e.getMessage());
        }
    }

    private static String joinTargets(EnumSet<TargetOs> targets) {
        if (targets == null || targets.isEmpty()) {
            return "Not specified";
        }
        return targets.stream()
            .map(target -> target.toString().toLowerCase(Locale.ENGLISH))
            .collect(Collectors.joining(", "));
    }

    /**
     * Build a combined prompt that includes scaffolding, module generation, and build/test phases
     * to be executed in a single agent run.
     * 
     * @param config The initial configuration
     * @param modules List of modules to generate
     * @return A comprehensive prompt for all phases
     */
    public static String buildCombinedPrompt(InitialConfig config, List<com.vibecoding.wizard.Module> modules) {
        return buildCombinedPrompt(config, modules, null);
    }

    /**
     * Build a combined prompt that includes scaffolding, module generation, and build/test phases
     * to be executed in a single agent run.
     * 
     * @param config The initial configuration
     * @param modules List of modules to generate
     * @param projectSettings Project-wide settings (can be null)
     * @return A comprehensive prompt for all phases
     */
    public static String buildCombinedPrompt(InitialConfig config, List<com.vibecoding.wizard.Module> modules, ProjectSettings projectSettings) {
        StringBuilder builder = new StringBuilder();
        
        // Get project name for use in commands (with fallback)
        String projectName = (config.getProjectName() != null && !config.getProjectName().trim().isEmpty()) 
            ? config.getProjectName() 
            : "MyProject";
        
        builder.append("You are GitHub Copilot CLI acting as an expert software engineer.\n");
        builder.append("Your job is to create a complete modular application in a SINGLE RUN.\n");
        builder.append("This includes scaffolding, implementing all modules, building, testing, and running.\n\n");
        
        builder.append("## CRITICAL RULES\n");
        builder.append("- DO NOT create any descriptive or status markdown files (e.g., README.md, STATUS.md, CHANGES.md, TODO.md, etc.).\n");
        builder.append("- Focus ONLY on creating functional source code, configuration files, and test files.\n");
        builder.append("- If documentation is needed, include it as code comments, not separate markdown files.\n");
        builder.append("- Complete ALL phases (scaffolding, module generation, build, test, run) in THIS SINGLE RUN.\n");
        builder.append("- DO NOT wait for additional prompts or user input between phases.\n\n");
        
        builder.append("## Project Context\n");
        builder.append("- Programming language: ").append(config.getProgrammingLanguage()).append('\n');
        builder.append("- Project style: ").append(config.getProjectStyle()).append('\n');
        
        // Add project name if available
        if (config.getProjectName() != null && !config.getProjectName().trim().isEmpty()) {
            builder.append("- Project name: ").append(config.getProjectName()).append('\n');
        }
        
        // Add program mode for GUI applications (same as in buildPrimaryPrompt)
        if (config.getProjectStyle() == ProjectStyle.GUI && config.getProgramMode() != null) {
            builder.append("- Program mode: ").append(config.getProgramMode()).append('\n');
            if (config.getProgramMode() == ProgramMode.MDI) {
                builder.append("  * **MDI Mode Requirements:**\n");
                builder.append("    ========================================\n");
                builder.append("    CRITICAL: DO NOT SHOW THE MAIN WINDOW AUTOMATICALLY ON STARTUP!\n");
                builder.append("    ========================================\n");
                builder.append("    - The application should start with an MDI (Multiple Document Interface) parent/container window.\n");
                builder.append("    - The MDI parent window should show ONLY:\n");
                builder.append("      * A menu bar at the top\n");
                builder.append("      * An empty workspace/client area (NO child windows initially)\n");
                builder.append("    - DO NOT automatically open, show, or load the main window on startup\n");
                builder.append("    - DO NOT automatically open, show, or load ANY window on startup\n");
                builder.append("\n");
                builder.append("    **Menu Structure (CRITICAL - Follow This Hierarchy Exactly):**\n");
                builder.append("    - Create a main menu bar in the MDI parent window.\n");
                builder.append("    - ONLY top-level modules (modules without a parent) should appear as top-level menu entries.\n");
                builder.append("    - For each TOP-LEVEL module:\n");
                builder.append("      * Create a top-level menu with the module name\n");
                builder.append("      * Add menu items for each of the module's dialogs/windows\n");
                builder.append("      * If the module has submodules, add them as SUBMENUS (not top-level menus):\n");
                builder.append("        - Each submodule appears as a submenu under its parent module's menu\n");
                builder.append("        - The submenu contains menu items for the submodule's dialogs/windows\n");
                builder.append("        - If a submodule has its own submodules, nest them further as sub-submenus\n");
                builder.append("    - IMPORTANT: Submodules must NOT appear as top-level menus - they must be nested under their parent\n");
                builder.append("    - You will see the module list below with parent-child relationships indicated.\n");
                builder.append("    - Use the parent module information to build the correct menu hierarchy.\n");
                builder.append("    - Example hierarchy:\n");
                builder.append("      Menu Bar:\n");
                builder.append("        - Module A (top-level menu)\n");
                builder.append("          ├─ Window A1 (menu item - opens window)\n");
                builder.append("          ├─ Window A2 (menu item - opens window)\n");
                builder.append("          └─ Submodule A-1 (submenu, NOT a top-level menu)\n");
                builder.append("              ├─ Window A1-1 (menu item - opens window)\n");
                builder.append("              └─ Window A1-2 (menu item - opens window)\n");
                builder.append("        - Module B (top-level menu)\n");
                builder.append("          ├─ Window B1 (menu item - opens window)\n");
                builder.append("          └─ Submodule B-1 (submenu, NOT a top-level menu)\n");
                builder.append("              └─ Window B1-1 (menu item - opens window)\n");
                builder.append("\n");
                builder.append("    - Clicking a menu item (not submenu) should open the corresponding window inside the MDI parent.\n");
                builder.append("    - Multiple child windows can be open simultaneously within the MDI container.\n");
                builder.append("    - Each window should be independently closable, movable, and resizable within the MDI parent.\n");
                builder.append("    - The application startup behavior: Show MDI parent with menu, nothing else!\n");
                builder.append("    - Users launch windows via the menu system as needed.\n");
                builder.append("\n");
                builder.append("    **MDI Technical Implementation (CRITICAL):**\n");
                
                // Add language-specific MDI instructions
                switch (config.getProgrammingLanguage()) {
                    case JAVA:
                        builder.append("    - For Java/Swing: Use JDesktopPane as the MDI container in the main JFrame.\n");
                        builder.append("    - Each dialog/window must be implemented as a JInternalFrame (not JFrame or JDialog).\n");
                        builder.append("    - CRITICAL: When opening a child window from a menu, follow this exact sequence:\n");
                        builder.append("      1. Create the JInternalFrame instance\n");
                        builder.append("      2. Add it to the JDesktopPane: desktopPane.add(internalFrame)\n");
                        builder.append("      3. Make it visible: internalFrame.setVisible(true)\n");
                        builder.append("      4. Move to front: internalFrame.toFront()\n");
                        builder.append("      5. Request focus: try { internalFrame.setSelected(true); } catch (PropertyVetoException e) {}\n");
                        builder.append("    - This ensures child windows appear ON TOP of the MDI parent, not behind it.\n");
                        builder.append("    - DO NOT create standalone JFrame windows for child windows in MDI mode.\n");
                        builder.append("    - Example structure: JFrame (MDI parent) → JMenuBar + JDesktopPane → JInternalFrame instances\n");
                        break;
                    case CSHARP:
                        builder.append("    - For C#/WPF: Implement proper MDI using a TabControl or custom MDI container.\n");
                        builder.append("    - For C#/WinForms: Set the main form's IsMdiContainer property to true.\n");
                        builder.append("    - Child forms must set their MdiParent property to the main form before showing.\n");
                        builder.append("    - Example: childForm.MdiParent = this; childForm.Show();\n");
                        builder.append("    - DO NOT create independent forms for child windows in MDI mode.\n");
                        break;
                    case PYTHON:
                        builder.append("    - For Python/Tkinter: Create child Toplevel windows with proper parent reference.\n");
                        builder.append("    - For Python/PyQt: Use QMdiArea as the central widget and QMdiSubWindow for children.\n");
                        builder.append("    - Ensure child windows are properly parented to the MDI container.\n");
                        break;
                    default:
                        builder.append("    - Implement true MDI with child windows inside the parent container.\n");
                        builder.append("    - Child windows must be parented to the MDI container, not created as independent windows.\n");
                        break;
                }
                builder.append("\n");
            } else {
                builder.append("  * **Main Window Mode:**\n");
                builder.append("    - The application should start by automatically loading and displaying the main window of the main module.\n");
                builder.append("    - This is the traditional desktop application startup behavior.\n");
            }
        }
        
        builder.append("- Target operating systems: ")
            .append(joinTargets(config.getTargetOperatingSystems())).append("\n");
        
        if (config.getProjectDirectory() != null) {
            builder.append("- Project directory: ").append(config.getProjectDirectory().toAbsolutePath()).append("\n");
        }
        
        // Add .NET version requirement for C# projects
        if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
            builder.append("- IMPORTANT: Use .NET 9.0 (net9.0) as the target framework for all C# projects.\n");
            builder.append("- When creating .csproj files, use <TargetFramework>net9.0</TargetFramework> or <TargetFramework>net9.0-windows</TargetFramework> for GUI apps.\n");
        }
        
        builder.append("- Number of modules to implement: ").append(modules.size()).append("\n\n");
        
        // Add project-wide settings if available
        if (projectSettings != null) {
            // For module generation, collect prompt text from all modules
            StringBuilder allModuleText = new StringBuilder();
            for (com.vibecoding.wizard.Module module : modules) {
                MainTaskData moduleData = module.getTaskData();
                if (moduleData != null) {
                    allModuleText.append(buildUserPromptText(moduleData)).append(" ");
                }
            }
            appendProjectSettings(builder, projectSettings, config.getProgrammingLanguage(), allModuleText.toString());
        }
        
        // Check if directory is empty to determine whether to create new or modify existing (IDE mode only)
        boolean directoryIsEmpty = true;
        if (config.getProjectDirectory() != null) {
            try {
                java.nio.file.Path projectDir = config.getProjectDirectory();
                if (java.nio.file.Files.exists(projectDir)) {
                    try (java.util.stream.Stream<java.nio.file.Path> entries = java.nio.file.Files.list(projectDir)) {
                        directoryIsEmpty = !entries.findAny().isPresent();
                    }
                }
            } catch (java.io.IOException e) {
                // If we can't check, assume empty and proceed with creation
                directoryIsEmpty = true;
            }
        }
        
        if (!directoryIsEmpty) {
            // Directory is not empty - add modification instructions
            builder.append("## IMPORTANT: Existing Project Detected\n");
            builder.append("- The project directory is NOT EMPTY: ")
                .append(config.getProjectDirectory().toAbsolutePath()).append("\n");
            builder.append("- ANALYZE the existing codebase before making any changes.\n");
            builder.append("- MODIFY existing code according to the module specifications below.\n");
            builder.append("- PRESERVE all existing functionality and layout UNLESS they differ from the module specifications.\n");
            builder.append("- Only change aspects that need to be changed to meet the requirements.\n");
            builder.append("- Use file editing tools to UPDATE existing files rather than recreating them.\n");
            builder.append("- Add new files only when necessary to fulfill the specifications.\n");
            builder.append("- Maintain the existing code style, patterns, and architecture.\n");
            builder.append("- Respect the project's current structure and organization.\n\n");
        }
        
        // ========== PHASE 1: SCAFFOLDING ==========
        builder.append("## PHASE 1: Project Scaffolding\n");
        
        if (directoryIsEmpty) {
            // Empty directory - create new scaffolding
            builder.append("Create the basic project structure and build configuration:\n\n");
            
            builder.append("### Scaffolding Tasks\n");
            builder.append("1. Create the main project directory structure\n");
            builder.append("2. Set up the build system and dependency management files\n");
            builder.append("   - For Java: build.gradle or pom.xml\n");
            builder.append("   - For C#: .csproj and .sln files\n");
            builder.append("   - For Python: setup.py or pyproject.toml\n");
            builder.append("   - For JavaScript: package.json\n");
            builder.append("3. Create the main application entry point\n");
            builder.append("4. Set up a 'modules' or 'src/modules' directory for module implementations\n");
            builder.append("5. Create the module loader/registry infrastructure\n");
            builder.append("6. Set up test infrastructure and directories\n");
            builder.append("7. DO NOT implement any specific modules yet - just create the scaffolding\n\n");
        } else {
            // Non-empty directory - analyze and adapt existing scaffolding
            builder.append("Analyze and adapt the existing project structure:\n\n");
            
            builder.append("### Scaffolding Tasks\n");
            builder.append("1. ANALYZE the existing project directory structure\n");
            builder.append("2. IDENTIFY the existing build system and dependency management files\n");
            builder.append("3. LOCATE the main application entry point\n");
            builder.append("4. FIND the modules directory or create one if missing\n");
            builder.append("5. VERIFY the module loader/registry infrastructure or add it if missing\n");
            builder.append("6. CHECK test infrastructure and add if missing\n");
            builder.append("7. PRESERVE all existing scaffolding unless it conflicts with requirements\n");
            builder.append("8. Only MODIFY or ADD scaffolding elements that are necessary for the modules below\n\n");
        }
        
        // ========== PHASE 2: MODULE GENERATION ==========
        builder.append("## PHASE 2: Module Implementation\n");
        builder.append("Implement ALL ").append(modules.size()).append(" modules in this phase:\n\n");
        
        for (int i = 0; i < modules.size(); i++) {
            com.vibecoding.wizard.Module module = modules.get(i);
            builder.append("### Module ").append(i + 1).append(": ").append(module.getName()).append("\n");
            builder.append("- Task Type: ").append(module.getTaskType()).append("\n");
            
            // Show parent-child relationship for MDI menu structure
            if (module.getParent() != null) {
                builder.append("- **Parent Module: ").append(module.getParent().getName()).append("**\n");
                builder.append("  (This is a SUBMODULE - must appear as a submenu under '")
                    .append(module.getParent().getName()).append("', NOT as a top-level menu)\n");
            } else {
                builder.append("- **Parent Module: None (Top-Level Module)**\n");
                builder.append("  (This is a TOP-LEVEL module - should appear as a top-level menu entry)\n");
            }
            
            // Add module variables if defined
            appendModuleVariables(builder, module);
            
            MainTaskData data = module.getTaskData();
            if (data != null) {
                appendIfPresent(builder, "Module Overview", data.getProjectOverview());
                appendIfPresent(builder, "Module Theme & Appearance", data.getThemeDescription());
                
                // Add module-specific specifications
                if (data.getDialogs() != null && !data.getDialogs().isEmpty()) {
                    appendDialogs(builder, config, data.getDialogs(), data.getMainWindowName());
                }
                if (data.getWorkflowItems() != null && !data.getWorkflowItems().isEmpty()) {
                    appendWorkflowItems(builder, data.getWorkflowItems());
                }
                
                appendIfPresent(builder, "Algorithm Description", data.getAlgorithmDescription());
                appendIfPresent(builder, "Change Description", data.getChangeDescription());
            }
            
            builder.append("**Module Implementation Instructions:**\n");
            builder.append("- Create all source code files for this module\n");
            builder.append("- Follow the existing project structure and coding patterns\n");
            builder.append("- Register this module with the module loader/registry\n");
            builder.append("- Implement all dialogs and workflows as specified\n");
            builder.append("- Create unit tests for this module\n");
            builder.append("- Ensure proper integration with other modules\n\n");
        }
        
        // ========== PHASE 3: BUILD, TEST, AND RUN ==========
        builder.append("## PHASE 3: Build, Test, and Run\n");
        builder.append("After implementing all modules, complete these final steps:\n\n");
        
        builder.append("### Build Instructions\n");
        builder.append("- Build the entire project using the appropriate build command\n");
        
        // Add Wine instructions for .NET projects when building on non-Windows with Windows target
        if (config.getProgrammingLanguage() == ProgrammingLanguage.CSHARP) {
            String currentOs = System.getProperty("os.name").toLowerCase();
            boolean isWindows = currentOs.contains("win");
            boolean targetIncludesWindows = config.getTargetOperatingSystems().contains(TargetOs.WINDOWS);
            builder.append("- IMPORTANT: When building C# projects, ALWAYS change to the project directory before running dotnet commands.\n");
            builder.append("- ALWAYS specify the .csproj file explicitly in dotnet commands to avoid ambiguity.\n");
            builder.append("- Use 'dotnet build ").append(projectName).append(".csproj' instead of just 'dotnet build'.\n");
            if (!isWindows && targetIncludesWindows) {
                builder.append("- IMPORTANT: This is a .NET project targeting Windows on a non-Windows system.\n");
                builder.append("- ALL .NET commands MUST be prefixed with 'wine':\n");
                builder.append("  * Build: WINEDEBUG=-all wine dotnet build ").append(projectName).append(".csproj\n");
                builder.append("  * Run: WINEDEBUG=-all wine dotnet run --project ").append(projectName).append(".csproj\n");
                builder.append("  * Test: WINEDEBUG=-all wine dotnet test ").append(projectName).append(".csproj\n");
            }
        }
        
        builder.append("- Ensure compilation succeeds without errors\n");
        builder.append("- Fix any compilation errors that arise\n\n");
        
        builder.append("### Testing Requirements\n");
        builder.append("- CRITICAL: You MUST run ALL tests after the build succeeds:\n");
        builder.append("  * Run unit tests for all modules and components\n");
        builder.append("  * Run integration tests if they exist\n");
        builder.append("  * Ensure ALL tests pass before proceeding to run the application\n");
        builder.append("- Test execution commands:\n");
        builder.append("  * Java/Gradle: 'gradle test' or './gradlew test'\n");
        builder.append("  * Java/Maven: 'mvn test'\n");
        builder.append("  * C#/.NET: 'dotnet test' (with wine prefix if needed)\n");
        builder.append("  * Python: 'pytest' or 'python -m pytest'\n");
        builder.append("  * JavaScript/Node: 'npm test'\n");
        builder.append("- Display the test execution output\n");
        builder.append("- If any tests fail, fix the issues and re-run tests until all pass\n");
        builder.append("- DO NOT proceed to running the application until all tests pass\n\n");
        
        builder.append("### Run Instructions\n");
        builder.append("- CRITICAL: Once all tests pass, you MUST AUTOMATICALLY RUN the application\n");
        builder.append("- DO NOT wait for user input - automatically launch the application after successful tests\n");
        
        if (config.getProjectStyle() == ProjectStyle.GUI) {
            builder.append("- For GUI applications:\n");
            builder.append("  * Load the main module\n");
            builder.append("  * Show ONLY the main module's MAIN WINDOW (the specific dialog designated as [MAIN WINDOW])\n");
            builder.append("  * DO NOT show any host, container, or launcher window - ONLY the designated main window\n");
        } else if (config.getProjectStyle() == ProjectStyle.SCRIPT) {
            builder.append("- For script/CLI applications:\n");
            builder.append("  * Execute the application with appropriate default parameters or show usage help\n");
        } else if (config.getProjectStyle() == ProjectStyle.WEB) {
            builder.append("- For web applications:\n");
            builder.append("  * Start the web server\n");
            builder.append("  * Provide the URL to access the application\n");
        }
        
        builder.append("- Ensure the program runs in the foreground so users can interact with it immediately\n\n");
        
        // ========== GENERAL INSTRUCTIONS ==========
        builder.append("## General Instructions\n");
        builder.append("- IMPORTANT: DO NOT install any system packages using package managers (apt, dnf, pacman, brew, etc.)\n");
        builder.append("- IMPORTANT: DO NOT use sudo or any commands requiring root/administrator privileges\n");
        builder.append("- Assume all necessary system-level tools and compilers are already installed\n");
        builder.append("- You may use language-specific package managers (npm, pip, cargo, etc.) for project dependencies only\n");
        builder.append("- Complete ALL phases in this SINGLE RUN without waiting for additional prompts\n");
        builder.append("- Use the file creation and editing tools available to you\n");
        builder.append("- Follow best practices for code organization and architecture\n");
        builder.append("- Ensure all modules integrate properly with each other\n");
        builder.append("- Maintain consistent coding style throughout the project\n\n");
        
        builder.append("## Success Criteria\n");
        builder.append("The task is complete when:\n");
        builder.append("1. ✓ Project structure is scaffolded\n");
        builder.append("2. ✓ All ").append(modules.size()).append(" modules are implemented\n");
        builder.append("3. ✓ Project builds successfully without errors\n");
        builder.append("4. ✓ All unit tests pass\n");
        builder.append("5. ✓ All integration tests pass (if applicable)\n");
        builder.append("6. ✓ Application is running and ready for user interaction\n\n");
        
        return builder.toString();
    }
}
