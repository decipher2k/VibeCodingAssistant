/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class CompilationErrorDialog extends JDialog {
    public CompilationErrorDialog(JFrame owner, String errorOutput) {
        super(owner, "Compilation Errors", true);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setPreferredSize(new Dimension(680, 400));
    getContentPane().setBackground(new java.awt.Color(0xF0F3F7));

        JTextArea area = new JTextArea(errorOutput == null ? "" : errorOutput);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    area.setBackground(java.awt.Color.WHITE);
    area.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(scrollPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);
    }
}
