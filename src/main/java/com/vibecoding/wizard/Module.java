/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a module in the IDE project hierarchy.
 * A module can contain task data and sub-modules forming a tree structure.
 */
public final class Module implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private String name;
    private TaskType taskType;
    private MainTaskData taskData;
    private final List<Module> subModules;
    private Module parent;
    private String mainWindowName; // The name of the main window for this module
    private List<ModuleVariable> moduleVariables; // Module-wide variables
    
    /**
     * Creates a new module with a generated ID and the given name.
     * 
     * @param name The name of the module
     */
    public Module(String name) {
        this(name, TaskType.GENERATE_APP_OR_SCRIPT);
    }
    
    /**
     * Creates a new module with a generated ID, name, and task type.
     * 
     * @param name The name of the module
     * @param taskType The task type for this module
     */
    public Module(String name, TaskType taskType) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.taskType = taskType;
        this.taskData = new MainTaskData();
        this.subModules = new ArrayList<>();
        this.parent = null;
        this.moduleVariables = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public TaskType getTaskType() {
        return taskType;
    }
    
    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }
    
    public MainTaskData getTaskData() {
        return taskData;
    }
    
    public void setTaskData(MainTaskData taskData) {
        this.taskData = taskData;
        // Sync main window name from task data
        if (taskData != null && taskData.getMainWindowName() != null) {
            this.mainWindowName = taskData.getMainWindowName();
        } else if (taskData != null && !taskData.getDialogs().isEmpty() && this.mainWindowName == null) {
            // Auto-set first dialog as main window if not set
            this.mainWindowName = taskData.getDialogs().get(0).getName();
            taskData.setMainWindowName(this.mainWindowName);
        }
    }
    
    public List<Module> getSubModules() {
        return new ArrayList<>(subModules);
    }
    
    public void addSubModule(Module module) {
        if (module != null && !subModules.contains(module)) {
            subModules.add(module);
            module.parent = this;
        }
    }
    
    public void removeSubModule(Module module) {
        if (subModules.remove(module)) {
            module.parent = null;
        }
    }
    
    public void insertSubModule(int index, Module module) {
        if (module != null && !subModules.contains(module)) {
            subModules.add(index, module);
            module.parent = this;
        }
    }
    
    public Module getParent() {
        return parent;
    }
    
    public String getMainWindowName() {
        return mainWindowName;
    }
    
    public void setMainWindowName(String mainWindowName) {
        this.mainWindowName = mainWindowName;
    }
    
    /**
     * Gets the module-wide variables.
     * 
     * @return Unmodifiable list of module variables
     */
    public List<ModuleVariable> getModuleVariables() {
        if (moduleVariables == null) {
            moduleVariables = new ArrayList<>();
        }
        return Collections.unmodifiableList(moduleVariables);
    }

    /**
     * Sets the module-wide variables.
     * 
     * @param variables List of module variables
     */
    public void setModuleVariables(List<ModuleVariable> variables) {
        if (this.moduleVariables == null) {
            this.moduleVariables = new ArrayList<>();
        } else {
            this.moduleVariables.clear();
        }
        if (variables != null) {
            this.moduleVariables.addAll(variables);
        }
    }
    
    /**
     * Gets the depth level of this module in the hierarchy.
     * Root modules have level 0.
     * 
     * @return The depth level
     */
    public int getLevel() {
        int level = 0;
        Module current = parent;
        while (current != null) {
            level++;
            current = current.parent;
        }
        return level;
    }
    
    /**
     * Checks if this module is an ancestor of the given module.
     * 
     * @param other The module to check
     * @return true if this module is an ancestor of other
     */
    public boolean isAncestorOf(Module other) {
        if (other == null) {
            return false;
        }
        Module current = other.parent;
        while (current != null) {
            if (current == this) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }
    
    /**
     * Gets all modules in this subtree (including this module).
     * 
     * @return List of all modules in depth-first order
     */
    public List<Module> getAllModules() {
        List<Module> result = new ArrayList<>();
        result.add(this);
        for (Module sub : subModules) {
            result.addAll(sub.getAllModules());
        }
        return result;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Module other = (Module) obj;
        return id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
