/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import com.vibecoding.wizard.DialogDefinition;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.PromptBuilder;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;
import com.vibecoding.wizard.WorkflowItem;
import com.vibecoding.wizard.WorkflowStep;

public final class PromptBuilderTests {
    private PromptBuilderTests() {
    }

    public static void run(TestContext ctx) {
        InitialConfig config = new InitialConfig(ProgrammingLanguage.JAVA, ProjectStyle.GUI,
            EnumSet.of(TargetOs.LINUX, TargetOs.WINDOWS), null);
        
        // Create a temporary directory with content for tasks that require non-empty directories
        Path tempDir = null;
        InitialConfig configWithDir = null;
        try {
            tempDir = Files.createTempDirectory("promptbuilder-test");
            Files.writeString(tempDir.resolve("dummy.java"), "public class Dummy {}");
            configWithDir = new InitialConfig(ProgrammingLanguage.JAVA, ProjectStyle.GUI,
                EnumSet.of(TargetOs.LINUX, TargetOs.WINDOWS), tempDir);
        } catch (IOException e) {
            ctx.assertTrue("Failed to create temp directory: " + e.getMessage(), false);
            return;
        }

        MainTaskData data = new MainTaskData();
        data.setProjectOverview("Overview text");
        data.setDialogs(List.of(new DialogDefinition("Dialog", "Title", "Description")));

        String prompt = PromptBuilder.buildPrimaryPrompt(TaskType.GENERATE_APP_OR_SCRIPT, config, data);
        ctx.assertTrue("Prompt contains overview", prompt.contains("Project overview"));
        ctx.assertTrue("Prompt contains dialogs", prompt.contains("Dialog Specifications"));

        // Test theme description inclusion
        data.setThemeDescription("Modern dark theme with rounded corners");
        String promptWithTheme = PromptBuilder.buildPrimaryPrompt(TaskType.GENERATE_APP_OR_SCRIPT, config, data);
        ctx.assertTrue("Prompt contains theme", promptWithTheme.contains("Theme & Appearance"));
        ctx.assertTrue("Prompt contains theme description", promptWithTheme.contains("Modern dark theme"));

        // Test workflow items inclusion
        WorkflowStep step1 = new WorkflowStep("Initialize database", "Database must exist", false, true);
        WorkflowStep step2 = new WorkflowStep("Load user data", null, true, false);
        WorkflowItem workflow = new WorkflowItem("Startup Process", "Main Window", "Application Launch", List.of(step1, step2));
        data.setWorkflowItems(List.of(workflow));
        
        String promptWithWorkflow = PromptBuilder.buildPrimaryPrompt(TaskType.GENERATE_APP_OR_SCRIPT, config, data);
        ctx.assertTrue("Prompt contains workflow", promptWithWorkflow.contains("Workflow Specifications"));
        ctx.assertTrue("Prompt contains workflow name", promptWithWorkflow.contains("Startup Process"));
        ctx.assertTrue("Prompt contains workflow window", promptWithWorkflow.contains("Main Window"));
        ctx.assertTrue("Prompt contains workflow trigger", promptWithWorkflow.contains("Application Launch"));
        ctx.assertTrue("Prompt contains wait flag", promptWithWorkflow.contains("[WAIT:"));
        ctx.assertTrue("Prompt contains stop flag", promptWithWorkflow.contains("[CRITICAL:"));

        data.setExpectedBehavior("Expected");
        data.setActualBehavior("Actual");
        data.setErrorDetails("Errors");
        String fixPrompt = PromptBuilder.buildPrimaryPrompt(TaskType.FIX_CODING_ERRORS, configWithDir, data);
        ctx.assertTrue("Fix prompt expected", fixPrompt.contains("Expected behaviour"));
        ctx.assertTrue("Fix prompt actual", fixPrompt.contains("Actual behaviour"));
        ctx.assertTrue("Fix prompt errors", fixPrompt.contains("Errors / logs"));

        data.setProjectOverview("Module overview");
        data.setThemeDescription("Module theme");
        String modulePrompt = PromptBuilder.buildPrimaryPrompt(TaskType.CREATE_MODULE, configWithDir, data);
        ctx.assertTrue("Module prompt overview", modulePrompt.contains("Project overview"));
        ctx.assertTrue("Module prompt theme", modulePrompt.contains("Theme & Appearance"));
        ctx.assertTrue("Module prompt theme analysis", modulePrompt.contains("ANALYZE the existing codebase to understand"));
        ctx.assertTrue("Module prompt replicate UI", modulePrompt.contains("IDENTIFY and REPLICATE the existing patterns"));
        ctx.assertTrue("Module prompt has theme section", modulePrompt.contains("Theme & Appearance"));

        data.setAlgorithmDescription("Algorithm");
        String algorithmPrompt = PromptBuilder.buildPrimaryPrompt(TaskType.CREATE_ALGORITHM, config, data);
        ctx.assertTrue("Algorithm prompt", algorithmPrompt.contains("Algorithm description"));

        data.setChangeDescription("Changes");
        data.setInvolvedFiles("Files");
        String modifyPrompt = PromptBuilder.buildPrimaryPrompt(TaskType.MODIFY_EXISTING_SOFTWARE, configWithDir, data);
        ctx.assertTrue("Modify prompt change", modifyPrompt.contains("Change description"));
        ctx.assertTrue("Modify prompt files", modifyPrompt.contains("Files / modules involved"));
        ctx.assertTrue("Modify prompt theme analysis", modifyPrompt.contains("ANALYZE the existing codebase to understand"));
        ctx.assertTrue("Modify prompt preserve design", modifyPrompt.contains("PRESERVE the existing app's visual design"));

        String fixAttempt = PromptBuilder.buildFixPrompt(TaskType.GENERATE_APP_OR_SCRIPT, config, data, "Compile error", 3);
        ctx.assertTrue("Fix attempt includes number", fixAttempt.contains("Attempt 3"));
        ctx.assertTrue("Fix attempt includes compiler output", fixAttempt.contains("Compile error"));

        // Test buildFinetuningPrompt method
        String finetuningPrompt = PromptBuilder.buildFinetuningPrompt(TaskType.GENERATE_APP_OR_SCRIPT, config, "Add dark mode toggle button");
        ctx.assertTrue("Finetuning prompt contains user request", finetuningPrompt.contains("Add dark mode toggle button"));
        ctx.assertTrue("Finetuning prompt has context", finetuningPrompt.contains("Project Context"));
        ctx.assertTrue("Finetuning prompt has instructions", finetuningPrompt.contains("## Instructions"));
        ctx.assertTrue("Finetuning prompt has testing", finetuningPrompt.contains("## Testing Requirements"));
        ctx.assertTrue("Finetuning prompt has build instructions", finetuningPrompt.contains("## Build and Run Instructions"));
        ctx.assertTrue("Finetuning prompt mentions updating tests", finetuningPrompt.contains("update or create unit tests"));
        ctx.assertTrue("Finetuning prompt mentions running tests", finetuningPrompt.contains("you MUST RUN all tests"));
        ctx.assertTrue("Finetuning prompt has test commands", finetuningPrompt.contains("gradle test") && finetuningPrompt.contains("dotnet test"));
        
        // Cleanup temp directory
        try {
            Files.deleteIfExists(tempDir.resolve("dummy.java"));
            Files.deleteIfExists(tempDir);
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }
}
