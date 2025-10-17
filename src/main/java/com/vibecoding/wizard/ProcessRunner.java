/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.Map;

public final class ProcessRunner {
    private ProcessRunner() {
    }

    public static ProcessResult run(List<String> command, Path workingDirectory, String stdin)
        throws IOException, InterruptedException {
        return runWithStreaming(command, workingDirectory, stdin, null);
    }

    public static ProcessResult runWithStreaming(List<String> command, Path workingDirectory, String stdin,
                                                  Consumer<String> outputConsumer)
        throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> env = builder.environment();
        env.put("WINEDEBUG", "-all");
        if (workingDirectory != null) {
            builder.directory(workingDirectory.toFile());
        }
        builder.redirectErrorStream(true); // Merge stdout and stderr for live streaming
        Process process = builder.start();

        if (stdin != null && !stdin.isEmpty()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(stdin);
                writer.flush();
            }
        } else {
            process.getOutputStream().close();
        }

        String output;
        if (outputConsumer != null) {
            output = readStreamWithCallback(process.getInputStream(), outputConsumer);
        } else {
            output = readStream(process.getInputStream());
        }
        
        int exitCode = process.waitFor();
        return new ProcessResult(exitCode, output, "");
    }

    /**
     * Run a command with bidirectional I/O: streaming output to a consumer and accepting input from a supplier.
     * This allows interactive communication with the subprocess through the provided callbacks.
     */
    public static ProcessResult runInteractiveWithCallbacks(
            List<String> command, 
            Path workingDirectory,
            Consumer<String> outputConsumer,
            java.util.function.Supplier<String> inputSupplier)
        throws IOException, InterruptedException {
        
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            builder.directory(workingDirectory.toFile());
        }
        builder.redirectErrorStream(true);
        Process process = builder.start();

        // Thread for reading output and sending to consumer
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (outputConsumer != null) {
                        outputConsumer.accept(line);
                    }
                }
            } catch (IOException e) {
                // Process terminated
            }
        });
        outputThread.start();

        // Keep output stream open for interactive input
        // The inputSupplier will be called from the UI when user provides input
        
        int exitCode = process.waitFor();
        outputThread.join();
        
        return new ProcessResult(exitCode, "", "");
    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        }
        return builder.toString().trim();
    }

    private static String readStreamWithCallback(InputStream stream, Consumer<String> callback) throws IOException {
        StringBuilder builder = new StringBuilder();
        StringBuilder lineBuilder = new StringBuilder();
        
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            int ch;
            while ((ch = reader.read()) != -1) {
                char c = (char) ch;
                builder.append(c);
                
                if (c == '\n') {
                    // Complete line - send it
                    if (callback != null && lineBuilder.length() > 0) {
                        callback.accept(lineBuilder.toString());
                    }
                    lineBuilder.setLength(0);
                } else if (c == '\r') {
                    // Carriage return - might be progress indicator, send what we have
                    if (callback != null && lineBuilder.length() > 0) {
                        callback.accept(lineBuilder.toString());
                    }
                    lineBuilder.setLength(0);
                } else {
                    lineBuilder.append(c);
                }
            }
            
            // Send any remaining content
            if (callback != null && lineBuilder.length() > 0) {
                callback.accept(lineBuilder.toString());
            }
        }
        return builder.toString().trim();
    }
}
