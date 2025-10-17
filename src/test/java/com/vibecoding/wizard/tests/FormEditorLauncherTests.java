/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.FormEditorLauncher;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Method;

public final class FormEditorLauncherTests {
    private FormEditorLauncherTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        // Note: We cannot actually test FormEditorLauncher by opening real windows
        // as they would stay open and block test execution. Instead, we test
        // what we can without creating actual UI components.
        
        testClassExists(ctx);
        testMethodsExist(ctx);
    }

    private static void testClassExists(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.FormEditorLauncher");
            ctx.assertNotNull("FormEditorLauncher class exists", clazz);
        } catch (ClassNotFoundException e) {
            ctx.fail("FormEditorLauncher class not found: " + e.getMessage());
        }
    }

    private static void testMethodsExist(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.FormEditorLauncher");
            
            // Check that openFormEditor method exists
            Method openFormEditor = clazz.getDeclaredMethod("openFormEditor", 
                String.class, String.class, java.awt.Window.class);
            ctx.assertNotNull("openFormEditor method exists", openFormEditor);
            
            // Check that openFormEditorModal method exists
            Method openFormEditorModal = clazz.getDeclaredMethod("openFormEditorModal", 
                String.class, String.class, java.awt.Window.class);
            ctx.assertNotNull("openFormEditorModal method exists", openFormEditorModal);
            
            ctx.assertTrue("FormEditorLauncher API validated", true);
        } catch (NoSuchMethodException e) {
            ctx.fail("Required method not found: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            ctx.fail("FormEditorLauncher class not found: " + e.getMessage());
        }
    }
}
