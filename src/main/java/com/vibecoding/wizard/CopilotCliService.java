/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CopilotCliService {
    private final Path workingDirectory;
    private Process currentProcess = null;

    public CopilotCliService(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ProcessResult runPrimaryTask(TaskType taskType, String prompt) throws IOException, InterruptedException {
        return executePrompt(prompt, "primary " + taskType.name().toLowerCase(), null);
    }

    public ProcessResult runPrimaryTask(TaskType taskType, String prompt, Consumer<String> outputConsumer) 
        throws IOException, InterruptedException {
        return executePrompt(prompt, "primary " + taskType.name().toLowerCase(), outputConsumer);
    }

    /**
     * Run the primary task interactively with bidirectional I/O through callbacks.
     * Returns the process's output stream for sending input.
     */
    public ProcessAndResult runPrimaryTaskInteractive(TaskType taskType, String prompt, Consumer<String> outputConsumer) 
        throws IOException, InterruptedException {
        return executePromptInteractive(prompt, "primary " + taskType.name().toLowerCase(), outputConsumer);
    }

    public ProcessResult runFixAttempt(String prompt, int attemptNumber) throws IOException, InterruptedException {
        return executePrompt(prompt, "fix attempt " + attemptNumber, null);
    }

    public ProcessResult runFixAttempt(String prompt, int attemptNumber, Consumer<String> outputConsumer) 
        throws IOException, InterruptedException {
        return executePrompt(prompt, "fix attempt " + attemptNumber, outputConsumer);
    }

    /**
     * Run a fix attempt interactively with bidirectional I/O through callbacks.
     */
    public ProcessAndResult runFixAttemptInteractive(String prompt, int attemptNumber, Consumer<String> outputConsumer) 
        throws IOException, InterruptedException {
        return executePromptInteractive(prompt, "fix attempt " + attemptNumber, outputConsumer);
    }

    /**
     * Run a finetuning prompt interactively with bidirectional I/O through callbacks.
     */
    public ProcessAndResult runFinetuningInteractive(String prompt, Consumer<String> outputConsumer) 
        throws IOException, InterruptedException {
        return executePromptInteractive(prompt, "finetuning", outputConsumer);
    }

    public OutputStream getCurrentProcessInput() {
        return currentProcess != null ? currentProcess.getOutputStream() : null;
    }

    private ProcessResult executePrompt(String prompt, String context, Consumer<String> outputConsumer)
        throws IOException, InterruptedException {
        if (Boolean.getBoolean("vibecodingwizard.skipCopilotCli")) {
            return new ProcessResult(0, "Skipped Copilot CLI execution for context: " + context, "");
        }
        // Using agent mode for autonomous task execution
        // Build command with --add-dir flags for working directory and its parent
        List<String> command = new ArrayList<>();
        
        // On Windows, npx is a .cmd file that needs to be run through cmd.exe
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            command.add("cmd.exe");
            command.add("/c");
        }
        
        command.add("npx");
        command.add("@github/copilot");
        command.add("-p");
        command.add(prompt);
        command.add("--allow-all-tools");
        // Add the working directory to allowed directories
        command.add("--add-dir");        
        command.add(workingDirectory.toAbsolutePath().toString());
        
        // Also add the parent directory to allow creating the project directory if needed
        Path parentDir = workingDirectory.toAbsolutePath().getParent();
        if (parentDir != null) {
            command.add("--add-dir");
            command.add(parentDir.toString());
        }
        
        command.add("--model");
        command.add("claude-sonnet-4.5");

        try {
            return ProcessRunner.runWithStreaming(command, workingDirectory, null, outputConsumer);
        } catch (IOException ex) {
            String message = "Failed to run GitHub Copilot CLI (" + context + "): " + ex.getMessage();
            throw new IOException(message, ex);
        }
    }

    private ProcessAndResult executePromptInteractive(String prompt, String context, Consumer<String> outputConsumer)
        throws IOException, InterruptedException {
        if (Boolean.getBoolean("vibecodingwizard.skipCopilotCli")) {
            throw new IOException("Cannot run interactive mode in skip mode");
        }
        // Using agent mode for autonomous task execution with interactive I/O
        List<String> command = new ArrayList<>();
        
        // On Windows, npx is a .cmd file that needs to be run through cmd.exe
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            command.add("cmd.exe");
            command.add("/c");
        }
        
        command.add("npx");
        command.add("@github/copilot");
        command.add("-p");
        command.add(prompt);
        command.add("--allow-all-tools");

        // Add the working directory to allowed directories
        command.add("--add-dir");
        command.add(workingDirectory.toAbsolutePath().toString());
        
        // Also add the parent directory to allow creating the project directory if needed
        Path parentDir = workingDirectory.toAbsolutePath().getParent();
        if (parentDir != null) {
            command.add("--add-dir");
            command.add(parentDir.toString());
        }

        command.add("--model");
        command.add("claude-sonnet-4.5");
        
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(workingDirectory.toFile());
            builder.redirectErrorStream(true);
            
            Process process = builder.start();
            currentProcess = process;
            
            // Thread for reading output and sending to consumer
            // Use character-based reading to capture all output including progress indicators
            Thread outputThread = new Thread(() -> {
                try (java.io.InputStreamReader reader = 
                        new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
                    StringBuilder lineBuilder = new StringBuilder();
                    int ch;
                    while ((ch = reader.read()) != -1) {
                        char c = (char) ch;
                        if (c == '\n') {
                            // Complete line - send it
                            if (outputConsumer != null && lineBuilder.length() > 0) {
                                outputConsumer.accept(lineBuilder.toString());
                            }
                            lineBuilder.setLength(0);
                        } else if (c == '\r') {
                            // Carriage return - might be progress indicator, send what we have
                            if (outputConsumer != null && lineBuilder.length() > 0) {
                                outputConsumer.accept(lineBuilder.toString());
                            }
                            lineBuilder.setLength(0);
                        } else {
                            lineBuilder.append(c);
                        }
                    }
                    // Send any remaining content
                    if (outputConsumer != null && lineBuilder.length() > 0) {
                        outputConsumer.accept(lineBuilder.toString());
                    }
                } catch (IOException e) {
                    // Process terminated
                }
            });
            outputThread.start();
            
            return new ProcessAndResult(process, process.getOutputStream(), outputThread);
        } catch (IOException ex) {
            String message = "Failed to run GitHub Copilot CLI (" + context + "): " + ex.getMessage();
            throw new IOException(message, ex);
        }
    }
}
