/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.ProcessAndResult;

public final class ProcessAndResultTests {
    private ProcessAndResultTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        testClassExists(ctx);
        testProcessWrapper(ctx);
    }

    private static void testClassExists(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.ProcessAndResult");
            ctx.assertNotNull("ProcessAndResult class exists", clazz);
        } catch (ClassNotFoundException e) {
            ctx.fail("ProcessAndResult class not found: " + e.getMessage());
        }
    }

    private static void testProcessWrapper(TestContext ctx) {
        try {
            // ProcessAndResult wraps Process, OutputStream, and Thread
            // We can't easily test without actually running a process
            // Just verify the class structure
            Class<?> clazz = Class.forName("com.vibecoding.wizard.ProcessAndResult");
            ctx.assertTrue("ProcessAndResult has waitForCompletion method", 
                clazz.getMethod("waitForCompletion") != null);
        } catch (Exception e) {
            ctx.fail("Process wrapper test failed: " + e.getMessage());
        }
    }
}
