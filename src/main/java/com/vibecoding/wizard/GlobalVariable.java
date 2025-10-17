/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.Serializable;

/**
 * Represents a global variable that can be referenced in prompts.
 * Global variables are project-wide and available to all modules.
 */
public final class GlobalVariable implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String value;

    /**
     * Creates a new global variable with empty name and value.
     */
    public GlobalVariable() {
        this.name = "";
        this.value = "";
    }

    /**
     * Creates a new global variable with the specified name and value.
     * 
     * @param name The variable name
     * @param value The variable value
     */
    public GlobalVariable(String name, String value) {
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
