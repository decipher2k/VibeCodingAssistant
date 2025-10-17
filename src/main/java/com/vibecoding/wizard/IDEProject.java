/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a project containing a hierarchy of modules.
 * This is the root container for all project data in the IDE.
 */
public final class IDEProject implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private InitialConfig initialConfig;
    private final List<Module> rootModules;
    private Module mainModule;
    private ProjectSettings projectSettings;
    
    /**
     * Creates a new empty IDE project.
     */
    public IDEProject() {
        this.rootModules = new ArrayList<>();
        this.initialConfig = null;
        this.mainModule = null;
        this.projectSettings = new ProjectSettings();
    }
    
    /**
     * Creates a new IDE project with the given initial configuration.
     * 
     * @param initialConfig The initial project configuration
     */
    public IDEProject(InitialConfig initialConfig) {
        this();
        this.initialConfig = initialConfig;
        
        // Initialize project settings with data from initial config
        if (initialConfig != null) {
            if (initialConfig.getProjectName() != null) {
                this.projectSettings.setProjectName(initialConfig.getProjectName());
            }
            if (initialConfig.getProjectDirectory() != null) {
                this.projectSettings.setProjectPath(initialConfig.getProjectDirectory().toString());
            }
        }
    }
    
    public InitialConfig getInitialConfig() {
        return initialConfig;
    }
    
    public void setInitialConfig(InitialConfig initialConfig) {
        this.initialConfig = initialConfig;
    }
    
    public List<Module> getRootModules() {
        return new ArrayList<>(rootModules);
    }
    
    public void addRootModule(Module module) {
        if (module != null && !rootModules.contains(module)) {
            rootModules.add(module);
        }
    }
    
    public void removeRootModule(Module module) {
        rootModules.remove(module);
        if (mainModule == module) {
            mainModule = null;
        }
    }
    
    public void insertRootModule(int index, Module module) {
        if (module != null && !rootModules.contains(module)) {
            rootModules.add(index, module);
        }
    }
    
    public Module getMainModule() {
        return mainModule;
    }
    
    public void setMainModule(Module module) {
        this.mainModule = module;
    }
    
    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }
    
    public void setProjectSettings(ProjectSettings settings) {
        this.projectSettings = settings;
    }
    
    /**
     * Gets all modules in the project (root and nested).
     * 
     * @return List of all modules in the project
     */
    public List<Module> getAllModules() {
        List<Module> all = new ArrayList<>();
        for (Module root : rootModules) {
            all.addAll(root.getAllModules());
        }
        return all;
    }
    
    /**
     * Finds a module by its ID.
     * 
     * @param id The module ID to search for
     * @return The module with the given ID, or null if not found
     */
    public Module findModuleById(String id) {
        if (id == null) {
            return null;
        }
        for (Module module : getAllModules()) {
            if (id.equals(module.getId())) {
                return module;
            }
        }
        return null;
    }
    
    /**
     * Checks if a module name is unique within the project.
     * 
     * @param name The name to check
     * @param excludeModule The module to exclude from the check (for rename operations), or null
     * @return true if the name is unique, false otherwise
     */
    public boolean isModuleNameUnique(String name, Module excludeModule) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        for (Module module : getAllModules()) {
            if (module != excludeModule && trimmedName.equalsIgnoreCase(module.getName())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Custom deserialization to ensure backward compatibility with older VCP files
     * that don't have projectSettings field.
     */
    private void readObject(java.io.ObjectInputStream in) 
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Initialize projectSettings if it's null (for backward compatibility)
        if (projectSettings == null) {
            projectSettings = new ProjectSettings();
        }
    }
}
