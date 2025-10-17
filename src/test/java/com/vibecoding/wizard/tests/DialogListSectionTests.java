/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;

public final class DialogListSectionTests {
    private DialogListSectionTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Cannot test DialogListSection in headless mode");
            return;
        }

        testClassExists(ctx);
    }

    private static void testClassExists(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.DialogListSection");
            ctx.assertNotNull("DialogListSection class exists", clazz);
        } catch (ClassNotFoundException e) {
            ctx.fail("DialogListSection class not found: " + e.getMessage());
        }
    }
}
