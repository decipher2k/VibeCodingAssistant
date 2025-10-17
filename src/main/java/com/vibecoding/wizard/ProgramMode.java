/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

/**
 * Defines the program mode for GUI applications.
 */
public enum ProgramMode {
    MAIN_WINDOW("Main Window", "The main window of the main module will be loaded at program start"),
    MDI("MDI", "An MDI window will be shown at program start, allowing modules to be launched individually via menu");

    private final String displayName;
    private final String description;

    ProgramMode(String displayName, String description) {
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
