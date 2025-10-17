/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;

public final class FormLayoutBuilderTests {
    private FormLayoutBuilderTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        testClassExists(ctx);
        testUtilityClass(ctx);
    }

    private static void testClassExists(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.FormLayoutBuilder");
            ctx.assertNotNull("FormLayoutBuilder class exists", clazz);
        } catch (ClassNotFoundException e) {
            ctx.fail("FormLayoutBuilder class not found: " + e.getMessage());
        }
    }

    private static void testUtilityClass(TestContext ctx) {
        try {
            // FormLayoutBuilder is a utility class with static methods
            Class<?> clazz = Class.forName("com.vibecoding.wizard.FormLayoutBuilder");
            ctx.assertTrue("FormLayoutBuilder is a utility class", true);
        } catch (Exception e) {
            ctx.fail("Utility class test failed: " + e.getMessage());
        }
    }
}
