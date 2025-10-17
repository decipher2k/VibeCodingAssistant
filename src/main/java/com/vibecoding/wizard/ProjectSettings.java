/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents project-wide settings including workflows, global variables,
 * and database description. These settings apply across all modules.
 */
public final class ProjectSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<WorkflowItem> projectWorkflows;
    private List<GlobalVariable> globalVariables;
    private String databaseDescription;
    private String projectName;
    private String projectPath;
    private transient Path databaseDefinitionFile; // transient to handle custom serialization

    /**
     * Creates a new ProjectSettings instance with empty values.
     */
    public ProjectSettings() {
        this.projectWorkflows = new ArrayList<>();
        this.globalVariables = new ArrayList<>();
        this.databaseDescription = "";
        this.projectName = "";
        this.projectPath = "";
    }

    /**
     * Gets the project-wide workflow items.
     * 
     * @return Unmodifiable list of workflow items
     */
    public List<WorkflowItem> getProjectWorkflows() {
        return Collections.unmodifiableList(projectWorkflows);
    }

    /**
     * Sets the project-wide workflow items.
     * 
     * @param workflows List of workflow items
     */
    public void setProjectWorkflows(List<WorkflowItem> workflows) {
        this.projectWorkflows = new ArrayList<>(workflows);
    }

    /**
     * Gets the global variables.
     * 
     * @return Unmodifiable list of global variables
     */
    public List<GlobalVariable> getGlobalVariables() {
        return Collections.unmodifiableList(globalVariables);
    }

    /**
     * Sets the global variables.
     * 
     * @param variables List of global variables
     */
    public void setGlobalVariables(List<GlobalVariable> variables) {
        this.globalVariables = new ArrayList<>(variables);
    }

    /**
     * Gets the database description (e.g., MySQL, PostgreSQL, etc.).
     * 
     * @return Database description
     */
    public String getDatabaseDescription() {
        return databaseDescription;
    }

    /**
     * Sets the database description.
     * 
     * @param description Database description
     */
    public void setDatabaseDescription(String description) {
        this.databaseDescription = description;
    }

    /**
     * Gets the database definition file path.
     * 
     * @return Database definition file path
     */
    public Path getDatabaseDefinitionFile() {
        return databaseDefinitionFile;
    }

    /**
     * Sets the database definition file path.
     * 
     * @param file Database definition file path
     */
    public void setDatabaseDefinitionFile(Path file) {
        this.databaseDefinitionFile = file;
    }

    /**
     * Clears all project settings.
     */
    public void clear() {
        projectWorkflows.clear();
        globalVariables.clear();
        databaseDescription = "";
        databaseDefinitionFile = null;
        projectName = "";
        projectPath = "";
    }
    
    /**
     * Gets the project name.
     * 
     * @return Project name
     */
    public String getProjectName() {
        return projectName;
    }
    
    /**
     * Sets the project name.
     * 
     * @param name Project name
     */
    public void setProjectName(String name) {
        this.projectName = name;
    }
    
    /**
     * Gets the project path.
     * 
     * @return Project path
     */
    public String getProjectPath() {
        return projectPath;
    }
    
    /**
     * Sets the project path.
     * 
     * @param path Project path
     */
    public void setProjectPath(String path) {
        this.projectPath = path;
    }
}
