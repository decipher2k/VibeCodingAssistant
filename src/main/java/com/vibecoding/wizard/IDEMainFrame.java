/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

/**
 * Main frame for the IDE containing:
 * - Menu bar with File and Project menus
 * - Module tree on the left
 * - Module editor (MainDialogPanel without buttons) on the right
 */
public final class IDEMainFrame extends JFrame {
    private final IDEController controller;
    private final ModuleTreePanel treePanel;
    private final ModuleEditorPanel editorPanel;
    private final JLabel statusLabel;
    private final JSplitPane splitPane;
    
    public IDEMainFrame(IDEController controller) {
        super("Vibe Coding IDE");
        this.controller = controller;
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                controller.handleExit();
            }
        });
        
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeManager.getBackgroundColor());
        
        // Create menu bar
        setJMenuBar(createMenuBar());
        
        // Create split pane
        treePanel = new ModuleTreePanel(controller);
        editorPanel = new ModuleEditorPanel(controller);
        
        splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            treePanel,
            editorPanel
        );
        splitPane.setDividerLocation(300);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.0);  // Give all extra space to the right panel (editor)
        splitPane.setContinuousLayout(true);  // Smooth resizing
        
        // Initially hide the tree panel (no project loaded)
        splitPane.setLeftComponent(null);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Create status bar
        statusLabel = new JLabel("No project open");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.setBackground(ThemeManager.getBackgroundColor());
        add(statusBar, BorderLayout.SOUTH);
        
        setSize(1400, 900);
        setMinimumSize(new Dimension(1000, 600));
    }
    
    /**
     * Creates the menu bar.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem newProjectItem = new JMenuItem("New Project...");
        newProjectItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        newProjectItem.addActionListener(e -> controller.createNewProject());
        fileMenu.add(newProjectItem);
        
        JMenuItem openItem = new JMenuItem("Open Project...");
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        openItem.addActionListener(e -> controller.openProject());
        fileMenu.add(openItem);
        
        fileMenu.addSeparator();
        
        JMenuItem saveItem = new JMenuItem("Save Project");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveItem.addActionListener(e -> controller.saveProject());
        fileMenu.add(saveItem);
        
        JMenuItem saveAsItem = new JMenuItem("Save Project As...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
        saveAsItem.addActionListener(e -> controller.saveProjectAs());
        fileMenu.add(saveAsItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        exitItem.addActionListener(e -> controller.handleExit());
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        
        // Project menu
        JMenu projectMenu = new JMenu("Project");
        projectMenu.setMnemonic('P');
        
        JMenuItem saveTemplateItem = new JMenuItem("Save Module Template...");
        saveTemplateItem.addActionListener(e -> controller.saveTemplate());
        projectMenu.add(saveTemplateItem);
        
        JMenuItem loadTemplateItem = new JMenuItem("Load Module Template...");
        loadTemplateItem.addActionListener(e -> controller.loadTemplate());
        projectMenu.add(loadTemplateItem);
        
        projectMenu.addSeparator();
        
        JMenuItem projectSettingsItem = new JMenuItem("Project Settings...");
        projectSettingsItem.addActionListener(e -> controller.openProjectSettings());
        projectMenu.add(projectSettingsItem);
        
        projectMenu.addSeparator();
        
        JMenuItem buildItem = new JMenuItem("Build Project");
        buildItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        buildItem.addActionListener(e -> controller.performBuild());
        projectMenu.add(buildItem);
        
        menuBar.add(projectMenu);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * Shows the about dialog.
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Vibe Coding IDE\n\n" +
            "A modular development environment for AI-assisted\n" +
            "software project generation.\n\n" +
            "Version 1.0",
            "About Vibe Coding IDE",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Refreshes the entire UI.
     */
    public void refreshUI() {
        // Show or hide tree panel based on whether a project is loaded
        showTreePanel(controller.getCurrentProject() != null);
        
        refreshModuleTree();
        updateTitle();
        updateStatusBar();
        
        if (controller.getSelectedModule() == null) {
            clearModuleEditor();
        }
    }
    
    /**
     * Refreshes the module tree.
     */
    public void refreshModuleTree() {
        treePanel.refreshTree();
    }
    
    /**
     * Shows or hides the tree panel based on whether a project is loaded.
     */
    private void showTreePanel(boolean show) {
        if (show && splitPane.getLeftComponent() == null) {
            // Show the tree panel
            splitPane.setLeftComponent(treePanel);
            splitPane.setDividerLocation(300);
        } else if (!show && splitPane.getLeftComponent() != null) {
            // Hide the tree panel
            splitPane.setLeftComponent(null);
        }
    }
    
    /**
     * Selects a module in the tree.
     */
    public void selectModuleInTree(Module module) {
        treePanel.selectModule(module);
    }
    
    /**
     * Shows the module editor for a specific module.
     */
    public void showModuleEditor(Module module) {
        if (module != null && controller.getCurrentProject() != null) {
            editorPanel.loadModule(module, controller.getCurrentProject().getInitialConfig());
        }
    }
    
    /**
     * Clears the module editor.
     */
    public void clearModuleEditor() {
        editorPanel.clear();
    }
    
    /**
     * Saves the current module data from the editor.
     */
    public void saveCurrentModuleData() {
        Module module = controller.getSelectedModule();
        if (module != null && controller.getCurrentProject() != null) {
            MainTaskData data = editorPanel.saveModule(controller.getCurrentProject().getInitialConfig());
            if (data != null) {
                controller.updateModuleData(data);
            }
        }
    }
    
    /**
     * Updates the window title.
     */
    public void updateTitle() {
        StringBuilder title = new StringBuilder("Vibe Coding IDE");
        
        Path projectFile = controller.getCurrentProjectFile();
        if (projectFile != null) {
            title.append(" - ").append(projectFile.getFileName());
        } else if (controller.getCurrentProject() != null) {
            title.append(" - Untitled Project");
        }
        
        if (controller.isDirty()) {
            title.append(" *");
        }
        
        setTitle(title.toString());
    }
    
    /**
     * Updates the status bar.
     */
    private void updateStatusBar() {
        IDEProject project = controller.getCurrentProject();
        if (project == null) {
            statusLabel.setText("No project open");
        } else {
            int moduleCount = project.getAllModules().size();
            Module mainModule = project.getMainModule();
            String mainModuleName = mainModule != null ? mainModule.getName() : "None";
            statusLabel.setText(String.format("Modules: %d | Main Module: %s", moduleCount, mainModuleName));
        }
    }
}
