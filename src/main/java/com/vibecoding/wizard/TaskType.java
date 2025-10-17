/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

public enum TaskType {
    GENERATE_APP_OR_SCRIPT("Generate or modify existing VCA app"),
    FIX_CODING_ERRORS("Fix coding errors"),
    CREATE_MODULE("Create module"),
    CREATE_ALGORITHM("Create algorithm"),
    MODIFY_EXISTING_SOFTWARE("Modify existing, unknown software");

    private final String displayName;

    TaskType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
