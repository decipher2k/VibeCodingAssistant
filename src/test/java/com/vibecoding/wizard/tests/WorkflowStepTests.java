/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.WorkflowStep;

public final class WorkflowStepTests {
    private WorkflowStepTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        testWorkflowStepCreation(ctx);
        testWorkflowStepProperties(ctx);
        testWorkflowStepFlags(ctx);
    }

    private static void testWorkflowStepCreation(TestContext ctx) {
        try {
            WorkflowStep step = new WorkflowStep("Click button", "User logged in", false, false);
            ctx.assertNotNull("WorkflowStep created", step);
        } catch (Exception e) {
            ctx.fail("WorkflowStep creation failed: " + e.getMessage());
        }
    }

    private static void testWorkflowStepProperties(TestContext ctx) {
        try {
            WorkflowStep step = new WorkflowStep("Click button", "User logged in", true, false);
            
            ctx.assertEquals("Description", "Click button", step.getDescription());
            ctx.assertEquals("Requirements", "User logged in", step.getRequirements());
            ctx.assertTrue("Stop if requirement not met", step.isStopIfRequirementNotMet());
            ctx.assertFalse("Not waiting for requirement", step.isWaitForRequirement());
            
        } catch (Exception e) {
            ctx.fail("WorkflowStep properties test failed: " + e.getMessage());
        }
    }

    private static void testWorkflowStepFlags(TestContext ctx) {
        try {
            WorkflowStep step1 = new WorkflowStep("Save data", "Data valid", false, true);
            ctx.assertFalse("Stop not set", step1.isStopIfRequirementNotMet());
            ctx.assertTrue("Wait for requirement", step1.isWaitForRequirement());
            
            WorkflowStep step2 = new WorkflowStep("Click and save", "Always", true, true);
            ctx.assertTrue("Stop if requirement not met", step2.isStopIfRequirementNotMet());
            ctx.assertTrue("Wait for requirement", step2.isWaitForRequirement());
            
        } catch (Exception e) {
            ctx.fail("WorkflowStep flags test failed: " + e.getMessage());
        }
    }
}
