/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for collecting initial project configuration.
 * This shows the InitialPanel and collects the configuration.
 */
public final class InitialConfigDialog extends JDialog {
    private InitialConfig result;
    private final InitialPanel initialPanel;
    
    public InitialConfigDialog(JFrame parent) {
        super(parent, "New Project Configuration", true);
        this.result = null;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 800);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Create initial panel with a mock controller
        initialPanel = new InitialPanel(new MockWizardController());
        
        add(initialPanel, BorderLayout.CENTER);
    }
    
    /**
     * Gets the result after the dialog is closed.
     * 
     * @return The initial configuration, or null if cancelled
     */
    public InitialConfig getResult() {
        return result;
    }
    
    /**
     * Mock wizard controller that just captures the initial config.
     */
    private class MockWizardController extends WizardController {
        public MockWizardController() {
            super();
        }
        
        @Override
        void submitInitialConfig(InitialConfig config) {
            result = config;
            dispose();
        }
    }
}
