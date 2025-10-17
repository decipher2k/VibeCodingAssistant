/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.WorkflowItem;
import com.vibecoding.wizard.WorkflowStep;

public final class WorkflowItemTests {
    private WorkflowItemTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        testWorkflowItemCreation(ctx);
        testWorkflowItemProperties(ctx);
        testWorkflowSteps(ctx);
    }

    private static void testWorkflowItemCreation(TestContext ctx) {
        try {
            WorkflowItem item = new WorkflowItem();
            ctx.assertNotNull("WorkflowItem created", item);
        } catch (Exception e) {
            ctx.fail("WorkflowItem creation failed: " + e.getMessage());
        }
    }

    private static void testWorkflowItemProperties(TestContext ctx) {
        try {
            WorkflowItem item = new WorkflowItem();
            
            item.setName("Login Workflow");
            ctx.assertEquals("Name set", "Login Workflow", item.getName());
            
            item.setWindowAffected("MainWindow");
            ctx.assertEquals("Window affected set", "MainWindow", item.getWindowAffected());
            
            item.setTrigger("User clicks login button");
            ctx.assertEquals("Trigger set", "User clicks login button", item.getTrigger());
            
        } catch (Exception e) {
            ctx.fail("WorkflowItem properties test failed: " + e.getMessage());
        }
    }

    private static void testWorkflowSteps(TestContext ctx) {
        try {
            WorkflowItem item = new WorkflowItem();
            
            WorkflowStep step1 = new WorkflowStep("Step 1", "Do something", false, false);
            WorkflowStep step2 = new WorkflowStep("Step 2", "Do something else", true, false);
            
            java.util.List<WorkflowStep> steps = new java.util.ArrayList<>();
            steps.add(step1);
            steps.add(step2);
            
            item.setSteps(steps);
            
            ctx.assertEquals("Steps set", 2, item.getSteps().size());
            ctx.assertEquals("First step", step1.getDescription(), item.getSteps().get(0).getDescription());
            ctx.assertEquals("Second step", step2.getDescription(), item.getSteps().get(1).getDescription());
            
        } catch (Exception e) {
            ctx.fail("WorkflowSteps test failed: " + e.getMessage());
        }
    }
}
