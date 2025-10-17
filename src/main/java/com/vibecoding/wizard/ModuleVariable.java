/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.Serializable;

/**
 * Represents a module-wide variable that can be referenced in prompts.
 * Module variables are specific to a module and available within that module's scope.
 */
public final class ModuleVariable implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String value;

    /**
     * Creates a new module variable with empty name and value.
     */
    public ModuleVariable() {
        this.name = "";
        this.value = "";
    }

    /**
     * Creates a new module variable with the specified name and value.
     * 
     * @param name The variable name
     * @param value The variable value
     */
    public ModuleVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name == null || name.isBlank() ? "(unnamed variable)" : name;
    }
}
