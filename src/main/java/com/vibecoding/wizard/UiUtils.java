/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public final class UiUtils {
    private UiUtils() {
    }

    public static JPanel createFieldSection(String caption, String helpTitle, String helpText, JComponent component) {
        JPanel container = new JPanel(new BorderLayout());
        EmptyBorder padding = new EmptyBorder(12, 16, 12, 16);
        // Modern rounded border with subtle shadow effect
        LineBorder outline = new LineBorder(ThemeManager.getBorderColor(), 1, true);
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                outline,
                BorderFactory.createEmptyBorder(2, 2, 4, 2) // Subtle shadow simulation
            ),
            padding
        ));
        container.setBackground(Color.WHITE);
        container.setOpaque(true);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);
        JLabel label = new JLabel(caption);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(ThemeManager.getTextColor());
        JButton helpButton = createHelpButton(helpTitle, helpText, component);
        header.add(label);
        header.add(helpButton);

        container.add(header, BorderLayout.NORTH);
        container.add(component, BorderLayout.CENTER);
        
        // Set the container to be only as tall as its content
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, container.getPreferredSize().height));
        
        return container;
    }

    public static JButton createHelpButton(String title, String message, Component parent) {
        JButton help = new JButton("?");
        help.setPreferredSize(new Dimension(22, 22));
        help.setMinimumSize(new Dimension(22, 22));
        help.setMaximumSize(new Dimension(22, 22));
        // Clean, professional appearance
        help.setBackground(new Color(0xF3F2F1));
        help.setForeground(new Color(0x323130));
        help.setFocusPainted(false);
        help.setFont(help.getFont().deriveFont(java.awt.Font.BOLD, 11f));
        help.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xD2D0CE), 1, true),
            new EmptyBorder(2, 5, 2, 5)
        ));
        help.setOpaque(true);
        help.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        help.addActionListener(event -> showHelpDialog(parent, title, message));
        
        // Subtle hover effect
        help.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                help.setBackground(new Color(0xE1DFDD));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                help.setBackground(new Color(0xF3F2F1));
            }
        });
        
        return help;
    }

    public static void showHelpDialog(Component parent, String title, String message) {
        JTextArea area = new JTextArea(message);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(null);
        area.setFont(area.getFont().deriveFont(14f));
        area.setForeground(ThemeManager.getTextColor());
        area.setRows(12);
        area.setColumns(50);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getBorderColor(), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(600, 320));

        JOptionPane.showMessageDialog(parent, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static JScrollPane wrapInScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        return scrollPane;
    }
}
