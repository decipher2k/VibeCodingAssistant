/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public final class CopilotAuthDialog extends JDialog {
    private boolean authLaunched = false;

    public CopilotAuthDialog(JFrame owner) {
        super(owner, "GitHub Copilot Authentication Required", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("🔐 GitHub Copilot CLI Authentication");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Description
        JTextArea descriptionArea = new JTextArea(
            "GitHub Copilot CLI is installed but not authenticated.\n\n" +
            "To use GitHub Copilot CLI, you need to:\n" +
            "1. Have a GitHub account\n" +
            "2. Have an active GitHub Copilot Pro subscription\n" +
            "3. Authenticate the CLI with your GitHub account"
        );
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(12f));
        descriptionArea.setAlignmentX(LEFT_ALIGNMENT);
        descriptionArea.setMaximumSize(new java.awt.Dimension(500, 120));
        contentPanel.add(descriptionArea);
        contentPanel.add(Box.createVerticalStrut(15));

        // Registration info
        JLabel subscriptionLabel = new JLabel("Don't have GitHub Copilot yet?");
        subscriptionLabel.setFont(subscriptionLabel.getFont().deriveFont(Font.BOLD, 12f));
        subscriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(subscriptionLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        // Clickable link for GitHub Copilot Pro
        JButton registerLink = new JButton("<html><u>Register for GitHub Copilot (including Pro)</u></html>");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setFocusPainted(false);
        registerLink.setForeground(new java.awt.Color(0, 102, 204));
        registerLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        registerLink.setAlignmentX(LEFT_ALIGNMENT);
        registerLink.setHorizontalAlignment(SwingConstants.LEFT);
        registerLink.addActionListener(e -> {
            System.out.println("DEBUG: Register button clicked!");
            openCopilotWebsite();
        });
        contentPanel.add(registerLink);
        contentPanel.add(Box.createVerticalStrut(20));

        // Authentication instructions
        JLabel authLabel = new JLabel("Ready to authenticate?");
        authLabel.setFont(authLabel.getFont().deriveFont(Font.BOLD, 12f));
        authLabel.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(authLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JTextArea authInstructions = new JTextArea(
            "Click the button below to launch the authentication process.\n" +
            "Run the '/login' command.\n" +
            "A browser window will open where you can log in with your GitHub account.\n" +
            "After successful authentication, return here and click OK."
        );
        authInstructions.setEditable(false);
        authInstructions.setLineWrap(true);
        authInstructions.setWrapStyleWord(true);
        authInstructions.setOpaque(false);
        authInstructions.setFont(authInstructions.getFont().deriveFont(12f));
        authInstructions.setAlignmentX(LEFT_ALIGNMENT);
        authInstructions.setMaximumSize(new java.awt.Dimension(500, 80));
        contentPanel.add(authInstructions);

        add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton launchAuthButton = new JButton("Launch GitHub Copilot CLI to Authenticate");
        launchAuthButton.addActionListener(e -> {
            authLaunched = true;
            launchAuthentication();
            launchAuthButton.setEnabled(false);
            launchAuthButton.setText("Authentication Launched");
        });
        buttonPanel.add(launchAuthButton);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            // Show restart message
            int result = JOptionPane.showConfirmDialog(
                this,
                "Please restart \"Vibe Coding Assistant\"",
                "Restart Required",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // If user clicks OK, exit the application
            if (result == JOptionPane.OK_OPTION) {
                System.exit(0);
            }
        });
        buttonPanel.add(okButton);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new java.awt.Dimension(550, 450));
        setLocationRelativeTo(owner);
    }

    private void openCopilotWebsite() {
        String url = "https://github.com/features/copilot";
        System.out.println("DEBUG: openCopilotWebsite called");
        
        try {
            String os = System.getProperty("os.name").toLowerCase();
            System.out.println("DEBUG: Operating System: " + os);
            
            // Try Desktop API first - it's the most reliable when it works
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                System.out.println("DEBUG: Desktop API is supported");
                
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    System.out.println("DEBUG: BROWSE action is supported");
                    try {
                        URI uri = new URI(url);
                        System.out.println("DEBUG: URI created: " + uri);
                        desktop.browse(uri);
                        System.out.println("DEBUG: Desktop.browse() called successfully");
                        
                        // Give it a moment to launch
                        Thread.sleep(500);
                        System.out.println("DEBUG: Browser should have opened");
                        return;
                    } catch (Exception e) {
                        System.err.println("DEBUG: Desktop API browse failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("DEBUG: BROWSE action not supported");
                }
            } else {
                System.out.println("DEBUG: Desktop API not supported");
            }
            
            // Fallback to OS-specific commands
            System.out.println("DEBUG: Trying OS-specific command fallback");
            
            if (os.contains("win")) {
                System.out.println("DEBUG: Attempting Windows-specific methods");
                
                // Method 1: cmd /c start with proper quoting
                try {
                    String[] cmd = {"cmd.exe", "/c", "start", "\"Browser\"", "\"" + url + "\""};
                    System.out.println("DEBUG: Executing: " + String.join(" ", cmd));
                    Process p = Runtime.getRuntime().exec(cmd);
                    int exitCode = p.waitFor();
                    System.out.println("DEBUG: cmd /c start exit code: " + exitCode);
                    if (exitCode == 0) {
                        System.out.println("DEBUG: cmd /c start succeeded");
                        return;
                    }
                } catch (Exception e1) {
                    System.err.println("DEBUG: cmd /c start failed: " + e1.getMessage());
                    e1.printStackTrace();
                }
                
                // Method 2: rundll32
                try {
                    String[] cmd = {"rundll32", "url.dll,FileProtocolHandler", url};
                    System.out.println("DEBUG: Executing: " + String.join(" ", cmd));
                    Process p = Runtime.getRuntime().exec(cmd);
                    int exitCode = p.waitFor();
                    System.out.println("DEBUG: rundll32 exit code: " + exitCode);
                    if (exitCode == 0) {
                        System.out.println("DEBUG: rundll32 succeeded");
                        return;
                    }
                } catch (Exception e2) {
                    System.err.println("DEBUG: rundll32 failed: " + e2.getMessage());
                    e2.printStackTrace();
                }
                
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[] {"open", url});
                return;
            } else {
                Runtime.getRuntime().exec(new String[] {"xdg-open", url});
                return;
            }
            
            // If we get here, nothing worked
            throw new IOException("All methods to open browser failed");
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to open browser: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Show error to user with the URL they can copy
            JOptionPane.showMessageDialog(
                this,
                "Unable to open browser automatically.\n\n" +
                "This may be due to system restrictions or browser configuration.\n\n" +
                "Please manually open your browser and visit:\n" + url + "\n\n" +
                "You can copy this URL from the console output.",
                "Browser Not Opened",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void launchAuthentication() {
        new Thread(() -> {
            try {
                ProcessBuilder pb;
                String os = System.getProperty("os.name").toLowerCase();
                
                // On Windows, we need to launch a new terminal window
                if (os.contains("win")) {
                    // Use 'start' to open a new command prompt window for the authentication
                    pb = new ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/k", 
                        "npx @github/copilot");
                } else if (os.contains("mac")) {
                    // On macOS, open a new Terminal window
                    pb = new ProcessBuilder("osascript", "-e", 
                        "tell application \"Terminal\" to do script \"npx @github/copilot\"");
                } else {
                    // On Linux, try various terminal emulators with proper command syntax
                    boolean launched = false;
                    
                    // Try gnome-terminal (Ubuntu, Fedora, etc.)
                    try {
                        pb = new ProcessBuilder("gnome-terminal", "--", "npx", "@github/copilot");
                        pb.start();
                        launched = true;
                        System.out.println("Launched GitHub Copilot in gnome-terminal");
                    } catch (IOException e) {
                        // Try next terminal
                    }
                    
                    // Try konsole (KDE)
                    if (!launched) {
                        try {
                            pb = new ProcessBuilder("konsole", "-e", "npx", "@github/copilot");
                            pb.start();
                            launched = true;
                            System.out.println("Launched GitHub Copilot in konsole");
                        } catch (IOException e) {
                            // Try next terminal
                        }
                    }
                    
                    // Try xfce4-terminal (XFCE)
                    if (!launched) {
                        try {
                            pb = new ProcessBuilder("xfce4-terminal", "-e", "npx @github/copilot");
                            pb.start();
                            launched = true;
                            System.out.println("Launched GitHub Copilot in xfce4-terminal");
                        } catch (IOException e) {
                            // Try next terminal
                        }
                    }
                    
                    // Try xterm (fallback, usually available)
                    if (!launched) {
                        try {
                            pb = new ProcessBuilder("xterm", "-e", "npx", "@github/copilot");
                            pb.start();
                            launched = true;
                            System.out.println("Launched GitHub Copilot in xterm");
                        } catch (IOException e) {
                            // Try next terminal
                        }
                    }
                    
                    // Terminal was launched successfully, return
                    return;
                }
                
                // For Windows/Mac: start the process in a new terminal window
                Process process = pb.start();
                
                // For Windows/Mac, the terminal window handles the interaction
                // For Linux fallback, we need to wait
                if (!os.contains("win") && !os.contains("mac")) {
                    process.waitFor();
                }
                
            } catch (IOException | InterruptedException e) {
                System.err.println("Error launching GitHub Copilot authentication: " + e.getMessage());
                e.printStackTrace();
                
                // Show error to user
                javax.swing.SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        CopilotAuthDialog.this,
                        "Failed to launch authentication.\n\n" +
                        "Please run this command manually in a terminal:\n" +
                        "npx @github/copilot auth login",
                        "Authentication Launch Failed",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }).start();
    }

    public boolean wasAuthLaunched() {
        return authLaunched;
    }
}
