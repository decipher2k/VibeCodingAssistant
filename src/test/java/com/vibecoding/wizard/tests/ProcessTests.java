/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.nio.file.Path;
import java.util.List;

import com.vibecoding.wizard.ProcessResult;
import com.vibecoding.wizard.ProcessRunner;

public final class ProcessTests {
    private ProcessTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        ProcessResult success = ProcessRunner.run(List.of("sh", "-c", "echo hello"), Path.of("."), null);
        ctx.assertEquals("Success exit code", 0, success.getExitCode());
        ctx.assertEquals("Success stdout", "hello", success.getStdout());
        ctx.assertEquals("Success stderr", "", success.getStderr());
        ctx.assertTrue("isSuccess", success.isSuccess());

        // Note: ProcessRunner redirects stderr to stdout, so stderr is always empty
        // and error messages appear in stdout
        ProcessResult failure = ProcessRunner.run(List.of("sh", "-c", "echo error 1>&2; exit 5"), Path.of("."), null);
        ctx.assertEquals("Failure exit code", 5, failure.getExitCode());
        ctx.assertTrue("Error in stdout", failure.getStdout().contains("error"));
        ctx.assertEquals("Failure stderr always empty", "", failure.getStderr());
        ctx.assertFalse("isSuccess false", failure.isSuccess());
    }
}
