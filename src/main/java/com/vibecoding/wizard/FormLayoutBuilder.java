/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public final class FormLayoutBuilder {
    private FormLayoutBuilder() {
    }

    public static JPanel vertical(Component... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
    panel.setOpaque(false);
        if (components != null) {
            for (Component component : components) {
                if (component == null) {
                    continue;
                }
                if (component instanceof javax.swing.JComponent) {
                    javax.swing.JComponent jc = (javax.swing.JComponent) component;
                    jc.setAlignmentX(Component.LEFT_ALIGNMENT);
                }
                panel.add(component);
            }
        }
        return panel;
    }
}
