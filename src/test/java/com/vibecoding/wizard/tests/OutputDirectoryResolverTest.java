/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.OutputDirectoryResolver;
import com.vibecoding.wizard.ProgrammingLanguage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class OutputDirectoryResolverTest {

    @Test
    public void testGetOutputDirectoryForCSharp(@TempDir Path tempDir) throws IOException {
        // Create a C# project structure with bin/Debug
        Path binDebug = tempDir.resolve("bin/Debug");
        Files.createDirectories(binDebug);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.CSHARP);
        
        assertEquals(binDebug, result);
    }

    @Test
    public void testGetOutputDirectoryForJava(@TempDir Path tempDir) throws IOException {
        // Create a Java project structure with target/
        Path target = tempDir.resolve("target");
        Files.createDirectories(target);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.JAVA);
        
        assertEquals(target, result);
    }

    @Test
    public void testGetOutputDirectoryForRust(@TempDir Path tempDir) throws IOException {
        // Create a Rust project structure with target/debug
        Path targetDebug = tempDir.resolve("target/debug");
        Files.createDirectories(targetDebug);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.RUST);
        
        assertEquals(targetDebug, result);
    }

    @Test
    public void testGetOutputDirectoryForCpp(@TempDir Path tempDir) throws IOException {
        // Create a C++ project structure with build/
        Path build = tempDir.resolve("build");
        Files.createDirectories(build);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.CPP);
        
        assertEquals(build, result);
    }

    @Test
    public void testGetOutputDirectoryForGo(@TempDir Path tempDir) throws IOException {
        // Create a Go project structure with bin/
        Path bin = tempDir.resolve("bin");
        Files.createDirectories(bin);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.GO);
        
        assertEquals(bin, result);
    }

    @Test
    public void testGetOutputDirectoryForJavaScript(@TempDir Path tempDir) throws IOException {
        // Create a JavaScript project structure with dist/
        Path dist = tempDir.resolve("dist");
        Files.createDirectories(dist);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.JAVASCRIPT);
        
        assertEquals(dist, result);
    }

    @Test
    public void testGetOutputDirectoryForPython(@TempDir Path tempDir) throws IOException {
        // Create a Python project structure with dist/
        Path dist = tempDir.resolve("dist");
        Files.createDirectories(dist);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.PYTHON);
        
        assertEquals(dist, result);
    }

    @Test
    public void testGetOutputDirectoryWhenNoOutputDirExists(@TempDir Path tempDir) {
        // Don't create any directories
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.CSHARP);
        
        // Should return the preferred directory (bin/Debug for C#) even if it doesn't exist
        assertEquals(tempDir.resolve("bin/Debug"), result);
    }

    @Test
    public void testGetOutputDirectoryWithNullProjectDirectory() {
        Path result = OutputDirectoryResolver.getOutputDirectory(null, ProgrammingLanguage.JAVA);
        
        assertNull(result);
    }

    @Test
    public void testGetOutputDirectoryDescription() {
        assertEquals("bin/Debug or bin/Release", 
            OutputDirectoryResolver.getOutputDirectoryDescription(ProgrammingLanguage.CSHARP));
        assertEquals("target or build/libs", 
            OutputDirectoryResolver.getOutputDirectoryDescription(ProgrammingLanguage.JAVA));
        assertEquals("target/debug or target/release", 
            OutputDirectoryResolver.getOutputDirectoryDescription(ProgrammingLanguage.RUST));
        assertEquals("build", 
            OutputDirectoryResolver.getOutputDirectoryDescription(ProgrammingLanguage.CPP));
    }

    @Test
    public void testGetOutputDirectoryPrefersMostSpecific(@TempDir Path tempDir) throws IOException {
        // Create both bin/ and bin/Debug/ for C#
        Path bin = tempDir.resolve("bin");
        Path binDebug = tempDir.resolve("bin/Debug");
        Files.createDirectories(bin);
        Files.createDirectories(binDebug);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.CSHARP);
        
        // Should prefer the more specific bin/Debug over just bin
        assertEquals(binDebug, result);
    }

    @Test
    public void testGetOutputDirectoryFallsBackToLessSpecific(@TempDir Path tempDir) throws IOException {
        // Create only bin/ but not bin/Debug/ for C#
        Path bin = tempDir.resolve("bin");
        Files.createDirectories(bin);
        
        Path result = OutputDirectoryResolver.getOutputDirectory(tempDir, ProgrammingLanguage.CSHARP);
        
        // Should fall back to bin when bin/Debug doesn't exist
        assertEquals(bin, result);
    }
}
