/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the output directory for compiled artifacts based on programming language conventions.
 */
public final class OutputDirectoryResolver {
    private OutputDirectoryResolver() {
    }

    /**
     * Gets the output directory where compiled artifacts are located for the given language.
     * Returns the first existing output directory, or the project directory if none are found.
     * 
     * @param projectDirectory The base project directory
     * @param language The programming language
     * @return The path to the output directory containing compiled artifacts
     */
    public static Path getOutputDirectory(Path projectDirectory, ProgrammingLanguage language) {
        if (projectDirectory == null) {
            return null;
        }

        List<String> possibleOutputDirs = getPossibleOutputDirectories(language);
        
        // Try to find the first existing output directory
        for (String outputDir : possibleOutputDirs) {
            Path fullPath = projectDirectory.resolve(outputDir);
            if (Files.exists(fullPath) && Files.isDirectory(fullPath)) {
                return fullPath;
            }
        }
        
        // If no output directory exists yet, return the most likely one (first in list)
        if (!possibleOutputDirs.isEmpty()) {
            return projectDirectory.resolve(possibleOutputDirs.get(0));
        }
        
        // Fallback to project directory
        return projectDirectory;
    }

    /**
     * Gets a list of possible output directory paths relative to the project root,
     * ordered by preference (most specific first).
     * 
     * @param language The programming language
     * @return List of possible output directory paths
     */
    private static List<String> getPossibleOutputDirectories(ProgrammingLanguage language) {
        List<String> dirs = new ArrayList<>();
        
        switch (language) {
            case CSHARP:
                // .NET typically outputs to bin/Debug or bin/Release
                dirs.add("bin/Debug");
                dirs.add("bin/Release");
                dirs.add("bin");
                break;
                
            case JAVA:
                // Maven uses target/, Gradle uses build/libs/
                dirs.add("target");
                dirs.add("build/libs");
                dirs.add("build");
                dirs.add("out");
                break;
                
            case RUST:
                // Cargo outputs to target/debug or target/release
                dirs.add("target/debug");
                dirs.add("target/release");
                dirs.add("target");
                break;
                
            case CPP:
                // CMake and other C++ build systems typically use build/
                dirs.add("build");
                dirs.add("out");
                dirs.add("bin");
                break;
                
            case GO:
                // Go build typically outputs to project root or bin/
                dirs.add("bin");
                break;
                
            case JAVASCRIPT:
                // Node.js/npm projects typically use dist/, build/, or out/
                dirs.add("dist");
                dirs.add("build");
                dirs.add("out");
                break;
                
            case PYTHON:
                // Python compiled bytecode goes to __pycache__ or dist/ for packages
                dirs.add("dist");
                dirs.add("build");
                dirs.add("__pycache__");
                break;
                
            case RUBY:
                // Ruby gems build to pkg/
                dirs.add("pkg");
                dirs.add("build");
                break;
                
            case PHP:
                // PHP doesn't compile, but may have a build/ or dist/ for deployments
                dirs.add("dist");
                dirs.add("build");
                break;
                
            default:
                // Generic fallback
                dirs.add("build");
                dirs.add("dist");
                dirs.add("out");
                dirs.add("bin");
                break;
        }
        
        return dirs;
    }
    
    /**
     * Gets a human-readable description of where output files are located for the given language.
     * 
     * @param language The programming language
     * @return A description string
     */
    public static String getOutputDirectoryDescription(ProgrammingLanguage language) {
        switch (language) {
            case CSHARP:
                return "bin/Debug or bin/Release";
            case JAVA:
                return "target or build/libs";
            case RUST:
                return "target/debug or target/release";
            case CPP:
                return "build";
            case GO:
                return "project root or bin";
            case JAVASCRIPT:
                return "dist or build";
            case PYTHON:
                return "dist or build";
            case RUBY:
                return "pkg";
            case PHP:
                return "dist or build";
            default:
                return "build or dist";
        }
    }
}
