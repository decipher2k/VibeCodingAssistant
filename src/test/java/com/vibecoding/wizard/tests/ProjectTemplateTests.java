/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class ProjectTemplateTests {

    public static void run(TestContext ctx) {
        testBasicSerialization(ctx);
        testCompleteTemplate(ctx);
        testRoundTrip(ctx);
    }

    private static void testBasicSerialization(TestContext ctx) {
        try {
            MainTaskData data = new MainTaskData();
            data.setProjectOverview("Test Project");
            data.setThemeDescription("Modern theme");
            
            ProjectTemplate template = new ProjectTemplate(
                ProgrammingLanguage.JAVA,
                ProjectStyle.GUI,
                EnumSet.of(TargetOs.LINUX),
                TaskType.GENERATE_APP_OR_SCRIPT,
                data,
                null
            );
            
            String json = template.toJson();
            
            ctx.assertTrue("Should contain programming language", json.contains("\"programmingLanguage\": \"JAVA\""));
            ctx.assertTrue("Should contain project overview", json.contains("\"projectOverview\": \"Test Project\""));
        } catch (Exception e) {
            ctx.fail("testBasicSerialization failed: " + e.getMessage());
        }
    }

    private static void testCompleteTemplate(TestContext ctx) {
        try {
            MainTaskData data = new MainTaskData();
            data.setProjectOverview("Complete Test");
            data.setThemeDescription("Dark theme");
            data.setExpectedBehavior("Should work");
            data.setActualBehavior("Works perfectly");
            
            // Add dialogs
            List<DialogDefinition> dialogs = new ArrayList<>();
            dialogs.add(new DialogDefinition("MainDialog", "Main Window", "The main window", true));
            dialogs.add(new DialogDefinition("Settings", "Settings", "Settings dialog", false));
            data.setDialogs(dialogs);
            
            // Add workflow items
            List<WorkflowItem> workflows = new ArrayList<>();
            WorkflowItem workflow = new WorkflowItem();
            workflow.setName("Login Workflow");
            workflow.setWindowAffected("MainDialog");
            workflow.setTrigger("User clicks login");
            
            List<WorkflowStep> steps = new ArrayList<>();
            steps.add(new WorkflowStep("Validate credentials", "Valid username and password", true, false));
            steps.add(new WorkflowStep("Show main screen", "", false, false));
            workflow.setSteps(steps);
            
            workflows.add(workflow);
            data.setWorkflowItems(workflows);
            
            ProjectTemplate template = new ProjectTemplate(
                ProgrammingLanguage.PYTHON,
                ProjectStyle.WEB,
                EnumSet.of(TargetOs.WINDOWS, TargetOs.LINUX),
                TaskType.GENERATE_APP_OR_SCRIPT,
                data,
                null
            );
            
            String json = template.toJson();
            
            ctx.assertTrue("Should contain dialog", json.contains("\"name\": \"MainDialog\""));
            ctx.assertTrue("Should contain workflow", json.contains("\"name\": \"Login Workflow\""));
            ctx.assertTrue("Should contain workflow step", json.contains("\"description\": \"Validate credentials\""));
        } catch (Exception e) {
            ctx.fail("testCompleteTemplate failed: " + e.getMessage());
        }
    }

    private static void testRoundTrip(TestContext ctx) {
        try {
            MainTaskData originalData = new MainTaskData();
            originalData.setProjectOverview("Round Trip Test");
            originalData.setThemeDescription("Light theme with blue accents");
            
            List<DialogDefinition> dialogs = new ArrayList<>();
            dialogs.add(new DialogDefinition("TestDialog", "Test", "A test dialog", true));
            originalData.setDialogs(dialogs);
            
            ProjectTemplate originalTemplate = new ProjectTemplate(
                ProgrammingLanguage.CSHARP,
                ProjectStyle.SCRIPT,
                EnumSet.of(TargetOs.MACOS),
                TaskType.CREATE_MODULE,
                originalData,
                java.nio.file.Paths.get("/test/path")
            );
            
            // Serialize
            String json = originalTemplate.toJson();
            
            // Deserialize
            ProjectTemplate loadedTemplate = ProjectTemplate.fromJson(json);
            
            // Verify
            ctx.assertEquals("Programming language should match", 
                ProgrammingLanguage.CSHARP, loadedTemplate.getProgrammingLanguage());
            ctx.assertEquals("Project style should match", 
                ProjectStyle.SCRIPT, loadedTemplate.getProjectStyle());
            ctx.assertEquals("Task type should match", 
                TaskType.CREATE_MODULE, loadedTemplate.getTaskType());
            ctx.assertTrue("Should contain MACOS", 
                loadedTemplate.getTargetOperatingSystems().contains(TargetOs.MACOS));
            
            MainTaskData loadedData = loadedTemplate.getTaskData();
            ctx.assertNotNull("Task data should not be null", loadedData);
            ctx.assertEquals("Project overview should match", 
                "Round Trip Test", loadedData.getProjectOverview());
            ctx.assertEquals("Theme description should match",
                "Light theme with blue accents", loadedData.getThemeDescription());
            ctx.assertEquals("Dialog count should match", 
                1, loadedData.getDialogs().size());
            
            DialogDefinition dialog = loadedData.getDialogs().get(0);
            ctx.assertEquals("Dialog name should match", "TestDialog", dialog.getName());
            ctx.assertTrue("Dialog should be modal", dialog.isModal());
            
            // Verify project directory is preserved
            ctx.assertNotNull("Project directory should not be null", loadedTemplate.getProjectDirectory());
            ctx.assertEquals("Project directory should match", 
                "/test/path", loadedTemplate.getProjectDirectory().toString());
        } catch (Exception e) {
            ctx.fail("testRoundTrip failed: " + e.getMessage());
        }
    }
}
