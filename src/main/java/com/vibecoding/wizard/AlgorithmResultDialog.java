/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class AlgorithmResultDialog extends JDialog {
    public AlgorithmResultDialog(JFrame owner, Path projectDirectory) {
        super(owner, "Algorithm Created", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));
        getContentPane().setBackground(new java.awt.Color(0xF0F3F7));

        String algorithmContent = findAndReadAlgorithmFile(projectDirectory);
        
        JTextArea area = new JTextArea(algorithmContent);
        area.setEditable(false);
        area.setLineWrap(false);
        area.setWrapStyleWord(false);
        area.setBackground(java.awt.Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(event -> dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(owner);
    }

    private String findAndReadAlgorithmFile(Path projectDirectory) {
        if (projectDirectory == null) {
            projectDirectory = Path.of("").toAbsolutePath();
        }

        List<Path> codeFiles = new ArrayList<>();
        
        try {
            // Search for common algorithm file patterns
            try (Stream<Path> paths = Files.walk(projectDirectory, 3)) {
                paths.filter(Files::isRegularFile)
                     .filter(p -> {
                         String name = p.getFileName().toString().toLowerCase();
                         String ext = getFileExtension(name);
                         // Look for common programming file extensions
                         return ext.matches("py|java|cpp|c|cs|js|ts|go|rs|rb|php|swift|kt");
                     })
                     .filter(p -> {
                         // Exclude test files, build artifacts, etc.
                         String pathStr = p.toString().toLowerCase();
                         return !pathStr.contains("test") && 
                                !pathStr.contains("build") && 
                                !pathStr.contains("target") &&
                                !pathStr.contains("node_modules") &&
                                !pathStr.contains(".git");
                     })
                     .forEach(codeFiles::add);
            }
        } catch (IOException ex) {
            return "Error reading algorithm files: " + ex.getMessage();
        }

        if (codeFiles.isEmpty()) {
            return "No algorithm file was found in the project directory:\n" + projectDirectory.toAbsolutePath();
        }

        // Read the most recently modified file or the first one found
        Path algorithmFile = codeFiles.stream()
            .sorted((a, b) -> {
                try {
                    return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                } catch (IOException e) {
                    return 0;
                }
            })
            .findFirst()
            .orElse(codeFiles.get(0));

        try {
            String content = Files.readString(algorithmFile);
            return "Algorithm File: " + algorithmFile.getFileName() + "\n" +
                   "Full Path: " + algorithmFile.toAbsolutePath() + "\n" +
                   "=" .repeat(80) + "\n\n" + content;
        } catch (IOException ex) {
            return "Error reading algorithm file: " + ex.getMessage();
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
}
