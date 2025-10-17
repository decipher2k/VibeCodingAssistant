/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import com.vibecoding.wizard.TaskSelectionPanel;
import com.vibecoding.wizard.TaskType;
import com.vibecoding.wizard.WizardController;

public final class TaskSelectionPanelTests {
    private TaskSelectionPanelTests() {
    }

    @SuppressWarnings("unchecked")
    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Graphics environment is headless");
            return;
        }

        System.setProperty("vibecodingwizard.skipExecution", "true");
        WizardController controller = new WizardController();
        TaskSelectionPanel panel = new TaskSelectionPanel(controller);

        Field buttonsField = TaskSelectionPanel.class.getDeclaredField("taskButtons");
        buttonsField.setAccessible(true);
        Map<TaskType, JRadioButton> buttons = (Map<TaskType, JRadioButton>) buttonsField.get(panel);
        buttons.get(TaskType.CREATE_ALGORITHM).setSelected(true);

        JButton next = findButton(panel, "Next");
        ctx.assertNotNull("Next button located", next);
        SwingUtilities.invokeAndWait(next::doClick);

        ctx.assertEquals("Controller task type", TaskType.CREATE_ALGORITHM,
            ReflectionUtils.invoke(controller, "getTaskType", new Class<?>[0]));

        ReflectionUtils.invoke(panel, "refreshSelection", new Class<?>[]{TaskType.class}, TaskType.FIX_CODING_ERRORS);
        ctx.assertTrue("Refresh selection toggles button", buttons.get(TaskType.FIX_CODING_ERRORS).isSelected());
    }

    private static JButton findButton(JComponent component, String text) {
        for (java.awt.Component child : component.getComponents()) {
            if (child instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (child instanceof JComponent nested) {
                JButton result = findButton(nested, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
