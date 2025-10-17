/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

public enum TargetOs {
    WINDOWS("Windows"),
    LINUX("Linux"),
    MACOS("macOS");

    private final String displayName;

    TargetOs(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
