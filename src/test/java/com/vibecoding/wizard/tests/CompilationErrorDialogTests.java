/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;

import com.vibecoding.wizard.CompilationErrorDialog;

public final class CompilationErrorDialogTests {
    private CompilationErrorDialogTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Graphics environment is headless");
            return;
        }

        SwingUtilities.invokeAndWait(() -> {
            CompilationErrorDialog dialog = new CompilationErrorDialog(null, "Error details");
            try {
                dialog.setVisible(false);
            } finally {
                dialog.dispose();
            }
        });
    }
}
