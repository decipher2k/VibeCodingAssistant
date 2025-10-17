/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

/**
 * Defines the project mode: IDE mode for complex projects or Wizard mode for simple projects.
 */
public enum ProjectMode {
    IDE("IDE Mode (Complex Projects)", "For complex multi-module projects with advanced features"),
    WIZARD("Wizard Mode (Simple Projects)", "For simple single-purpose projects with guided setup");

    private final String displayName;
    private final String description;

    ProjectMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
