/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public final class TaskExecutionDialog extends JDialog {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String AUTH_ERROR_MESSAGE = "No authentication information found.";

    private final JTextArea logArea = new JTextArea();
    private final JTextArea finetuningArea = new JTextArea();
    private final JButton finetuningButton = new JButton("Start Finetuning");
    private final JLabel statusLabel = new JLabel("Ready");
    private final JButton closeButton = new JButton("Close");
    private final JFrame owner;
    private Writer processInput = null;
    private TaskType taskType;
    private InitialConfig config;
    private CopilotCliService copilotCliService;
    private boolean authErrorDetected = false;

    public TaskExecutionDialog(JFrame owner) {
        super(owner, "Performing Task", false);
        this.owner = owner;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(720, 520));
        getContentPane().setBackground(new Color(0xF0F3F7));

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(Color.WHITE);
        logArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Finetuning panel
        finetuningArea.setLineWrap(true);
        finetuningArea.setWrapStyleWord(true);
        finetuningArea.setRows(3);
        finetuningArea.setEnabled(false);
        finetuningArea.setBackground(Color.WHITE);
        finetuningArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDE4ED), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        JScrollPane finetuningScrollPane = new JScrollPane(finetuningArea);
        finetuningScrollPane.setBorder(BorderFactory.createEmptyBorder());

        finetuningButton.setEnabled(false);
        finetuningButton.addActionListener(event -> handleFinetuning());
        
        JButton finetuningHelpButton = UiUtils.createHelpButton(
            "Finetuning",
            "After the initial task completes successfully, you can use this feature to send additional prompts to GitHub Copilot to refine the generated code. " +
            "For example, you can ask to change colors, add features, fix issues, or modify the implementation. " +
            "The build and run instructions will be automatically appended to your prompt.",
            this);

        JPanel finetuningButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        finetuningButtonPanel.setOpaque(false);
        finetuningButtonPanel.add(finetuningButton);
        finetuningButtonPanel.add(finetuningHelpButton);

        JPanel finetuningPanel = new JPanel(new BorderLayout(8, 8));
        finetuningPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        finetuningPanel.setOpaque(false);
        
        JLabel finetuningLabel = new JLabel("Finetuning Prompt:");
        JPanel finetuningLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        finetuningLabelPanel.setOpaque(false);
        finetuningLabelPanel.add(finetuningLabel);
        
        finetuningPanel.add(finetuningLabelPanel, BorderLayout.NORTH);
        finetuningPanel.add(finetuningScrollPane, BorderLayout.CENTER);
        finetuningPanel.add(finetuningButtonPanel, BorderLayout.SOUTH);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        footer.setOpaque(false);
        footer.add(statusLabel, BorderLayout.CENTER);

        closeButton.setEnabled(true);
        closeButton.addActionListener(event -> dispose());
        footer.add(closeButton, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(finetuningPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    public void setProcessInputStream(OutputStream processOutputStream) {
        this.processInput = new OutputStreamWriter(processOutputStream, StandardCharsets.UTF_8);
    }

    public void closeProcessInputStream() {
        if (processInput != null) {
            try {
                processInput.close();
            } catch (Exception ignored) {
            }
            processInput = null;
        }
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + TIME_FORMATTER.format(LocalTime.now()) + "] " + message + '\n');
            logArea.setCaretPosition(logArea.getDocument().getLength());
            
            // Check for authentication error
            if (message.contains(AUTH_ERROR_MESSAGE)) {
                handleAuthenticationError();
            }
        });
    }

    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    public void markCompleted() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Completed");
            closeButton.setEnabled(true);
        });
    }
    
    /**
     * Opens the output directory for the compiled application in the system's file manager.
     * Automatically resolves the output directory based on the programming language.
     * 
     * @param projectDirectory The base project directory
     * @param language The programming language used in the project
     */
    public void openOutputDirectory(java.nio.file.Path projectDirectory, ProgrammingLanguage language) {
        if (projectDirectory == null) {
            appendLog("Cannot open output directory: project directory is null");
            return;
        }
        
        // Resolve the actual output directory based on the language
        java.nio.file.Path outputDirectory = OutputDirectoryResolver.getOutputDirectory(projectDirectory, language);
        
        if (outputDirectory == null || !java.nio.file.Files.exists(outputDirectory)) {
            appendLog("Output directory does not exist yet: " + 
                     (outputDirectory != null ? outputDirectory.toAbsolutePath() : "null"));
            appendLog("Opening project directory instead: " + projectDirectory.toAbsolutePath());
            outputDirectory = projectDirectory;
        }
        
        final java.nio.file.Path dirToOpen = outputDirectory;
        SwingUtilities.invokeLater(() -> {
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                        appendLog("Opening output directory: " + dirToOpen.toAbsolutePath());
                        desktop.open(dirToOpen.toFile());
                    } else {
                        appendLog("Desktop OPEN action not supported on this system");
                    }
                } else {
                    appendLog("Desktop operations not supported on this system");
                }
            } catch (Exception ex) {
                appendLog("Failed to open directory: " + ex.getMessage());
            }
        });
    }

    public void markFailed() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Failed (see log)");
            closeButton.setEnabled(true);
        });
    }

    public void enableFinetuning(TaskType taskType, InitialConfig config, CopilotCliService copilotCliService) {
        this.taskType = taskType;
        this.config = config;
        this.copilotCliService = copilotCliService;
        SwingUtilities.invokeLater(() -> {
            finetuningArea.setEnabled(true);
            finetuningButton.setEnabled(true);
            appendLog("\n=== Finetuning enabled ===");
            appendLog("You can now enter additional prompts to refine the generated code.");
        });
    }

    private void handleFinetuning() {
        String userPrompt = finetuningArea.getText().trim();
        if (userPrompt.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a finetuning prompt.",
                "Empty Prompt",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Disable button and textarea during execution
        finetuningButton.setEnabled(false);
        finetuningArea.setEnabled(false);

        // Run finetuning in background
        new Thread(() -> {
            try {
                appendLog("\n=== Starting finetuning ===");
                appendLog("User prompt: " + userPrompt);
                
                String prompt = PromptBuilder.buildFinetuningPrompt(taskType, config, userPrompt);
                appendLog("Finetuning prompt generated.");
                
                setStatus("Running finetuning via GitHub Copilot CLI...");
                appendLog("--- Copilot CLI running finetuning (interactive mode) ---");
                
                ProcessAndResult processAndResult = copilotCliService.runFinetuningInteractive(
                    prompt, line -> appendLog(line));
                
                setProcessInputStream(processAndResult.getProcessInput());
                ProcessResult result = processAndResult.waitForCompletion();
                closeProcessInputStream();
                
                appendLog("--- Copilot CLI finetuning completed ---");
                appendLog("Finetuning exit code: " + result.getExitCode());
                
                if (result.isSuccess()) {
                    appendLog("Finetuning completed successfully.");
                    
                    // Build and run the modified application
                    appendLog("\n=== Building and testing modified application ===");
                    BuildCommandPlanner.BuildPlan plan = BuildCommandPlanner.plan(
                        config.getProgrammingLanguage(),
                        config.getProjectStyle(),
                        config);
                    
                    appendLog("Build plan: " + plan.getDescription());
                    setStatus("Building modified application...");
                    
                    // Use the project directory if specified
                    java.nio.file.Path workingDirectory = config.getProjectDirectory() != null 
                        ? config.getProjectDirectory() 
                        : java.nio.file.Path.of("").toAbsolutePath();
                    
                    ProcessResult buildResult = runBuildCommands(plan.getCommands(), workingDirectory);
                    
                    if (buildResult.isSuccess()) {
                        appendLog("Build succeeded.");
                        
                        // Run the application to test it
                        appendLog("\n=== Launching application for testing ===");
                        setStatus("Launching application...");
                        
                        java.util.List<String> runCommand = BuildCommandPlanner.getRunCommand(
                            config.getProgrammingLanguage(),
                            config.getProjectStyle(),
                            config);
                        
                        if (runCommand != null && !runCommand.isEmpty()) {
                            appendLog("Run command: " + String.join(" ", runCommand));
                            appendLog("Working directory: " + workingDirectory.toAbsolutePath());
                            
                            // Run with streaming output for real-time feedback
                            ProcessResult runResult = ProcessRunner.runWithStreaming(runCommand, workingDirectory, null,
                                line -> appendLog(line));
                            appendLog("Application launched (exit code: " + runResult.getExitCode() + ")");
                            
                            if (!runResult.getStdout().isBlank()) {
                                appendLog("Output:\n" + runResult.getStdout());
                            }
                            if (!runResult.getStderr().isBlank()) {
                                appendLog("Errors:\n" + runResult.getStderr());
                            }
                            
                            setStatus("Finetuning completed - Application launched");
                        } else {
                            appendLog("No run command available for this project type.");
                            setStatus("Finetuning completed - Build successful");
                        }
                    } else {
                        appendLog("Build failed after finetuning.");
                        appendLog("Build stdout:\n" + buildResult.getStdout());
                        appendLog("Build stderr:\n" + buildResult.getStderr());
                        setStatus("Build failed after finetuning");
                    }
                } else {
                    appendLog("Finetuning failed with exit code: " + result.getExitCode());
                    setStatus("Finetuning failed");
                }
                
            } catch (Exception ex) {
                appendLog("Finetuning error: " + ex.getMessage());
                setStatus("Finetuning error");
            } finally {
                // Re-enable controls
                SwingUtilities.invokeLater(() -> {
                    finetuningButton.setEnabled(true);
                    finetuningArea.setEnabled(true);
                });
            }
        }).start();
    }
    
    private ProcessResult runBuildCommands(java.util.List<java.util.List<String>> commands, java.nio.file.Path workingDirectory) throws Exception {
        ProcessResult lastResult = new ProcessResult(0, "", "");
        for (java.util.List<String> command : commands) {
            appendLog("Running: " + String.join(" ", command));
            appendLog("Working directory: " + workingDirectory.toAbsolutePath());
            // Stream output to dialog for real-time feedback
            ProcessResult result = ProcessRunner.runWithStreaming(command, workingDirectory, null, 
                line -> appendLog(line));
            appendLog("Exit code: " + result.getExitCode());
            if (!result.getStdout().isBlank()) {
                appendLog("Stdout:\n" + result.getStdout());
            }
            if (!result.getStderr().isBlank()) {
                appendLog("Stderr:\n" + result.getStderr());
            }
            lastResult = result;
            if (!result.isSuccess()) {
                break;
            }
        }
        return lastResult;
    }
    
    private void handleAuthenticationError() {
        // Only handle once
        if (authErrorDetected) {
            return;
        }
        authErrorDetected = true;
        
        appendLog("\n=== Authentication Error Detected ===");
        appendLog("GitHub Copilot is not authenticated.");
        
        // Delete the AUTH file from StartupChecker
        try {
            java.nio.file.Path authFile = java.nio.file.Paths.get(System.getProperty("user.dir"), "AUTH");
            java.nio.file.Files.deleteIfExists(authFile);
            appendLog("Removed cached authentication status.");
        } catch (Exception e) {
            appendLog("Warning: Could not remove AUTH file: " + e.getMessage());
        }
        
        // Show authentication dialog
        SwingUtilities.invokeLater(() -> {
            appendLog("Opening authentication dialog...");
            CopilotAuthDialog authDialog = new CopilotAuthDialog(owner);
            authDialog.setVisible(true);
        });
    }
}
