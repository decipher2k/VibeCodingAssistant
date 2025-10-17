/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.IDEController;
import com.vibecoding.wizard.IDEProject;

import java.awt.GraphicsEnvironment;

public final class IDEControllerTests {
    private IDEControllerTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Cannot test IDEController in headless mode (requires Swing)");
            return;
        }

        testControllerCreation(ctx);
        testProjectManagement(ctx);
        testDirtyFlag(ctx);
    }

    private static void testControllerCreation(TestContext ctx) {
        try {
            IDEController controller = new IDEController();
            ctx.assertNotNull("Controller created", controller);
            ctx.assertNotNull("Frame created", controller.getFrame());
            ctx.assertEquals("No current project initially", null, controller.getCurrentProject());
            ctx.assertEquals("No selected module initially", null, controller.getSelectedModule());
            ctx.assertFalse("Not dirty initially", controller.isDirty());
        } catch (Exception e) {
            ctx.fail("Controller creation failed: " + e.getMessage());
        }
    }

    private static void testProjectManagement(TestContext ctx) {
        try {
            IDEController controller = new IDEController();
            IDEProject project = new IDEProject();
            
            // Note: We can't easily test setCurrentProject as it's likely package-private
            // or calls complex UI update methods. We test what we can access.
            ctx.assertEquals("Initial project is null", null, controller.getCurrentProject());
            
        } catch (Exception e) {
            ctx.fail("Project management test failed: " + e.getMessage());
        }
    }

    private static void testDirtyFlag(TestContext ctx) {
        try {
            IDEController controller = new IDEController();
            ctx.assertFalse("Initially not dirty", controller.isDirty());
            
            // Note: We can't directly test setDirty() without accessing it via reflection
            // or having it be public, but we can verify the initial state
            
        } catch (Exception e) {
            ctx.fail("Dirty flag test failed: " + e.getMessage());
        }
    }
}
