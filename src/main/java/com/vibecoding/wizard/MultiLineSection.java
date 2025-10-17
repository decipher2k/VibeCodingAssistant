/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MultiLineSection {
    private final JPanel component;
    private final JTextArea textArea;

    public MultiLineSection(String caption, String helpText) {
    this.textArea = new JTextArea(5, 20);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
    textArea.setBorder(new javax.swing.border.EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(100, 120));
        scrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xDDE4ED)));

        this.component = UiUtils.createFieldSection(caption, caption, helpText, scrollPane);
        component.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    }

    public JPanel getComponent() {
        return component;
    }

    public void setText(String value) {
        textArea.setText(value == null ? "" : value);
    }

    public String getText() {
        return textArea.getText().trim();
    }
}
