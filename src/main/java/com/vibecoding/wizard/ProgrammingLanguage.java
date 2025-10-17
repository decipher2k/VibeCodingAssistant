/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

public enum ProgrammingLanguage {
    CSHARP("C# (DotNET 9.0)"),
    CPP("C++"),
    JAVA("Java"),
    PYTHON("Python"),
    PHP("PHP"),
    RUST("Rust"),
    GO("Go"),
    JAVASCRIPT("JavaScript"),
    RUBY("Ruby");

    private final String displayName;

    ProgrammingLanguage(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
