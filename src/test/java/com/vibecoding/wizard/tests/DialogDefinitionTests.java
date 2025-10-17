/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.DialogDefinition;

public final class DialogDefinitionTests {
    private DialogDefinitionTests() {
    }

    public static void run(TestContext ctx) {
        DialogDefinition dialog = new DialogDefinition("Login", "Login Window", "Collects credentials");
        ctx.assertEquals("Name getter", "Login", dialog.getName());
        ctx.assertEquals("Window title getter", "Login Window", dialog.getWindowTitle());
        ctx.assertEquals("Description getter", "Collects credentials", dialog.getDescription());
        ctx.assertEquals("toString", "Login", dialog.toString());

        DialogDefinition renamed = dialog.withName("SignIn");
        ctx.assertEquals("withName", "SignIn", renamed.getName());
        ctx.assertEquals("withName preserves title", "Login Window", renamed.getWindowTitle());

        DialogDefinition retitled = dialog.withWindowTitle("Sign In Window");
        ctx.assertEquals("withWindowTitle", "Sign In Window", retitled.getWindowTitle());

        DialogDefinition reworded = dialog.withDescription("Collects user credentials securely");
        ctx.assertEquals("withDescription", "Collects user credentials securely", reworded.getDescription());

        ctx.assertTrue("equals identical", dialog.equals(new DialogDefinition("Login", "Login Window", "Collects credentials")));
        ctx.assertTrue("hashCode identical",
            dialog.hashCode() == new DialogDefinition("Login", "Login Window", "Collects credentials").hashCode());
        ctx.assertFalse("equals different name", dialog.equals(new DialogDefinition("Foo", "Login Window", "Collects credentials")));
    }
}
