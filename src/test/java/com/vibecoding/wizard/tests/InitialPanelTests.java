/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.EnumSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.InitialPanel;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.WizardController;

public final class InitialPanelTests {
    private InitialPanelTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Graphics environment is headless");
            return;
        }

        System.setProperty("vibecodingwizard.skipExecution", "true");
        WizardController controller = new WizardController();
        InitialPanel panel = new InitialPanel(controller);

        Field languageField = InitialPanel.class.getDeclaredField("languageCombo");
        languageField.setAccessible(true);
        @SuppressWarnings("unchecked")
        JComboBox<ProgrammingLanguage> languageCombo = (JComboBox<ProgrammingLanguage>) languageField.get(panel);
        languageCombo.setSelectedItem(ProgrammingLanguage.PYTHON);

        Field styleField = InitialPanel.class.getDeclaredField("styleCombo");
        styleField.setAccessible(true);
        @SuppressWarnings("unchecked")
        JComboBox<ProjectStyle> styleCombo = (JComboBox<ProjectStyle>) styleField.get(panel);
        styleCombo.setSelectedItem(ProjectStyle.SCRIPT);

        // Set project name (required)
        Field nameField = InitialPanel.class.getDeclaredField("projectNameField");
        nameField.setAccessible(true);
        javax.swing.JTextField projectNameField = (javax.swing.JTextField) nameField.get(panel);
        projectNameField.setText("TestProject");

        // Set project directory (required)
        Field dirField = InitialPanel.class.getDeclaredField("selectedProjectDirectory");
        dirField.setAccessible(true);
        dirField.set(panel, java.nio.file.Paths.get("/tmp/test-project"));

        // Set at least one target OS (required)
        Field linuxCheckField = InitialPanel.class.getDeclaredField("linuxCheck");
        linuxCheckField.setAccessible(true);
        javax.swing.JCheckBox linuxCheck = (javax.swing.JCheckBox) linuxCheckField.get(panel);
        linuxCheck.setSelected(true);

        panel.getComponentCount();

        JButton next = findButton(panel);
        ctx.assertNotNull("Next button found", next);

        SwingUtilities.invokeAndWait(next::doClick);

    InitialConfig config = (InitialConfig) ReflectionUtils.invoke(controller, "getInitialConfig", new Class<?>[0]);
        ctx.assertNotNull("Config stored", config);
        ctx.assertEquals("Language", ProgrammingLanguage.PYTHON, config.getProgrammingLanguage());
        ctx.assertEquals("Style", ProjectStyle.SCRIPT, config.getProjectStyle());
        ctx.assertEquals("Targets default Linux", EnumSet.of(TargetOs.LINUX), config.getTargetOperatingSystems());
        ctx.assertEquals("Project name", "TestProject", config.getProjectName());
    }

    private static JButton findButton(JComponent component) {
        for (java.awt.Component child : component.getComponents()) {
            if (child instanceof JButton button && "Next".equals(button.getText())) {
                return button;
            }
            if (child instanceof JComponent nested) {
                JButton result = findButton(nested);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
