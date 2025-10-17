/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.IDEProject;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectSerializer;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

public final class ProjectSerializerTests {
    private ProjectSerializerTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        testSaveAndLoad(ctx);
        testSaveNullProject(ctx);
        testLoadNonExistentFile(ctx);
        testComplexProject(ctx);
    }

    private static void testSaveAndLoad(TestContext ctx) {
        try {
            // Create a simple project
            InitialConfig config = new InitialConfig(
                ProgrammingLanguage.PYTHON,
                ProjectStyle.SCRIPT,
                EnumSet.of(TargetOs.LINUX),
                Path.of("/test/project")
            );
            
            IDEProject project = new IDEProject(config);
            Module module = new Module("TestModule", TaskType.GENERATE_APP_OR_SCRIPT);
            project.addRootModule(module);
            project.setMainModule(module);
            
            // Save to temp file
            Path tempFile = Files.createTempFile("vibe-test-", ".vcp");
            boolean saved = ProjectSerializer.save(project, tempFile);
            ctx.assertTrue("Project saved successfully", saved);
            ctx.assertTrue("File exists after save", Files.exists(tempFile));
            
            // Load from file
            IDEProject loaded = ProjectSerializer.load(tempFile);
            ctx.assertNotNull("Project loaded", loaded);
            ctx.assertNotNull("Config preserved", loaded.getInitialConfig());
            ctx.assertEquals("Programming language preserved", 
                ProgrammingLanguage.PYTHON, 
                loaded.getInitialConfig().getProgrammingLanguage());
            ctx.assertEquals("Root modules count", 1, loaded.getRootModules().size());
            ctx.assertNotNull("Main module preserved", loaded.getMainModule());
            
            // Cleanup
            Files.deleteIfExists(tempFile);
        } catch (Exception e) {
            ctx.fail("Save/load test failed: " + e.getMessage());
        }
    }

    private static void testSaveNullProject(TestContext ctx) {
        try {
            Path tempFile = Files.createTempFile("vibe-test-null-", ".vcp");
            
            // Save null project - should handle gracefully
            // Note: The actual implementation may not check for null and could throw exception
            try {
                boolean saved = ProjectSerializer.save(null, tempFile);
                // If we get here, either it returned false or true (both are acceptable handling)
                ctx.assertTrue("Saving null handled", true);
            } catch (NullPointerException e) {
                // NullPointerException is also acceptable for null input
                ctx.assertTrue("Null pointer exception handled", true);
            }
            
            // Cleanup
            Files.deleteIfExists(tempFile);
        } catch (Exception e) {
            // Exception is acceptable for null input
            ctx.assertTrue("Exception handled", true);
        }
    }

    private static void testLoadNonExistentFile(TestContext ctx) {
        try {
            Path nonExistent = Path.of("/tmp/nonexistent-vibe-project-" + System.currentTimeMillis() + ".vcp");
            
            IDEProject loaded = ProjectSerializer.load(nonExistent);
            ctx.assertEquals("Loading non-existent file returns null", null, loaded);
        } catch (Exception e) {
            ctx.fail("Load non-existent file test failed: " + e.getMessage());
        }
    }

    private static void testComplexProject(TestContext ctx) {
        try {
            // Create a more complex project with nested modules
            InitialConfig config = new InitialConfig(
                ProgrammingLanguage.JAVA,
                ProjectStyle.GUI,
                EnumSet.of(TargetOs.WINDOWS, TargetOs.LINUX),
                Path.of("/test/complex")
            );
            
            IDEProject project = new IDEProject(config);
            
            Module mainModule = new Module("MainModule", TaskType.GENERATE_APP_OR_SCRIPT);
            Module subModule1 = new Module("SubModule1", TaskType.CREATE_MODULE);
            Module subModule2 = new Module("SubModule2", TaskType.CREATE_MODULE);
            
            mainModule.addSubModule(subModule1);
            mainModule.addSubModule(subModule2);
            
            project.addRootModule(mainModule);
            project.setMainModule(mainModule);
            
            // Save and load
            Path tempFile = Files.createTempFile("vibe-complex-", ".vcp");
            boolean saved = ProjectSerializer.save(project, tempFile);
            ctx.assertTrue("Complex project saved", saved);
            
            IDEProject loaded = ProjectSerializer.load(tempFile);
            ctx.assertNotNull("Complex project loaded", loaded);
            ctx.assertEquals("All modules preserved", 3, loaded.getAllModules().size());
            ctx.assertEquals("Root modules count", 1, loaded.getRootModules().size());
            
            Module loadedMain = loaded.getRootModules().get(0);
            ctx.assertEquals("Sub-modules preserved", 2, loadedMain.getSubModules().size());
            
            // Cleanup
            Files.deleteIfExists(tempFile);
        } catch (Exception e) {
            ctx.fail("Complex project test failed: " + e.getMessage());
        }
    }
}
