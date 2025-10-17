/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public final class WizardFrame extends JFrame {
    private static final String VIEW_INITIAL = "initial";
    private static final String VIEW_TASK_SELECTION = "taskSelection";
    private static final String VIEW_MAIN = "main";

    private final WizardController controller;
    private final CardLayout cardLayout;
    private final JPanel cards;
    private final InitialPanel initialPanel;
    private final TaskSelectionPanel taskSelectionPanel;
    private final MainDialogPanel mainDialogPanel;

    public WizardFrame(WizardController controller) {
        super("Vibe Coding Wizard");
        this.controller = controller;
        this.cardLayout = new CardLayout();
        this.cards = new JPanel(cardLayout);
        this.cards.setOpaque(true);
        this.cards.setBackground(ThemeManager.getBackgroundColor());
        this.initialPanel = new InitialPanel(controller);
        this.taskSelectionPanel = new TaskSelectionPanel(controller);
        this.mainDialogPanel = new MainDialogPanel(controller);

        cards.add(initialPanel, VIEW_INITIAL);
        cards.add(taskSelectionPanel, VIEW_TASK_SELECTION);
        cards.add(mainDialogPanel, VIEW_MAIN);

        setContentPane(cards);
        
        // Create menu bar
        createMenuBar();
        
        // Handle window closing
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.handleExit();
            }
        });
        
        setSize(1100, 780);
        setMinimumSize(getSize());
    }

    void showInitialPanel() {
        cardLayout.show(cards, VIEW_INITIAL);
    }
    
    /**
     * Loads initial configuration into the initial panel without showing it.
     * This is useful when loading a template - the config is loaded so that
     * if the user navigates back, they see the correct values.
     * 
     * @param config The configuration to load
     */
    void loadInitialPanelConfig(InitialConfig config) {
        initialPanel.loadConfig(config);
    }

    void showTaskSelection() {
        taskSelectionPanel.refreshSelection(controller.getTaskType());
        cardLayout.show(cards, VIEW_TASK_SELECTION);
    }

    void showMainDialog() {
        mainDialogPanel.configure(controller.getTaskType(), controller.getInitialConfig(), controller.getMainTaskData());
        cardLayout.show(cards, VIEW_MAIN);
    }
    
    /**
     * Gets the current task data from the main dialog panel.
     * This saves the current form data and returns it.
     * 
     * @return The current task data, or null if not available
     */
    MainTaskData getCurrentTaskData() {
        return mainDialogPanel.getCurrentTaskData();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        // New menu item
        JMenuItem newItem = new JMenuItem("New");
        newItem.setMnemonic('N');
        newItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        newItem.addActionListener(e -> controller.handleNew());
        fileMenu.add(newItem);
        
        // Load menu item
        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.setMnemonic('L');
        loadItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        loadItem.addActionListener(e -> controller.handleLoad());
        fileMenu.add(loadItem);
        
        // Save menu item
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setMnemonic('S');
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveItem.addActionListener(e -> controller.handleSave());
        fileMenu.add(saveItem);
        
        fileMenu.addSeparator();
        
        // Exit menu item
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        exitItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitItem.addActionListener(e -> controller.handleExit());
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        
        // Project menu
        JMenu projectMenu = new JMenu("Project");
        projectMenu.setMnemonic('P');
        
        // Project Settings menu item
        JMenuItem projectSettingsItem = new JMenuItem("Settings");
        projectSettingsItem.setMnemonic('S');
        projectSettingsItem.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
        projectSettingsItem.addActionListener(e -> controller.handleProjectSettings());
        projectMenu.add(projectSettingsItem);
        
        menuBar.add(projectMenu);
        
        setJMenuBar(menuBar);
    }
}
