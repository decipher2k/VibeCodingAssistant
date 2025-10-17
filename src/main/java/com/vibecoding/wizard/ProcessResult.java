/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

public final class ProcessResult {
    private final int exitCode;
    private final String stdout;
    private final String stderr;

    public ProcessResult(int exitCode, String stdout, String stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public boolean isSuccess() {
        return exitCode == 0;
    }

    @Override
    public String toString() {
        return "ProcessResult{" +
            "exitCode=" + exitCode +
            ", stdout='" + stdout + '\'' +
            ", stderr='" + stderr + '\'' +
            '}';
    }
}
