/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Service for saving and loading project templates.
 */
public final class TemplateManager {
    private static final String TEMPLATE_EXTENSION = "json";
    private static final String TEMPLATE_DESCRIPTION = "Vibe Coding Template (*.json)";
    private Path lastDirectory;

    public TemplateManager() {
        // Try to use user's home directory as default
        String homeDir = System.getProperty("user.home");
        if (homeDir != null) {
            Path templatesDir = Paths.get(homeDir, ".vibe-coding-wizard", "templates");
            try {
                if (!Files.exists(templatesDir)) {
                    Files.createDirectories(templatesDir);
                }
                this.lastDirectory = templatesDir;
            } catch (IOException e) {
                this.lastDirectory = Paths.get(homeDir);
            }
        } else {
            this.lastDirectory = Paths.get("").toAbsolutePath();
        }
    }

    /**
     * Saves a project template to a file chosen by the user.
     * 
     * @param parent The parent frame for the file chooser dialog
     * @param template The template to save
     * @return true if the template was saved successfully, false otherwise
     */
    public boolean saveTemplate(JFrame parent, ProjectTemplate template) {
        if (template == null) {
            JOptionPane.showMessageDialog(parent,
                "No template data to save.",
                "Save Template",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Template As");
        fileChooser.setFileFilter(new FileNameExtensionFilter(TEMPLATE_DESCRIPTION, TEMPLATE_EXTENSION));
        
        if (lastDirectory != null && Files.exists(lastDirectory)) {
            fileChooser.setCurrentDirectory(lastDirectory.toFile());
        }

        // Suggest a default filename based on task type
        if (template.getTaskType() != null) {
            String taskName = template.getTaskType().name().toLowerCase().replace('_', '-');
            fileChooser.setSelectedFile(new java.io.File(taskName + "-template.json"));
        }

        int result = fileChooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        Path selectedPath = fileChooser.getSelectedFile().toPath();
        
        // Ensure the file has the correct extension
        if (!selectedPath.toString().toLowerCase().endsWith("." + TEMPLATE_EXTENSION)) {
            selectedPath = Paths.get(selectedPath.toString() + "." + TEMPLATE_EXTENSION);
        }

        // Check if file exists and confirm overwrite
        if (Files.exists(selectedPath)) {
            int overwrite = JOptionPane.showConfirmDialog(parent,
                "File already exists. Do you want to overwrite it?",
                "Confirm Overwrite",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (overwrite != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        try {
            String json = template.toJson();
            Files.writeString(selectedPath, json, StandardCharsets.UTF_8);
            lastDirectory = selectedPath.getParent();
            
            JOptionPane.showMessageDialog(parent,
                "Template saved successfully to:\n" + selectedPath.toAbsolutePath(),
                "Template Saved",
                JOptionPane.INFORMATION_MESSAGE);
            
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Failed to save template:\n" + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Loads a project template from a file chosen by the user.
     * 
     * @param parent The parent frame for the file chooser dialog
     * @return The loaded template, or null if loading was cancelled or failed
     */
    public ProjectTemplate loadTemplate(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Template");
        fileChooser.setFileFilter(new FileNameExtensionFilter(TEMPLATE_DESCRIPTION, TEMPLATE_EXTENSION));
        
        if (lastDirectory != null && Files.exists(lastDirectory)) {
            fileChooser.setCurrentDirectory(lastDirectory.toFile());
        }

        int result = fileChooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        Path selectedPath = fileChooser.getSelectedFile().toPath();
        
        if (!Files.exists(selectedPath)) {
            JOptionPane.showMessageDialog(parent,
                "File does not exist:\n" + selectedPath.toAbsolutePath(),
                "Load Error",
                JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            String json = Files.readString(selectedPath, StandardCharsets.UTF_8);
            ProjectTemplate template = ProjectTemplate.fromJson(json);
            lastDirectory = selectedPath.getParent();
            
            return template;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Failed to load template:\n" + e.getMessage() + 
                "\n\nPlease ensure the file is a valid template file.",
                "Load Error",
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Gets the last directory used for saving/loading templates.
     */
    public Path getLastDirectory() {
        return lastDirectory;
    }

    /**
     * Sets the last directory used for saving/loading templates.
     */
    public void setLastDirectory(Path directory) {
        if (directory != null && Files.isDirectory(directory)) {
            this.lastDirectory = directory;
        }
    }
    
    /**
     * Saves a project template with confirmation dialog.
     * This is a convenience method for the File > Save menu action.
     * 
     * @param parent The parent frame for the dialog
     * @param template The template to save
     * @return true if the template was saved successfully, false otherwise
     */
    public boolean saveTemplateWithConfirmation(JFrame parent, ProjectTemplate template) {
        return saveTemplate(parent, template);
    }
}
