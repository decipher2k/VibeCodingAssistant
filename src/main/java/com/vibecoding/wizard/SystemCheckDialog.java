/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public final class SystemCheckDialog extends JDialog {
    private final JTextArea logArea = new JTextArea(20, 60);
    private final JButton continueButton = new JButton("Continue");
    private boolean canContinue = false;
    private boolean requiresRestart = false;

    public SystemCheckDialog(JFrame owner) {
        super(owner, "System Check", true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Add window close listener to handle restart requirement
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (requiresRestart) {
                    System.exit(0);
                } else if (canContinue) {
                    dispose();
                }
            }
        });
        
        setLayout(new BorderLayout(8, 8));
        
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(Color.WHITE);
        logArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        continueButton.setEnabled(false);
        continueButton.addActionListener(event -> {
            if (requiresRestart) {
                System.exit(0);
            } else {
                dispose();
            }
        });
        footer.add(continueButton);
        add(footer, BorderLayout.SOUTH);
        
        pack();
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);
    }
    
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void enableContinue() {
        SwingUtilities.invokeLater(() -> {
            canContinue = true;
            continueButton.setEnabled(true);
        });
    }
    
    public void requireRestart() {
        SwingUtilities.invokeLater(() -> {
            requiresRestart = true;
            canContinue = true;
            continueButton.setText("Exit Application");
            continueButton.setEnabled(true);
        });
    }
    
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("\n❌ ERROR: " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            canContinue = true;
            continueButton.setText("Close");
            continueButton.setEnabled(true);
        });
    }
}
