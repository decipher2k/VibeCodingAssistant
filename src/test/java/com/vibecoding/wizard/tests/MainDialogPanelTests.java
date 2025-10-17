/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.vibecoding.wizard.DialogDefinition;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.MainDialogPanel;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;
import com.vibecoding.wizard.WizardController;
import com.vibecoding.wizard.WizardFrame;

public final class MainDialogPanelTests {
    private MainDialogPanelTests() {
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

        Field mainPanelField = WizardFrame.class.getDeclaredField("mainDialogPanel");
        mainPanelField.setAccessible(true);
        MainDialogPanel mainPanel = (MainDialogPanel) mainPanelField.get(frame);

        InitialConfig config = new InitialConfig(ProgrammingLanguage.JAVA, ProjectStyle.GUI,
            EnumSet.of(TargetOs.WINDOWS), null);
    ReflectionUtils.invoke(controller, "submitInitialConfig", new Class<?>[]{InitialConfig.class}, config);
    ReflectionUtils.invoke(controller, "submitTaskType", new Class<?>[]{TaskType.class}, TaskType.GENERATE_APP_OR_SCRIPT);

        MainTaskData data = new MainTaskData();
        data.setProjectOverview("GUI Overview");
        data.setDialogs(List.of(new DialogDefinition("Home", "Home", "Main screen")));

        SwingUtilities.invokeAndWait(() -> ReflectionUtils.invoke(mainPanel, "configure",
            new Class<?>[]{TaskType.class, InitialConfig.class, MainTaskData.class},
            TaskType.GENERATE_APP_OR_SCRIPT, config, data));

        JButton performButton = findButton(mainPanel, "Perform Task");
        ctx.assertNotNull("Perform Task button found", performButton);
        SwingUtilities.invokeAndWait(performButton::doClick);

        MainTaskData stored = (MainTaskData) ReflectionUtils.invoke(controller, "getMainTaskData", new Class<?>[0]);
        ctx.assertEquals("Project overview persisted", "GUI Overview", stored.getProjectOverview());
        ctx.assertEquals("Dialogs persisted", 1, stored.getDialogs().size());
    }

    private static JButton findButton(JComponent component, String text) {
        for (java.awt.Component child : component.getComponents()) {
            if (child instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (child instanceof JComponent nested) {
                JButton candidate = findButton(nested, text);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }
}
