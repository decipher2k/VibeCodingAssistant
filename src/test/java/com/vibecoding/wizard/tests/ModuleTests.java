/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.Module;

public final class ModuleTests {
    private ModuleTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        testModuleCreation(ctx);
        testModuleProperties(ctx);
        testSubModules(ctx);
        testModuleHierarchy(ctx);
    }

    private static void testModuleCreation(TestContext ctx) {
        try {
            Module module1 = new Module("TestModule");
            ctx.assertNotNull("Module created with name", module1);
            ctx.assertNotNull("Module has ID", module1.getId());
            ctx.assertEquals("Name is set", "TestModule", module1.getName());
        } catch (Exception e) {
            ctx.fail("Module creation failed: " + e.getMessage());
        }
    }

    private static void testModuleProperties(TestContext ctx) {
        try {
            Module module = new Module("PropTest");
            
            module.setName("Updated Name");
            ctx.assertEquals("Name updated", "Updated Name", module.getName());
            
            module.setMainWindowName("MainWindow");
            ctx.assertEquals("Main window name set", "MainWindow", module.getMainWindowName());
            
        } catch (Exception e) {
            ctx.fail("Module properties test failed: " + e.getMessage());
        }
    }

    private static void testSubModules(TestContext ctx) {
        try {
            Module parent = new Module("Parent");
            Module child1 = new Module("Child1");
            Module child2 = new Module("Child2");
            
            parent.addSubModule(child1);
            parent.addSubModule(child2);
            
            ctx.assertEquals("Two sub-modules", 2, parent.getSubModules().size());
            ctx.assertTrue("Child1 is sub-module", parent.getSubModules().contains(child1));
            ctx.assertTrue("Child2 is sub-module", parent.getSubModules().contains(child2));
            
            // Check parent references
            ctx.assertEquals("Child1 parent is set", parent, child1.getParent());
            ctx.assertEquals("Child2 parent is set", parent, child2.getParent());
            
        } catch (Exception e) {
            ctx.fail("Sub-modules test failed: " + e.getMessage());
        }
    }

    private static void testModuleHierarchy(TestContext ctx) {
        try {
            Module root = new Module("Root");
            Module level1 = new Module("Level1");
            Module level2 = new Module("Level2");
            
            root.addSubModule(level1);
            level1.addSubModule(level2);
            
            // Test getAllModules
            ctx.assertEquals("All modules count", 3, root.getAllModules().size());
            
            // Test hierarchy
            ctx.assertEquals("Level2 parent", level1, level2.getParent());
            ctx.assertEquals("Level1 parent", root, level1.getParent());
            ctx.assertEquals("Root has no parent", null, root.getParent());
            
        } catch (Exception e) {
            ctx.fail("Module hierarchy test failed: " + e.getMessage());
        }
    }
}
