/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.OutputStream;

public final class ProcessAndResult {
    private final Process process;
    private final OutputStream processInput;
    private final Thread outputThread;

    public ProcessAndResult(Process process, OutputStream processInput, Thread outputThread) {
        this.process = process;
        this.processInput = processInput;
        this.outputThread = outputThread;
    }

    public Process getProcess() {
        return process;
    }

    public OutputStream getProcessInput() {
        return processInput;
    }

    public ProcessResult waitForCompletion() throws InterruptedException {
        int exitCode = process.waitFor();
        if (outputThread != null) {
            outputThread.join();
        }
        return new ProcessResult(exitCode, "", "");
    }
}
