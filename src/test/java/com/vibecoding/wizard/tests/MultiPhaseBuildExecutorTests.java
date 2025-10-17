/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.CopilotCliService;
import com.vibecoding.wizard.IDEProject;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.MultiPhaseBuildExecutor;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.EnumSet;

import javax.swing.JFrame;

public final class MultiPhaseBuildExecutorTests {
    private MultiPhaseBuildExecutorTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Cannot test MultiPhaseBuildExecutor in headless mode");
            return;
        }

        testExecutorCreation(ctx);
        testExecutorWithEmptyProject(ctx);
    }

    private static void testExecutorCreation(TestContext ctx) {
        try {
            IDEProject project = new IDEProject();
            CopilotCliService service = new CopilotCliService(Path.of("."));
            JFrame frame = new JFrame();
            
            MultiPhaseBuildExecutor executor = new MultiPhaseBuildExecutor(project, service, frame);
            ctx.assertNotNull("Executor created", executor);
            
            frame.dispose();
        } catch (Exception e) {
            ctx.fail("Executor creation failed: " + e.getMessage());
        }
    }

    private static void testExecutorWithEmptyProject(TestContext ctx) {
        try {
            // Create project without config
            IDEProject emptyProject = new IDEProject();
            CopilotCliService service = new CopilotCliService(Path.of("."));
            JFrame frame = new JFrame();
            
            MultiPhaseBuildExecutor executor = new MultiPhaseBuildExecutor(emptyProject, service, frame);
            ctx.assertNotNull("Executor handles empty project", executor);
            
            // Note: We can't actually execute the build without mocking the UI and copilot service
            // But we can verify the executor is created properly
            
            frame.dispose();
        } catch (Exception e) {
            ctx.fail("Empty project test failed: " + e.getMessage());
        }
    }
}
