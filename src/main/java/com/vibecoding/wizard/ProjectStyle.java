/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

public enum ProjectStyle {
    WEB("Web"),
    GUI("GUI"),
    SCRIPT("Script");

    private final String displayName;

    ProjectStyle(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
