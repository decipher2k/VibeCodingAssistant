/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles serialization and deserialization of IDE projects.
 */
public final class ProjectSerializer {
    
    private ProjectSerializer() {
    }
    
    /**
     * Saves a project to a file.
     * 
     * @param project The project to save
     * @param path The file path to save to
     * @return true if successful, false otherwise
     */
    public static boolean save(IDEProject project, Path path) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(path)))) {
            out.writeObject(project);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Loads a project from a file.
     * 
     * @param path The file path to load from
     * @return The loaded project, or null if failed
     */
    public static IDEProject load(Path path) {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(path)))) {
            return (IDEProject) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
