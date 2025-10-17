/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.util.List;

import com.vibecoding.wizard.DialogDefinition;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.WorkflowItem;
import com.vibecoding.wizard.WorkflowStep;

public final class MainTaskDataTests {
    private MainTaskDataTests() {
    }

    public static void run(TestContext ctx) {
        MainTaskData data = new MainTaskData();
        data.setProjectOverview("Project");
        data.setExpectedBehavior("Should work");
        data.setActualBehavior("Crashes");
        data.setErrorDetails("Stacktrace");
        data.setDialogs(List.of(new DialogDefinition("Login", "Login", "Desc")));
        data.setAlgorithmDescription("Algo");
        data.setChangeDescription("Change");
        data.setInvolvedFiles("Files");
        data.setThemeDescription("Dark theme with blue accents");
        
        // Create workflow items with steps
        WorkflowStep step1 = new WorkflowStep("Click login button", "User must be logged in", false, true);
        WorkflowStep step2 = new WorkflowStep("Navigate to dashboard", null, false, false);
        WorkflowItem workflow = new WorkflowItem("User Login", "Main Window", "Startup", List.of(step1, step2));
        data.setWorkflowItems(List.of(workflow));

        ctx.assertEquals("Overview getter", "Project", data.getProjectOverview());
        ctx.assertEquals("Expected getter", "Should work", data.getExpectedBehavior());
        ctx.assertEquals("Actual getter", "Crashes", data.getActualBehavior());
        ctx.assertEquals("Error getter", "Stacktrace", data.getErrorDetails());
        ctx.assertEquals("Dialogs size", 1, data.getDialogs().size());
        ctx.assertEquals("Algorithm description", "Algo", data.getAlgorithmDescription());
        ctx.assertEquals("Change description", "Change", data.getChangeDescription());
        ctx.assertEquals("Involved files", "Files", data.getInvolvedFiles());
        ctx.assertEquals("Theme description", "Dark theme with blue accents", data.getThemeDescription());
        ctx.assertEquals("Workflow items size", 1, data.getWorkflowItems().size());
        ctx.assertEquals("Workflow item name", "User Login", data.getWorkflowItems().get(0).getName());

        MainTaskData clone = new MainTaskData();
        clone.copyFrom(data);
        ctx.assertEquals("Copy overview", data.getProjectOverview(), clone.getProjectOverview());
        ctx.assertEquals("Copy dialogs count", data.getDialogs().size(), clone.getDialogs().size());
        ctx.assertEquals("Copy theme", data.getThemeDescription(), clone.getThemeDescription());
        ctx.assertEquals("Copy workflow count", data.getWorkflowItems().size(), clone.getWorkflowItems().size());
    }
}
