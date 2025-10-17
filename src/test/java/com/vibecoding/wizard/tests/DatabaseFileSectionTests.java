/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComponent;

import com.vibecoding.wizard.DatabaseFileSection;

public final class DatabaseFileSectionTests {
    private DatabaseFileSectionTests() {
    }

    public static void run(TestContext ctx) {
        AtomicBoolean invoked = new AtomicBoolean(false);
        DatabaseFileSection section = new DatabaseFileSection(() -> {
            invoked.set(true);
            return Path.of("/tmp/dataset.csv");
        });

        section.setPath(Path.of("/tmp/initial.sql"));
        ctx.assertEquals("Initial path", Path.of("/tmp/initial.sql"), section.getPath());

        JComponent component = section.getComponent();
        JButton button = findButton(component, "Browse...");
        ctx.assertNotNull("Browse button located", button);
        try {
            javax.swing.SwingUtilities.invokeAndWait(button::doClick);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        ctx.assertTrue("Chooser invoked after click", invoked.get());
        section.setPath(Path.of("/tmp/override.sql"));
        ctx.assertEquals("Override path", Path.of("/tmp/override.sql"), section.getPath());
    }

    private static JButton findButton(JComponent component, String label) {
        if (component instanceof JButton) {
            JButton button = (JButton) component;
            if (label.equals(button.getText())) {
                return button;
            }
        }
        for (java.awt.Component child : component.getComponents()) {
            if (child instanceof JButton) {
                JButton button = (JButton) child;
                if (label.equals(button.getText())) {
                    return button;
                }
            }
            if (child instanceof JComponent) {
                JButton nested = findButton((JComponent) child, label);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }
}
