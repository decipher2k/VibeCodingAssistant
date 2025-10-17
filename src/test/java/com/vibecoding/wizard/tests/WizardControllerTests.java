/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.util.EnumSet;

import javax.swing.SwingUtilities;

import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;
import com.vibecoding.wizard.WizardController;

public final class WizardControllerTests {
    private WizardControllerTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Graphics environment is headless");
            return;
        }

        System.setProperty("vibecodingwizard.skipExecution", "true");
        WizardController controller = new WizardController();
        SwingUtilities.invokeAndWait(controller::show);

        InitialConfig config = new InitialConfig(ProgrammingLanguage.JAVA, ProjectStyle.GUI,
            EnumSet.of(TargetOs.LINUX), null);
        SwingUtilities.invokeAndWait(() -> ReflectionUtils.invoke(controller, "submitInitialConfig",
            new Class<?>[]{InitialConfig.class}, config));
        ctx.assertEquals("Initial config stored", config,
            ReflectionUtils.invoke(controller, "getInitialConfig", new Class<?>[0]));

        SwingUtilities.invokeAndWait(() -> ReflectionUtils.invoke(controller, "submitTaskType",
            new Class<?>[]{TaskType.class}, TaskType.CREATE_MODULE));
        ctx.assertEquals("Task type stored", TaskType.CREATE_MODULE,
            ReflectionUtils.invoke(controller, "getTaskType", new Class<?>[0]));

        MainTaskData data = new MainTaskData();
        data.setProjectOverview("Overview");
        SwingUtilities.invokeAndWait(() -> ReflectionUtils.invoke(controller, "prepareTaskExecution",
            new Class<?>[]{MainTaskData.class}, data));
        MainTaskData stored = (MainTaskData) ReflectionUtils.invoke(controller, "getMainTaskData", new Class<?>[0]);
        ctx.assertEquals("MainTaskData overview copied", "Overview", stored.getProjectOverview());

        // Close frames opened during the test
        for (Frame frame : Frame.getFrames()) {
            frame.dispose();
        }
    }
}
