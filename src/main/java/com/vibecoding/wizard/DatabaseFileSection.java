/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.nio.file.Path;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public final class DatabaseFileSection {
    private final JPanel component;
    private final JTextField fileField = new JTextField();
    private final Supplier<Path> chooser;

    public DatabaseFileSection(Supplier<Path> chooser) {
        this.chooser = chooser;
        fileField.setEditable(false);
        fileField.setColumns(30);

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(event -> handleBrowse());

    JPanel inner = new JPanel(new java.awt.BorderLayout(8, 0));
    inner.setOpaque(false);
    inner.setBorder(new EmptyBorder(0, 8, 8, 8));
        inner.add(fileField, java.awt.BorderLayout.CENTER);
        inner.add(browseButton, java.awt.BorderLayout.EAST);

        this.component = UiUtils.createFieldSection(
            "Database Definition File",
            "Database Definition",
            "Attach SQL or CSV files that describe the database schema. The wizard will feed this file path into the generated prompt.",
            inner);
    }

    private void handleBrowse() {
        Path path = chooser.get();
        if (path != null) {
            fileField.setText(path.toAbsolutePath().toString());
        }
    }

    public JPanel getComponent() {
        return component;
    }

    public void setPath(Path path) {
        fileField.setText(path == null ? "" : path.toAbsolutePath().toString());
    }

    public Path getPath() {
        String text = fileField.getText().trim();
        return text.isEmpty() ? null : Path.of(text);
    }
}
