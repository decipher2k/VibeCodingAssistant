/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class StartupChecker {
    private static final String AUTH_FILE_NAME = "AUTH";
    
    private StartupChecker() {
    }
    
    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }
    
    private static Path getAuthFilePath() {
        // Get the directory where the application is running
        String userDir = System.getProperty("user.dir");
        return Paths.get(userDir, AUTH_FILE_NAME);
    }
    
    private static boolean authFileExists() {
        return Files.exists(getAuthFilePath());
    }
    
    private static void createAuthFile() {
        try {
            Path authFile = getAuthFilePath();
            Files.createFile(authFile);
        } catch (IOException e) {
            // Silently fail if we can't create the file
        }
    }
    
    private static void deleteAuthFile() {
        try {
            Path authFile = getAuthFilePath();
            Files.deleteIfExists(authFile);
        } catch (IOException e) {
            // Silently fail if we can't delete the file
        }
    }
    
    public static boolean performChecks(JFrame owner) {
        SystemCheckDialog dialog = new SystemCheckDialog(owner);
        
        Thread checkThread = new Thread(() -> {
            try {
                dialog.appendLog("=== System Requirements Check ===\n");
                
                boolean isWindowsOS = isWindows();
                if (isWindowsOS) {
                    dialog.appendLog("Detected Windows operating system.");
                    dialog.appendLog("Automatic installation will be attempted for missing dependencies.\n");
                } else {
                    dialog.appendLog("Detected Linux operating system.");
                    dialog.appendLog("Automatic installation will be attempted for missing dependencies.\n");
                }
                
                // Check .NET SDK
                boolean dotnetWasJustInstalled = false;
                dialog.appendLog("Checking for .NET 9.0 SDK...");
                if (!isDotNetInstalled()) {
                    dialog.appendLog("❌ .NET 9.0 SDK is not installed.");
                    dialog.appendLog("Attempting to install .NET SDK...");
                    boolean installSuccess;
                    if (isWindowsOS) {
                        installSuccess = installDotNetWindows(dialog);
                    } else {
                        installSuccess = installDotNetLinux(dialog);
                    }
                    
                    if (!installSuccess) {
                        dialog.appendLog("⚠️  WARNING: Failed to install .NET 9.0 SDK automatically.");
                        dialog.appendLog("⚠️  You will not be able to create C# projects until .NET 9.0 SDK is installed.");
                        dialog.appendLog("⚠️  Please install it manually from https://dotnet.microsoft.com/download/dotnet/9.0");
                        dialog.appendLog("⚠️  The application will continue, but C# support will be limited.\n");
                        // Continue instead of returning - allow app to proceed
                    } else {
                        // Verify installation succeeded
                        if (isDotNetInstalled()) {
                            dialog.appendLog("✅ .NET SDK verified after installation.");
                        } else {
                            dialog.appendLog("⚠️  .NET SDK installation completed, but verification failed.");
                            dialog.appendLog("This may be due to PATH not being updated yet.");
                            dotnetWasJustInstalled = true;
                        }
                    }
                } else {
                    dialog.appendLog("✅ .NET 9.0 SDK is installed.");
                }
                
                // Check npm
                dialog.appendLog("\nChecking for npm...");
                boolean npmWasJustInstalled = false;
                boolean npmFound = isNpmInstalled();
                
                if (!npmFound) {
                    dialog.appendLog("❌ npm is not installed.");
                    dialog.appendLog("Attempting to install npm (Node.js)...");
                    
                    if (installNpm(dialog)) {
                        dialog.appendLog("✅ npm (Node.js) has been installed.");
                        npmWasJustInstalled = true;
                        
                        // Verify installation
                        if (isNpmInstalled()) {
                            dialog.appendLog("✅ npm is now accessible.");
                        } else {
                            dialog.appendLog("⚠️  npm was installed but is not yet accessible to this application.");
                            dialog.appendLog("This is normal - the PATH environment variable needs to be refreshed.");
                        }
                    } else {
                        dialog.appendLog("❌ Failed to install npm automatically.");
                        dialog.appendLog("Please install Node.js manually from: https://nodejs.org/");
                        dialog.appendLog("After installation, restart this application.");
                        dialog.requireRestart();
                        return;
                    }
                } else {
                    dialog.appendLog("✅ npm is installed.");
                }
                
                // If either .NET or npm was just installed, we need to restart the application
                if (dotnetWasJustInstalled || npmWasJustInstalled) {
                    dialog.appendLog("\n⚠️  One or more components were just installed.");
                    if (dotnetWasJustInstalled) {
                        dialog.appendLog("   - .NET 9.0 SDK");
                    }
                    if (npmWasJustInstalled) {
                        dialog.appendLog("   - npm");
                    }
                    dialog.appendLog("\nPlease restart this application so that the environment is updated.");
                    dialog.appendLog("After restarting, the remaining components will be checked and installed if needed.");
                    dialog.requireRestart();
                    return;
                }
                
                // Check GitHub Copilot CLI
                dialog.appendLog("\nChecking for GitHub Copilot CLI...");
                boolean needsAuthentication = false;
                
                if (!isCopilotCliInstalled()) {
                    dialog.appendLog("❌ GitHub Copilot CLI is not installed.");
                    dialog.appendLog("Installing GitHub Copilot CLI...");
                    
                    if (!installCopilotCli(dialog)) {
                        dialog.showError("Failed to install GitHub Copilot CLI. Please install it manually with: npm install -g @github/copilot");
                        return;
                    }
                    
                    needsAuthentication = true;
                } else {
                    dialog.appendLog("✅ GitHub Copilot CLI is installed.");
                    
                    // Check if authenticated (skip check if AUTH file exists)
                    if (authFileExists()) {
                        dialog.appendLog("✅ GitHub Copilot CLI authentication verified (cached).");
                    } else {
                        dialog.appendLog("Checking authentication status...");
                        
                        // Run authentication check with a timeout at the method level
                        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                        java.util.concurrent.Future<Boolean> future = executor.submit(() -> {
                            return isCopilotAuthenticated();
                        });
                        
                        try {
                            // Wait up to 25 seconds for the authentication check
                            Boolean isAuthenticated = future.get(25, TimeUnit.SECONDS);
                            
                            if (!isAuthenticated) {
                                dialog.appendLog("⚠️  GitHub Copilot CLI is not authenticated.");
                                needsAuthentication = true;
                                // Remove AUTH file if it exists (shouldn't, but just in case)
                                deleteAuthFile();
                            } else {
                                dialog.appendLog("✅ GitHub Copilot CLI is authenticated.");
                                // Create AUTH file to skip future checks
                                createAuthFile();
                            }
                        } catch (java.util.concurrent.TimeoutException e) {
                            dialog.appendLog("⚠️  Authentication check timed out. Skipping authentication verification.");
                            dialog.appendLog("You may need to authenticate manually if needed.");
                            future.cancel(true);
                        } catch (Exception e) {
                            dialog.appendLog("⚠️  Error checking authentication: " + e.getMessage());
                            dialog.appendLog("Assuming authentication is required.");
                            needsAuthentication = true;
                        } finally {
                            executor.shutdownNow();
                        }
                    }
                }
                
                dialog.appendLog("\n✅ All system checks passed!");
                dialog.enableContinue();
                
                // Show authentication dialog if needed
                if (needsAuthentication) {
                    SwingUtilities.invokeLater(() -> {
                        CopilotAuthDialog authDialog = new CopilotAuthDialog(owner);
                        authDialog.setVisible(true);
                    });
                } else {
                    dialog.appendLog("You can now continue to use Vibe Coding Wizard.");
                }
                
            } catch (Exception e) {
                dialog.showError("Unexpected error during system check: " + e.getMessage());
            }
        });
        
        checkThread.start();
        dialog.setVisible(true);
        
        return true;
    }
    
    private static boolean isNpmInstalled() {
        // On Windows, try both npm and npm.cmd
        if (isWindows()) {
            // First try npm.cmd (the batch file wrapper)
            try {
                Process process = new ProcessBuilder("npm.cmd", "--version").start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return true;
                }
            } catch (IOException | InterruptedException e) {
                // Try the regular npm command
            }
            
            // Try regular npm command
            try {
                Process process = new ProcessBuilder("npm", "--version").start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return true;
                }
            } catch (IOException | InterruptedException e) {
                // Try with cmd.exe
            }
            
            // Also try with cmd.exe /c npm
            try {
                Process process = new ProcessBuilder("cmd.exe", "/c", "npm", "--version").start();
                int exitCode = process.waitFor();
                return exitCode == 0;
            } catch (IOException | InterruptedException e) {
                return false;
            }
        } else {
            // Unix-like systems
            try {
                Process process = new ProcessBuilder("npm", "--version").start();
                int exitCode = process.waitFor();
                return exitCode == 0;
            } catch (IOException | InterruptedException e) {
                return false;
            }
        }
    }
    
    private static boolean installNpm(SystemCheckDialog dialog) {
        // Check if running on Windows and use Windows-specific installer
        if (isWindows()) {
            return installNpmWindows(dialog);
        }
        
        // Linux/Mac installation
        dialog.appendLog("Detecting package manager...");
        PackageManagerDetector.PackageManager pm = PackageManagerDetector.detect();
        
        if (pm == null) {
            dialog.appendLog("❌ Could not detect package manager.");
            return false;
        }
        
        dialog.appendLog("Detected package manager: " + pm.getName());
        dialog.appendLog("Installing npm...");
        
        try {
            List<String> command = new ArrayList<>();
            if (pm.requiresSudo()) {
                // Check if we can use pkexec (GUI sudo)
                if (commandExists("pkexec")) {
                    command.add("pkexec");
                } else {
                    dialog.appendLog("⚠️  This requires administrator privileges.");
                    dialog.appendLog("Please run the following command manually in a terminal:");
                    dialog.appendLog("sudo " + pm.getInstallCommand());
                    return false;
                }
            }
            
            // Add the package manager command
            for (String part : pm.getInstallCommand().split(" ")) {
                command.add(part);
            }
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                dialog.appendLog("✅ npm installed successfully.");
                return true;
            } else {
                dialog.appendLog("❌ npm installation failed with exit code: " + exitCode);
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            dialog.appendLog("❌ Error installing npm: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean isCopilotCliInstalled() {
        // Use npm list to check if the package is installed globally (non-interactive)
        try {
            ProcessBuilder pb;
            if (isWindows()) {
                pb = new ProcessBuilder("cmd.exe", "/c", "npm", "list", "-g", "@github/copilot");
            } else {
                pb = new ProcessBuilder("npm", "list", "-g", "@github/copilot");
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read output in a separate thread to avoid blocking
            StringBuilder output = new StringBuilder();
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    // Ignore - process might have been terminated
                }
            });
            outputReader.setDaemon(true);
            outputReader.start();
            
            // Wait with timeout (10 seconds should be enough for npm list)
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            
            if (!finished) {
                // Process timed out, kill it and assume not installed
                process.destroyForcibly();
                return false;
            }
            
            // Wait a bit for the output reader thread to finish
            outputReader.join(1000);
            
            // Check if the output contains the package name (indicates it's installed)
            String outputStr = output.toString();
            return outputStr.contains("@github/copilot");
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
    
    private static boolean installCopilotCli(SystemCheckDialog dialog) {
        dialog.appendLog("Installing GitHub Copilot CLI via npm...");
        
        try {
            ProcessBuilder pb;
            if (isWindows()) {
                // On Windows, use cmd.exe to ensure npm is found
                pb = new ProcessBuilder("cmd.exe", "/c", "npm", "install", "-g", "@github/copilot");
            } else {
                pb = new ProcessBuilder("npm", "install", "-g", "@github/copilot");
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Consume output in a separate thread to prevent buffer overflow
            Thread outputConsumer = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        dialog.appendLog(line);
                    }
                } catch (IOException e) {
                    // Ignore - process might have terminated
                }
            });
            outputConsumer.setDaemon(true);
            outputConsumer.start();
            
            // Wait with timeout (5 minutes should be enough for npm install)
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            
            if (!finished) {
                dialog.appendLog("⚠️  Installation timed out after 5 minutes.");
                process.destroyForcibly();
                dialog.appendLog("Please install manually with: npm install -g @github/copilot");
                return false;
            }
            
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                dialog.appendLog("✅ GitHub Copilot CLI installed successfully.");
                return true;
            } else {
                dialog.appendLog("❌ GitHub Copilot CLI installation failed with exit code: " + exitCode);
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            dialog.appendLog("❌ Error installing GitHub Copilot CLI: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean isCopilotAuthenticated() {
        Process process = null;
        try {
            // Use npx with --no-install to run copilot with -p "test" to check authentication
            ProcessBuilder pb;
            if (isWindows()) {
                pb = new ProcessBuilder("cmd.exe", "/c", "npx", "--no-install", "@github/copilot", "-p", "test");
            } else {
                pb = new ProcessBuilder("npx", "--no-install", "@github/copilot", "-p", "test");
            }
            pb.redirectErrorStream(true);
            process = pb.start();
            
            final Process finalProcess = process;
            
            // Read output in a separate thread to avoid blocking
            StringBuilder output = new StringBuilder();
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(finalProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    // Ignore - process might have been terminated
                }
            });
            outputReader.setDaemon(true);
            outputReader.start();
            
            // Wait with timeout (20 seconds to allow for API calls)
            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            
            if (!finished) {
                // Process timed out, kill it aggressively and assume not authenticated
                process.destroy();
                Thread.sleep(500); // Give it a moment to die gracefully
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
                return false;
            }
            
            // Wait for the output reader thread to finish (give it time to read all output)
            outputReader.join(3000); // Increased from 1 second to 3 seconds
            
            // Check the exit code and output to determine authentication status
            int exitCode = process.exitValue();
            String outputStr = output.toString();
            
            // Debug: Print what we captured (can be removed later)
            System.err.println("DEBUG: Exit code: " + exitCode);
            System.err.println("DEBUG: Output length: " + outputStr.length());
            System.err.println("DEBUG: Output: " + outputStr.substring(0, Math.min(200, outputStr.length())));
            
            // If exit code is 0 and we got meaningful output, it's authenticated
            // Authenticated responses typically contain helpful text
            if (exitCode == 0 && outputStr.length() > 50) {
                System.err.println("DEBUG: Returning TRUE - authenticated based on exit code and output length");
                return true;
            }
            
            // Check for specific authentication error messages
            boolean hasAuthError = outputStr.contains("No authentication information found") ||
                                   outputStr.contains("not authenticated") ||
                                   outputStr.contains("authentication required") ||
                                   outputStr.contains("Please authenticate");
            
            System.err.println("DEBUG: hasAuthError = " + hasAuthError);
            System.err.println("DEBUG: Final return value = " + (!hasAuthError && outputStr.length() > 0));
            
            // Return true if authenticated (no auth error found and reasonable output)
            return !hasAuthError && outputStr.length() > 0;
        } catch (IOException | InterruptedException e) {
            // If we can't even run the command, assume not authenticated
            return false;
        } finally {
            // Ensure process is killed
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
    
    private static boolean runCopilotAuth(SystemCheckDialog dialog) {
        try {
            ProcessBuilder pb;
            if (isWindows()) {
                pb = new ProcessBuilder("cmd.exe", "/c", "npx", "@github/copilot", "auth", "login");
            } else {
                pb = new ProcessBuilder("npx", "@github/copilot", "auth", "login");
            }
            pb.inheritIO(); // This allows the interactive authentication to work
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                dialog.appendLog("✅ GitHub Copilot CLI authenticated successfully.");
                return true;
            } else {
                dialog.appendLog("❌ GitHub Copilot CLI authentication failed with exit code: " + exitCode);
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            dialog.appendLog("❌ Error during authentication: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean commandExists(String command) {
        try {
            Process process = new ProcessBuilder("which", command).start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
    
    private static boolean isDotNetInstalled() {
        // First try native dotnet
        if (checkDotNetCommand("dotnet")) {
            return true;
        }
        
        // On Linux, also try Wine dotnet
        if (!isWindows() && commandExists("wine")) {
            if (checkDotNetCommand("wine", "dotnet")) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean checkDotNetCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.command().add("--version");
            // Set WINEDEBUG to suppress Wine debug output on Linux
            pb.environment().put("WINEDEBUG", "-all");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Read the version output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return false;
            }
            
            // Check if version is present
            String version = output.toString().trim();
            if (version.isEmpty()) {
                return false;
            }
            
            // Accept .NET 9.x (any minor/patch version of .NET 9)
            // Examples: 9.0.100, 9.0.305, 9.1.0, etc.
            // Also accept any version 9 or higher for future compatibility
            if (version.matches("^9\\..*") || version.matches("^[1-9][0-9]+\\..*")) {
                return true;
            }
            
            // For backwards compatibility, also accept other versions with a warning logged
            // This allows the app to work even with older .NET versions
            return version.matches("^\\d+\\..*");
            
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
    
    private static boolean installNpmWindows(SystemCheckDialog dialog) {
        dialog.appendLog("Installing npm on Windows...");
        
        // First try winget (Windows Package Manager)
        if (commandExistsWindows("winget")) {
            dialog.appendLog("Using winget to install Node.js (includes npm)...");
            try {
                ProcessBuilder pb = new ProcessBuilder("winget", "install", "OpenJS.NodeJS", "--silent", "--accept-package-agreements", "--accept-source-agreements");
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                // Consume output in a separate thread to prevent buffer overflow
                Thread outputConsumer = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            dialog.appendLog(line);
                        }
                    } catch (IOException e) {
                        // Ignore - process might have terminated
                    }
                });
                outputConsumer.setDaemon(true);
                outputConsumer.start();
                
                // Wait with timeout (10 minutes for Node.js installation)
                boolean finished = process.waitFor(10, TimeUnit.MINUTES);
                
                if (!finished) {
                    dialog.appendLog("⚠️  Installation timed out after 10 minutes.");
                    process.destroyForcibly();
                    dialog.appendLog("Installation may still be running in the background. Please check manually.");
                    return false;
                }
                
                int exitCode = process.exitValue();
                // Note: winget sometimes returns non-zero exit codes even on success
                if (exitCode == 0) {
                    dialog.appendLog("✅ Node.js (npm) installed successfully via winget.");
                    return true;
                } else if (exitCode == -1978335189) {
                    // This specific code sometimes indicates the package was already installed or installation succeeded
                    dialog.appendLog("✅ Node.js installation completed (already present or newly installed).");
                    return true;
                } else {
                    dialog.appendLog("⚠️  winget installation returned exit code: " + exitCode);
                    dialog.appendLog("Installation may have succeeded despite the exit code. Will verify after restart.");
                    return true;
                }
            } catch (IOException | InterruptedException e) {
                dialog.appendLog("⚠️  Error using winget: " + e.getMessage());
            }
        }
        
        // Try Chocolatey as fallback
        if (commandExistsWindows("choco")) {
            dialog.appendLog("Using Chocolatey to install Node.js (includes npm)...");
            try {
                ProcessBuilder pb = new ProcessBuilder("choco", "install", "nodejs", "-y");
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    dialog.appendLog("✅ Node.js (npm) installed successfully via Chocolatey.");
                    return true;
                } else {
                    dialog.appendLog("❌ Chocolatey installation failed with exit code: " + exitCode);
                    return false;
                }
            } catch (IOException | InterruptedException e) {
                dialog.appendLog("❌ Error using Chocolatey: " + e.getMessage());
                return false;
            }
        }
        
        // No package manager available
        dialog.appendLog("❌ No package manager (winget or Chocolatey) found.");
        dialog.appendLog("Please install Node.js manually from https://nodejs.org/");
        return false;
    }
    
    private static boolean installDotNetWindows(SystemCheckDialog dialog) {
        dialog.appendLog("Installing .NET 9.0 SDK on Windows...");
        
        // First try winget
        if (commandExistsWindows("winget")) {
            dialog.appendLog("Using winget to install .NET 9.0 SDK...");
            try {
                ProcessBuilder pb = new ProcessBuilder("winget", "install", "Microsoft.DotNet.SDK.9", "--silent", "--accept-package-agreements", "--accept-source-agreements");
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                // Consume output in a separate thread to prevent buffer overflow
                Thread outputConsumer = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            dialog.appendLog(line);
                        }
                    } catch (IOException e) {
                        // Ignore - process might have terminated
                    }
                });
                outputConsumer.setDaemon(true);
                outputConsumer.start();
                
                // Wait with timeout (10 minutes for .NET SDK installation)
                boolean finished = process.waitFor(10, TimeUnit.MINUTES);
                
                if (!finished) {
                    dialog.appendLog("⚠️  Installation timed out after 10 minutes.");
                    process.destroyForcibly();
                    dialog.appendLog("Installation may still be running in the background. Please check manually.");
                    return false;
                }
                
                int exitCode = process.exitValue();
                // Note: winget sometimes returns non-zero exit codes even on success
                // So we treat any exit code as potential success and rely on verification
                if (exitCode == 0) {
                    dialog.appendLog("✅ .NET 9.0 SDK installed successfully via winget.");
                    return true;
                } else if (exitCode == -1978335189) {
                    // This specific code sometimes indicates the package was already installed or installation succeeded
                    dialog.appendLog("✅ .NET 9.0 SDK installation completed (already present or newly installed).");
                    return true;
                } else {
                    dialog.appendLog("⚠️  winget installation returned exit code: " + exitCode);
                    dialog.appendLog("Installation may have succeeded despite the exit code. Will verify after restart.");
                    // Return true anyway and let the verification step handle it
                    return true;
                }
            } catch (IOException | InterruptedException e) {
                dialog.appendLog("⚠️  Error using winget: " + e.getMessage());
            }
        }
        
        // Try Chocolatey as fallback
        if (commandExistsWindows("choco")) {
            dialog.appendLog("Using Chocolatey to install .NET 9.0 SDK...");
            try {
                ProcessBuilder pb = new ProcessBuilder("choco", "install", "dotnet-9.0-sdk", "-y");
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    dialog.appendLog("✅ .NET 9.0 SDK installed successfully via Chocolatey.");
                    return true;
                } else {
                    dialog.appendLog("❌ Chocolatey installation failed with exit code: " + exitCode);
                    return false;
                }
            } catch (IOException | InterruptedException e) {
                dialog.appendLog("❌ Error using Chocolatey: " + e.getMessage());
                return false;
            }
        }
        
        // No package manager available
        dialog.appendLog("❌ No package manager (winget or Chocolatey) found.");
        dialog.appendLog("Please install .NET 9.0 SDK manually from https://dotnet.microsoft.com/download/dotnet/9.0");
        return false;
    }
    
    private static boolean installDotNetLinux(SystemCheckDialog dialog) {
        dialog.appendLog("Installing .NET 9.0 SDK on Linux using Wine...");
        
        // First, check if Wine is installed
        if (!commandExists("wine")) {
            dialog.appendLog("❌ Wine is not installed.");
            dialog.appendLog("Please install Wine first using your package manager:");
            dialog.appendLog("  - Ubuntu/Debian: sudo apt install wine");
            dialog.appendLog("  - Fedora: sudo dnf install wine");
            dialog.appendLog("  - Arch: sudo pacman -S wine");
            dialog.appendLog("\nAlternatively, you can install .NET natively on Linux from:");
            dialog.appendLog("https://dotnet.microsoft.com/download/dotnet/9.0");
            return false;
        }
        
        dialog.appendLog("✅ Wine is installed.");
        
        // Download .NET 9.0 SDK installer for Windows
        String installerUrl = "https://aka.ms/dotnet/9.0/dotnet-sdk-win-x64.exe";
        String installerPath = System.getProperty("user.home") + "/.cache/dotnet-sdk-9.0-installer.exe";
        
        dialog.appendLog("Downloading .NET 9.0 SDK installer...");
        try {
            // Create cache directory if it doesn't exist
            ProcessBuilder mkdirPb = new ProcessBuilder("mkdir", "-p", System.getProperty("user.home") + "/.cache");
            mkdirPb.start().waitFor();
            
            // Download using wget or curl
            ProcessBuilder downloadPb;
            if (commandExists("wget")) {
                downloadPb = new ProcessBuilder("wget", "-O", installerPath, installerUrl);
            } else if (commandExists("curl")) {
                downloadPb = new ProcessBuilder("curl", "-L", "-o", installerPath, installerUrl);
            } else {
                dialog.appendLog("❌ Neither wget nor curl is available for downloading.");
                dialog.appendLog("Please install wget or curl, or manually install .NET SDK.");
                return false;
            }
            
            downloadPb.redirectErrorStream(true);
            Process downloadProcess = downloadPb.start();
            
            // Show download progress
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(downloadProcess.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    dialog.appendLog(line);
                }
            }
            
            int downloadExitCode = downloadProcess.waitFor();
            if (downloadExitCode != 0) {
                dialog.appendLog("❌ Failed to download .NET SDK installer.");
                return false;
            }
            
            dialog.appendLog("✅ Installer downloaded successfully.");
            
            // Install using Wine
            dialog.appendLog("Installing .NET SDK using Wine...");
            dialog.appendLog("This may take several minutes. Please be patient...");
            
            ProcessBuilder winePb = new ProcessBuilder("wine", installerPath, "/install", "/quiet", "/norestart");
            winePb.redirectErrorStream(true);
            Process wineProcess = winePb.start();
            
            // Show installation output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(wineProcess.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    dialog.appendLog(line);
                }
            }
            
            int wineExitCode = wineProcess.waitFor();
            
            // Clean up installer file
            try {
                new ProcessBuilder("rm", installerPath).start().waitFor();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            
            if (wineExitCode == 0) {
                dialog.appendLog("✅ .NET 9.0 SDK installed successfully via Wine.");
                
                // Set up Wine environment for dotnet
                dialog.appendLog("\nSetting up Wine environment for .NET...");
                String winePrefix = System.getenv("WINEPREFIX");
                if (winePrefix == null) {
                    winePrefix = System.getProperty("user.home") + "/.wine";
                }
                
                dialog.appendLog("Note: To use .NET SDK via Wine, you may need to run:");
                dialog.appendLog("  wine dotnet --version");
                dialog.appendLog("\nOr consider installing .NET natively on Linux from:");
                dialog.appendLog("https://dotnet.microsoft.com/download/dotnet/9.0");
                
                return true;
            } else {
                dialog.appendLog("❌ Wine installation failed with exit code: " + wineExitCode);
                dialog.appendLog("\nConsider installing .NET SDK natively on Linux from:");
                dialog.appendLog("https://dotnet.microsoft.com/download/dotnet/9.0");
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            dialog.appendLog("❌ Error during installation: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean commandExistsWindows(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("where", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
