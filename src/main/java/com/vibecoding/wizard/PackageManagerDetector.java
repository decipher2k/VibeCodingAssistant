/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class PackageManagerDetector {
    private PackageManagerDetector() {
    }
    
    public static class PackageManager {
        private final String name;
        private final String installCommand;
        private final boolean requiresSudo;
        
        public PackageManager(String name, String installCommand, boolean requiresSudo) {
            this.name = name;
            this.installCommand = installCommand;
            this.requiresSudo = requiresSudo;
        }
        
        public String getName() {
            return name;
        }
        
        public String getInstallCommand() {
            return installCommand;
        }
        
        public boolean requiresSudo() {
            return requiresSudo;
        }
    }
    
    public static PackageManager detect() {
        // Check for various package managers
        if (commandExists("apt")) {
            return new PackageManager("apt", "apt install -y npm", true);
        } else if (commandExists("dnf")) {
            return new PackageManager("dnf", "dnf install -y npm", true);
        } else if (commandExists("yum")) {
            return new PackageManager("yum", "yum install -y npm", true);
        } else if (commandExists("pacman")) {
            return new PackageManager("pacman", "pacman -S --noconfirm npm", true);
        } else if (commandExists("zypper")) {
            return new PackageManager("zypper", "zypper install -y npm", true);
        } else if (commandExists("brew")) {
            return new PackageManager("brew", "brew install npm", false);
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            // Assume Homebrew on macOS
            return new PackageManager("brew", "brew install npm", false);
        }
        
        return null;
    }
    
    private static boolean commandExists(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", command);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
