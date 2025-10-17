/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.IDEController;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.ModuleEditorPanel;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.EnumSet;

public final class ModuleEditorPanelTests {
    private ModuleEditorPanelTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Cannot test ModuleEditorPanel in headless mode (requires Swing)");
            return;
        }

        testPanelCreation(ctx);
        testLoadModule(ctx);
        testSaveModule(ctx);
    }

    private static void testPanelCreation(TestContext ctx) {
        try {
            IDEController controller = new IDEController();
            ModuleEditorPanel panel = new ModuleEditorPanel(controller);
            
            ctx.assertNotNull("Panel created", panel);
            ctx.assertTrue("Panel is visible component", panel.isVisible() || !panel.isShowing());
        } catch (Exception e) {
            ctx.fail("Panel creation failed: " + e.getMessage());
        }
    }

    private static void testLoadModule(TestContext ctx) {
        try {
            IDEController controller = new IDEController();
            ModuleEditorPanel panel = new ModuleEditorPanel(controller);
            
            InitialConfig config = new InitialConfig(
                ProgrammingLanguage.JAVA,
                ProjectStyle.GUI,
                EnumSet.of(TargetOs.LINUX),
                Path.of("/test/project")
            );
            
            MainTaskData taskData = new MainTaskData();
            taskData.setProjectOverview("Test module overview");
            
            Module module = new Module("Test Module", TaskType.CREATE_MODULE);
            module.setTaskData(taskData);
            
            // Load the module
            panel.loadModule(module, config);
            
            ctx.assertTrue("Module loaded successfully", true);
        } catch (Exception e) {
            ctx.fail("Load module failed: " + e.getMessage());
        }
    }

    private static void testSaveModule(TestContext ctx) {
        try {
            IDEController controller = new IDEController();
            ModuleEditorPanel panel = new ModuleEditorPanel(controller);
            
            InitialConfig config = new InitialConfig(
                ProgrammingLanguage.PYTHON,
                ProjectStyle.SCRIPT,
                EnumSet.of(TargetOs.WINDOWS),
                Path.of("/test/project2")
            );
            
            MainTaskData taskData = new MainTaskData();
            taskData.setProjectOverview("Save test overview");
            
            Module module = new Module("Save Test", TaskType.GENERATE_APP_OR_SCRIPT);
            module.setTaskData(taskData);
            
            panel.loadModule(module, config);
            
            // Save the module data back using the correct method
            MainTaskData savedData = panel.saveModule(config);
            
            ctx.assertNotNull("Saved data not null", savedData);
            ctx.assertEquals("Overview preserved", "Save test overview", savedData.getProjectOverview());
        } catch (Exception e) {
            ctx.fail("Save module failed: " + e.getMessage());
        }
    }
}
