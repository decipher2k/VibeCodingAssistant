/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;

public final class AlgorithmResultDialogTests {
    private AlgorithmResultDialogTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Cannot test AlgorithmResultDialog in headless mode");
            return;
        }

        testClassExists(ctx);
        testIsDialog(ctx);
    }

    private static void testClassExists(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.AlgorithmResultDialog");
            ctx.assertNotNull("AlgorithmResultDialog class exists", clazz);
        } catch (ClassNotFoundException e) {
            ctx.fail("AlgorithmResultDialog class not found: " + e.getMessage());
        }
    }

    private static void testIsDialog(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.AlgorithmResultDialog");
            ctx.assertTrue("AlgorithmResultDialog is a JDialog", 
                javax.swing.JDialog.class.isAssignableFrom(clazz));
        } catch (Exception e) {
            ctx.fail("JDialog test failed: " + e.getMessage());
        }
    }
}
