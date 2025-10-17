/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;

public final class ModuleTreePanelTests {
    private ModuleTreePanelTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Cannot test ModuleTreePanel in headless mode");
            return;
        }

        testClassExists(ctx);
        testIsJPanel(ctx);
    }

    private static void testClassExists(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.ModuleTreePanel");
            ctx.assertNotNull("ModuleTreePanel class exists", clazz);
        } catch (ClassNotFoundException e) {
            ctx.fail("ModuleTreePanel class not found: " + e.getMessage());
        }
    }

    private static void testIsJPanel(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.ModuleTreePanel");
            ctx.assertTrue("ModuleTreePanel is a JPanel", 
                javax.swing.JPanel.class.isAssignableFrom(clazz));
        } catch (Exception e) {
            ctx.fail("JPanel test failed: " + e.getMessage());
        }
    }
}
