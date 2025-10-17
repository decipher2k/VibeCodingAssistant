/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.IDEProject;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

public final class IDEProjectTests {
    private IDEProjectTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        testEmptyProject(ctx);
        testProjectWithConfig(ctx);
        testAddRemoveModules(ctx);
        testMainModule(ctx);
        testFindModuleById(ctx);
        testGetAllModules(ctx);
    }

    private static void testEmptyProject(TestContext ctx) {
        IDEProject project = new IDEProject();
        ctx.assertNotNull("Empty project created", project);
        ctx.assertEquals("No root modules initially", 0, project.getRootModules().size());
        ctx.assertEquals("No main module initially", null, project.getMainModule());
        ctx.assertEquals("No initial config", null, project.getInitialConfig());
    }

    private static void testProjectWithConfig(TestContext ctx) {
        InitialConfig config = new InitialConfig(
            ProgrammingLanguage.JAVA,
            ProjectStyle.GUI,
            EnumSet.of(TargetOs.LINUX),
            Path.of("/test/project")
        );
        
        IDEProject project = new IDEProject(config);
        ctx.assertNotNull("Project with config created", project);
        ctx.assertEquals("Config stored", config, project.getInitialConfig());
        
        // Test setting config
        InitialConfig newConfig = new InitialConfig(
            ProgrammingLanguage.PYTHON,
            ProjectStyle.WEB,
            EnumSet.of(TargetOs.WINDOWS),
            Path.of("/test/project2")
        );
        project.setInitialConfig(newConfig);
        ctx.assertEquals("Config updated", newConfig, project.getInitialConfig());
    }

    private static void testAddRemoveModules(TestContext ctx) {
        IDEProject project = new IDEProject();
        
        Module module1 = new Module("Module 1");
        Module module2 = new Module("Module 2");
        
        // Add modules
        project.addRootModule(module1);
        ctx.assertEquals("One root module", 1, project.getRootModules().size());
        
        project.addRootModule(module2);
        ctx.assertEquals("Two root modules", 2, project.getRootModules().size());
        
        // Try to add duplicate
        project.addRootModule(module1);
        ctx.assertEquals("Still two root modules (no duplicate)", 2, project.getRootModules().size());
        
        // Try to add null
        project.addRootModule(null);
        ctx.assertEquals("Still two root modules (null ignored)", 2, project.getRootModules().size());
        
        // Remove module
        project.removeRootModule(module1);
        ctx.assertEquals("One root module after removal", 1, project.getRootModules().size());
        
        // Insert at index
        Module module3 = new Module("Module 3");
        project.insertRootModule(0, module3);
        ctx.assertEquals("Three root modules after insert", 2, project.getRootModules().size());
        ctx.assertEquals("Inserted at correct position", module3, project.getRootModules().get(0));
    }

    private static void testMainModule(TestContext ctx) {
        IDEProject project = new IDEProject();
        Module mainModule = new Module("Main Module");
        
        project.addRootModule(mainModule);
        project.setMainModule(mainModule);
        
        ctx.assertEquals("Main module set", mainModule, project.getMainModule());
        
        // Remove main module - should clear main module reference
        project.removeRootModule(mainModule);
        ctx.assertEquals("Main module cleared on removal", null, project.getMainModule());
    }

    private static void testFindModuleById(TestContext ctx) {
        IDEProject project = new IDEProject();
        
        Module root = new Module("Root");
        Module child = new Module("Child");
        root.addSubModule(child);
        
        project.addRootModule(root);
        
        // Find existing modules
        Module foundRoot = project.findModuleById(root.getId());
        ctx.assertEquals("Found root module", root, foundRoot);
        
        Module foundChild = project.findModuleById(child.getId());
        ctx.assertEquals("Found child module", child, foundChild);
        
        // Try to find non-existent module
        Module notFound = project.findModuleById("nonexistent-id-12345");
        ctx.assertEquals("Non-existent module returns null", null, notFound);
        
        // Try with null ID
        Module nullFind = project.findModuleById(null);
        ctx.assertEquals("Null ID returns null", null, nullFind);
    }

    private static void testGetAllModules(TestContext ctx) {
        IDEProject project = new IDEProject();
        
        Module root1 = new Module("Root 1");
        Module root2 = new Module("Root 2");
        Module child1 = new Module("Child 1");
        Module child2 = new Module("Child 2");
        
        root1.addSubModule(child1);
        root2.addSubModule(child2);
        
        project.addRootModule(root1);
        project.addRootModule(root2);
        
        List<Module> allModules = project.getAllModules();
        ctx.assertEquals("All modules count", 4, allModules.size());
        ctx.assertTrue("Contains root1", allModules.contains(root1));
        ctx.assertTrue("Contains root2", allModules.contains(root2));
        ctx.assertTrue("Contains child1", allModules.contains(child1));
        ctx.assertTrue("Contains child2", allModules.contains(child2));
    }
}
