/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public final class InitialPanel extends JPanel {
    private final WizardController controller;
    private final JComboBox<ProgrammingLanguage> languageCombo;
    private final JComboBox<ProjectStyle> styleCombo;
    private final JComboBox<ProgramMode> programModeCombo;
    private final JPanel programModeSection;
    private final JCheckBox windowsCheck;
    private final JCheckBox linuxCheck;
    private final JCheckBox macCheck;
    private final JTextField projectNameField;
    private final JTextField projectDirectoryField;
    private final JRadioButton ideModeRadio;
    private final JRadioButton wizardModeRadio;
    private Path selectedProjectDirectory;

    public InitialPanel(WizardController controller) {
        this.controller = controller;
        this.languageCombo = new JComboBox<>(ProgrammingLanguage.values());
        this.styleCombo = new JComboBox<>(ProjectStyle.values());
        this.programModeCombo = new JComboBox<>(ProgramMode.values());
        this.windowsCheck = new JCheckBox(TargetOs.WINDOWS.toString());
        this.linuxCheck = new JCheckBox(TargetOs.LINUX.toString());
        this.macCheck = new JCheckBox(TargetOs.MACOS.toString());
        this.projectNameField = new JTextField(30);
        this.projectDirectoryField = new JTextField(30);
        this.ideModeRadio = new JRadioButton(ProjectMode.IDE.toString());
        this.wizardModeRadio = new JRadioButton(ProjectMode.WIZARD.toString());
        this.selectedProjectDirectory = null;
        
        // Group the radio buttons
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(ideModeRadio);
        modeGroup.add(wizardModeRadio);
        
        // Select Wizard mode by default
        wizardModeRadio.setSelected(true);
        
    // Set maximum size for comboboxes to prevent them from stretching
    Dimension comboMaxSize = new Dimension(250, 30);
    languageCombo.setMaximumSize(comboMaxSize);
    languageCombo.setPreferredSize(new Dimension(200, 25));
    styleCombo.setMaximumSize(comboMaxSize);
    styleCombo.setPreferredSize(new Dimension(200, 25));
    programModeCombo.setMaximumSize(comboMaxSize);
    programModeCombo.setPreferredSize(new Dimension(200, 25));
    
    windowsCheck.setOpaque(false);
    linuxCheck.setOpaque(false);
    macCheck.setOpaque(false);
    ideModeRadio.setOpaque(false);
    wizardModeRadio.setOpaque(false);
    projectDirectoryField.setEditable(false);

    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(16, 24, 16, 24));
    setOpaque(true);
    setBackground(ThemeManager.getBackgroundColor());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    form.setOpaque(false);

        // Project Mode selection
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        modePanel.setOpaque(false);
        modePanel.add(ideModeRadio);
        modePanel.add(wizardModeRadio);

        form.add(UiUtils.createFieldSection(
            "Project Mode",
            "Project Mode",
            "Choose IDE Mode for complex projects with multiple modules and advanced features, or Wizard Mode for simpler single-purpose projects with guided setup.",
            modePanel));

        // Wrap language combo in a left-aligned panel
        JPanel languageWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        languageWrapper.setOpaque(false);
        languageWrapper.add(languageCombo);

        form.add(UiUtils.createFieldSection(
            "Programming Language",
            "Programming Language",
            "Choose the main language for the project. This will be used when generating prompts and selecting compilation tooling.",
            languageWrapper));

        // Wrap style combo in a left-aligned panel
        JPanel styleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        styleWrapper.setOpaque(false);
        styleWrapper.add(styleCombo);

        form.add(UiUtils.createFieldSection(
            "Project Style",
            "Project Style",
            "Select the overall style of the outcome. GUI and Web projects will allow dialog definitions, while Script focuses on single-file flows.",
            styleWrapper));

        // Program Mode section (only visible for GUI projects)
        JPanel programModeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        programModeWrapper.setOpaque(false);
        programModeWrapper.add(programModeCombo);

        programModeSection = UiUtils.createFieldSection(
            "Program Mode",
            "Program Mode",
            "For GUI applications, choose how the application starts: Main Window (loads the main module's main window) or MDI (shows an MDI container with menu access to all modules).",
            programModeWrapper);
        form.add(programModeSection);

        // Listen for style changes to show/hide program mode
        styleCombo.addActionListener(event -> updateProgramModeVisibility());
        updateProgramModeVisibility(); // Initial state

        JPanel osPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
    osPanel.setOpaque(false);
        osPanel.add(windowsCheck);
        osPanel.add(linuxCheck);
        osPanel.add(macCheck);

        form.add(UiUtils.createFieldSection(
            "Target Operating Systems",
            "Target Operating Systems",
            "Mark one or more operating systems that the generated solution must support. This influences build and deployment instructions.",
            osPanel));

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        namePanel.setOpaque(false);
        namePanel.add(projectNameField);

        form.add(UiUtils.createFieldSection(
            "Project Name",
            "Project Name",
            "Enter a name for your project. This will be used for the project file and other references.",
            namePanel));

        JPanel directoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        directoryPanel.setOpaque(false);
        directoryPanel.add(projectDirectoryField);
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(event -> selectProjectDirectory());
        directoryPanel.add(browseButton);

        form.add(UiUtils.createFieldSection(
            "Project Directory",
            "Project Directory",
            "Select the directory where the project files will be created. If not specified, the current directory will be used.",
            directoryPanel));

        add(form, BorderLayout.CENTER);

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(event -> handleNext());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(nextButton);
        add(footer, BorderLayout.SOUTH);
    }

    private void selectProjectDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Project Directory");
        if (selectedProjectDirectory != null) {
            chooser.setCurrentDirectory(selectedProjectDirectory.toFile());
        }
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedProjectDirectory = chooser.getSelectedFile().toPath();
            projectDirectoryField.setText(selectedProjectDirectory.toString());
        }
    }

    private void updateProgramModeVisibility() {
        ProjectStyle style = (ProjectStyle) styleCombo.getSelectedItem();
        boolean isGui = style == ProjectStyle.GUI;
        programModeSection.setVisible(isGui);
        revalidate();
        repaint();
    }

    private void handleNext() {
        // Validation
        String projectName = projectNameField.getText().trim();
        if (projectName.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please enter a project name.",
                "Project Name Required",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedProjectDirectory == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please select a project directory.",
                "Project Directory Required",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        EnumSet<TargetOs> targets = EnumSet.noneOf(TargetOs.class);
        if (windowsCheck.isSelected()) {
            targets.add(TargetOs.WINDOWS);
        }
        if (linuxCheck.isSelected()) {
            targets.add(TargetOs.LINUX);
        }
        if (macCheck.isSelected()) {
            targets.add(TargetOs.MACOS);
        }
        
        if (targets.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Please select at least one target operating system.",
                "Target Platform Required",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProgrammingLanguage language = (ProgrammingLanguage) languageCombo.getSelectedItem();
        ProjectStyle style = (ProjectStyle) styleCombo.getSelectedItem();
        ProgramMode programMode = (ProgramMode) programModeCombo.getSelectedItem();
        ProjectMode projectMode = ideModeRadio.isSelected() ? ProjectMode.IDE : ProjectMode.WIZARD;

        InitialConfig config = new InitialConfig(language, style, targets, selectedProjectDirectory, programMode, projectName, projectMode);
        controller.submitInitialConfig(config);
    }
    
    /**
     * Loads configuration values into the panel controls.
     * This is used when loading a template to populate the initial settings.
     * 
     * @param config The configuration to load
     */
    void loadConfig(InitialConfig config) {
        if (config == null) {
            return;
        }
        
        // Set programming language
        if (config.getProgrammingLanguage() != null) {
            languageCombo.setSelectedItem(config.getProgrammingLanguage());
        }
        
        // Set project style
        if (config.getProjectStyle() != null) {
            styleCombo.setSelectedItem(config.getProjectStyle());
        }
        
        // Set program mode
        if (config.getProgramMode() != null) {
            programModeCombo.setSelectedItem(config.getProgramMode());
        }
        
        // Set target operating systems
        EnumSet<TargetOs> targetOs = config.getTargetOperatingSystems();
        windowsCheck.setSelected(targetOs.contains(TargetOs.WINDOWS));
        linuxCheck.setSelected(targetOs.contains(TargetOs.LINUX));
        macCheck.setSelected(targetOs.contains(TargetOs.MACOS));
        
        // Set project name
        String projectName = config.getProjectName();
        if (projectName != null) {
            projectNameField.setText(projectName);
        } else {
            projectNameField.setText("");
        }
        
        // Set project directory
        Path projectDir = config.getProjectDirectory();
        if (projectDir != null) {
            selectedProjectDirectory = projectDir;
            projectDirectoryField.setText(projectDir.toString());
        } else {
            selectedProjectDirectory = null;
            projectDirectoryField.setText("");
        }
        
        // Set IDE/Wizard mode
        ProjectMode mode = config.getIdeOrWizardMode();
        if (mode == ProjectMode.IDE) {
            ideModeRadio.setSelected(true);
        } else {
            wizardModeRadio.setSelected(true);
        }
    }
}
