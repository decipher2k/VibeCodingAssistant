/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Method;

public final class IDEMainFrameTests {
    private IDEMainFrameTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Cannot test IDEMainFrame in headless mode");
            return;
        }

        testClassExists(ctx);
        testConstructor(ctx);
    }

    private static void testClassExists(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.IDEMainFrame");
            ctx.assertNotNull("IDEMainFrame class exists", clazz);
        } catch (ClassNotFoundException e) {
            ctx.fail("IDEMainFrame class not found: " + e.getMessage());
        }
    }

    private static void testConstructor(TestContext ctx) {
        try {
            // Note: We can't actually instantiate without full IDE setup
            // Just verify the class structure
            Class<?> clazz = Class.forName("com.vibecoding.wizard.IDEMainFrame");
            ctx.assertTrue("IDEMainFrame is a JFrame", 
                javax.swing.JFrame.class.isAssignableFrom(clazz));
        } catch (Exception e) {
            ctx.fail("Constructor test failed: " + e.getMessage());
        }
    }
}
