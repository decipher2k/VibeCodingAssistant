/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;

import com.vibecoding.wizard.VibeCodingWizardApp;

public final class WizardAppHeadlessTests {
    private WizardAppHeadlessTests() {
    }

    public static void run(TestContext ctx) throws InterruptedException {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Graphics environment is headless");
            return;
        }

        Thread thread = new Thread(() -> VibeCodingWizardApp.main(new String[0]));
        thread.start();
        thread.join(1000L);

        // Close any frames that may have opened.
        Frame[] frames = Frame.getFrames();
        for (Frame frame : frames) {
            frame.dispose();
        }

        ctx.assertTrue("Application main thread completed", !thread.isAlive());
    }
}
