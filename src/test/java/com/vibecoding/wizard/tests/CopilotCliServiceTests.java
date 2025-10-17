/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.io.IOException;
import java.nio.file.Path;

import com.vibecoding.wizard.CopilotCliService;
import com.vibecoding.wizard.TaskType;

public final class CopilotCliServiceTests {
    private CopilotCliServiceTests() {
    }

    public static void run(TestContext ctx) {
        System.setProperty("vibecodingwizard.skipCopilotCli", "true");
        CopilotCliService service = new CopilotCliService(Path.of("."));
        try {
            ctx.assertEquals("Primary task skip", 0,
                service.runPrimaryTask(TaskType.GENERATE_APP_OR_SCRIPT, "prompt").getExitCode());
            ctx.assertEquals("Fix attempt skip", 0, service.runFixAttempt("prompt", 1).getExitCode());
        } catch (IOException | InterruptedException ex) {
            ctx.fail("Copilot CLI execution should be skipped in tests");
        } finally {
            System.clearProperty("vibecodingwizard.skipCopilotCli");
        }
    }
}
