/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;

import com.vibecoding.wizard.TaskExecutionDialog;

public final class TaskExecutionDialogTests {
    private TaskExecutionDialogTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Graphics environment is headless");
            return;
        }

        SwingUtilities.invokeAndWait(() -> {
            TaskExecutionDialog dialog = new TaskExecutionDialog(null);
            try {
                dialog.appendLog("Starting test");
                dialog.setStatus("Running");
                dialog.markCompleted();
                dialog.markFailed();
            } finally {
                dialog.dispose();
            }
        });
    }
}
