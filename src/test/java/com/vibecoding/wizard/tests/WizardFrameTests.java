/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.EnumSet;

import javax.swing.SwingUtilities;

import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;
import com.vibecoding.wizard.WizardController;
import com.vibecoding.wizard.WizardFrame;

public final class WizardFrameTests {
    private WizardFrameTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Graphics environment is headless");
            return;
        }

        System.setProperty("vibecodingwizard.skipExecution", "true");
        WizardController controller = new WizardController();
        Field frameField = WizardController.class.getDeclaredField("frame");
        frameField.setAccessible(true);
        WizardFrame frame = (WizardFrame) frameField.get(controller);

        SwingUtilities.invokeAndWait(() -> {
            ReflectionUtils.invoke(controller, "submitInitialConfig",
                new Class<?>[]{InitialConfig.class},
                new InitialConfig(ProgrammingLanguage.JAVA, ProjectStyle.GUI, EnumSet.of(TargetOs.LINUX), null));
            ReflectionUtils.invoke(controller, "submitTaskType",
                new Class<?>[]{TaskType.class}, TaskType.GENERATE_APP_OR_SCRIPT);
            ReflectionUtils.invoke(frame, "showInitialPanel", new Class<?>[0]);
            ReflectionUtils.invoke(frame, "showTaskSelection", new Class<?>[0]);
            ReflectionUtils.invoke(frame, "showMainDialog", new Class<?>[0]);
        });

        ctx.assertNotNull("WizardFrame not null", frame);
        ctx.assertEquals("Frame title", "Vibe Coding Wizard", frame.getTitle());
        frame.dispose();
    }
}
