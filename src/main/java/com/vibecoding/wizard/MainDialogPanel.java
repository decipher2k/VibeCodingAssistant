/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public final class MainDialogPanel extends JPanel {
    private final WizardController controller;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardHolder = new JPanel(cardLayout);
    private final Map<TaskType, TaskPanel> panels = new EnumMap<>(TaskType.class);
    private TaskType currentTask;
    private InitialConfig initialConfig;

    public MainDialogPanel(WizardController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(ThemeManager.getBackgroundColor());

    panels.put(TaskType.GENERATE_APP_OR_SCRIPT, new GenerateAppForm(this::selectFile, controller));
    panels.put(TaskType.FIX_CODING_ERRORS, new FixCodingErrorsForm());
    panels.put(TaskType.CREATE_MODULE, new CreateModuleForm(this::selectFile, controller));
    panels.put(TaskType.CREATE_ALGORITHM, new CreateAlgorithmForm());
    panels.put(TaskType.MODIFY_EXISTING_SOFTWARE, new ModifySoftwareForm(this::selectFile, controller));

        panels.forEach((taskType, panel) -> cardHolder.add(panel.getComponent(), taskType.name()));
        cardHolder.setOpaque(false);

        add(cardHolder, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> controller.goBackToTaskSelection());

        JButton saveTemplateButton = new JButton("Save Template");
        saveTemplateButton.addActionListener(event -> handleSaveTemplate());

        JButton loadTemplateButton = new JButton("Load Template");
        loadTemplateButton.addActionListener(event -> handleLoadTemplate());

        JButton performButton = new JButton("Perform Task");
        performButton.addActionListener(event -> handlePerformTask());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(backButton);
        footer.add(Box.createHorizontalStrut(10));
        footer.add(saveTemplateButton);
        footer.add(loadTemplateButton);
        footer.add(Box.createHorizontalStrut(10));
        footer.add(performButton);

        add(footer, BorderLayout.SOUTH);
    }

    void configure(TaskType taskType, InitialConfig config, MainTaskData data) {
        this.currentTask = taskType;
        this.initialConfig = config;
        TaskPanel panel = panels.get(taskType);
        if (panel == null) {
            throw new IllegalStateException("Unsupported task type: " + taskType);
        }
        panel.load(config, data);
        cardLayout.show(cardHolder, taskType.name());
    }
    
    /**
     * Gets the current task data from the active panel.
     * 
     * @return The current task data, or null if no task is active
     */
    MainTaskData getCurrentTaskData() {
        if (currentTask == null || initialConfig == null) {
            return null;
        }
        TaskPanel panel = panels.get(currentTask);
        if (panel == null) {
            return null;
        }
        return panel.save(initialConfig);
    }

    private void handlePerformTask() {
        if (currentTask == null) {
            JOptionPane.showMessageDialog(this,
                "No task selected.",
                "Missing Task",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        TaskPanel panel = panels.get(currentTask);
        MainTaskData updated = panel.save(initialConfig);
        controller.prepareTaskExecution(updated);
    }

    private void handleSaveTemplate() {
        if (currentTask == null || initialConfig == null) {
            JOptionPane.showMessageDialog(this,
                "Please complete the initial configuration before saving a template.",
                "Cannot Save Template",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Gather current data from the active panel
        TaskPanel panel = panels.get(currentTask);
        MainTaskData currentData = panel.save(initialConfig);

        // Create template with current config and data
        ProjectTemplate template = new ProjectTemplate(
            initialConfig.getProgrammingLanguage(),
            initialConfig.getProjectStyle(),
            initialConfig.getTargetOperatingSystems(),
            currentTask,
            currentData,
            initialConfig.getProjectDirectory()
        );

        // Save using controller
        controller.saveTemplate(template);
    }

    private void handleLoadTemplate() {
        if (initialConfig == null) {
            JOptionPane.showMessageDialog(this,
                "Please complete the initial configuration before loading a template.",
                "Cannot Load Template",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm if there's unsaved data
        int confirm = JOptionPane.showConfirmDialog(this,
            "Loading a template will replace all current form data.\n" +
            "Do you want to continue?",
            "Confirm Load Template",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Load using controller
        ProjectTemplate template = controller.loadTemplate();
        if (template != null) {
            // Check if the loaded template matches the current initial config
            boolean configMatches = 
                template.getProgrammingLanguage() == initialConfig.getProgrammingLanguage() &&
                template.getProjectStyle() == initialConfig.getProjectStyle() &&
                template.getTargetOperatingSystems().equals(initialConfig.getTargetOperatingSystems());

            if (!configMatches) {
                int continueAnyway = JOptionPane.showConfirmDialog(this,
                    "The loaded template was created with different initial settings:\n" +
                    "Language: " + template.getProgrammingLanguage() + "\n" +
                    "Style: " + template.getProjectStyle() + "\n" +
                    "Target OS: " + template.getTargetOperatingSystems() + "\n\n" +
                    "Your current settings:\n" +
                    "Language: " + initialConfig.getProgrammingLanguage() + "\n" +
                    "Style: " + initialConfig.getProjectStyle() + "\n" +
                    "Target OS: " + initialConfig.getTargetOperatingSystems() + "\n\n" +
                    "Do you want to load the template anyway?",
                    "Configuration Mismatch",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (continueAnyway != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Apply the template data to the current panel
            if (template.getTaskData() != null) {
                TaskPanel panel = panels.get(currentTask);
                panel.load(initialConfig, template.getTaskData());
                
                JOptionPane.showMessageDialog(this,
                    "Template loaded successfully.",
                    "Template Loaded",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private Path selectFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().toPath();
        }
        return null;
    }

    // Public factory methods for creating task panels (used by ModuleEditorPanel)
    public static TaskPanel createGenerateAppForm(Supplier<Path> chooser, WizardController controller) {
        return new GenerateAppForm(chooser, controller);
    }
    
    // Overload for IDE usage with JFrame
    public static TaskPanel createGenerateAppForm(Supplier<Path> chooser, JFrame parentFrame) {
        return new GenerateAppForm(chooser, parentFrame);
    }

    public static TaskPanel createFixCodingErrorsForm() {
        return new FixCodingErrorsForm();
    }

    public static TaskPanel createCreateModuleForm(Supplier<Path> chooser, WizardController controller) {
        return new CreateModuleForm(chooser, controller);
    }
    
    // Overload for IDE usage with JFrame
    public static TaskPanel createCreateModuleForm(Supplier<Path> chooser, JFrame parentFrame) {
        return new CreateModuleForm(chooser, parentFrame);
    }

    public static TaskPanel createCreateAlgorithmForm() {
        return new CreateAlgorithmForm();
    }

    public static TaskPanel createModifySoftwareForm(Supplier<Path> chooser, WizardController controller) {
        return new ModifySoftwareForm(chooser, controller);
    }
    
    // Overload for IDE usage with JFrame
    public static TaskPanel createModifySoftwareForm(Supplier<Path> chooser, JFrame parentFrame) {
        return new ModifySoftwareForm(chooser, parentFrame);
    }

    // Package-private interface to allow ModuleEditorPanel to use task panels
    interface TaskPanel {
        void load(InitialConfig config, MainTaskData data);

        MainTaskData save(InitialConfig config);

        JPanel getComponent();
    }

    private abstract static class AbstractTaskPanel implements TaskPanel {
        private final JPanel container;

        protected AbstractTaskPanel(JPanel container) {
            this.container = container;
            this.container.setOpaque(false);
        }

        @Override
        public JPanel getComponent() {
            return container;
        }

        protected void setContent(JPanel content) {
            content.setBorder(new EmptyBorder(8, 24, 8, 24));
            container.removeAll();
            container.add(content, BorderLayout.CENTER);
            container.revalidate();
            container.repaint();
        }
    }

    // Make inner form classes package-private so ModuleEditorPanel can use them
    static final class GenerateAppForm extends AbstractTaskPanel {
        private final ProjectOverviewSection overview;
        private final MultiLineSection themeDescription;
        private final DialogListSection dialogs;
        private final WorkflowSection workflow;
        private final WizardController controller;

    GenerateAppForm(Supplier<Path> chooser, WizardController controller) {
            super(new JPanel(new BorderLayout()));
            this.controller = controller;
            overview = new ProjectOverviewSection();
            themeDescription = new MultiLineSection(
                "App Theme & Appearance",
                "Describe the desired visual theme and appearance (e.g., color scheme, light/dark mode, button styles, fonts, overall aesthetic). This guides the UI design.");
            dialogs = new DialogListSection("Screen Dialogs");
            workflow = new WorkflowSection("Workflow Items",
                "Define the core workflows, user journeys, and critical logic that the generated application must implement.",
                controller != null ? controller.getFrame() : null);
            workflow.setDialogListSection(dialogs);

            JPanel form = FormLayoutBuilder.vertical(
                overview.getComponent(),
                themeDescription.getComponent(),
                dialogs.getComponent(),
                workflow.getComponent());

            setContent(form);
        }
        
        // Constructor for IDE usage
        GenerateAppForm(Supplier<Path> chooser, JFrame parentFrame) {
            super(new JPanel(new BorderLayout()));
            this.controller = null;
            overview = new ProjectOverviewSection();
            themeDescription = new MultiLineSection(
                "App Theme & Appearance",
                "Describe the desired visual theme and appearance (e.g., color scheme, light/dark mode, button styles, fonts, overall aesthetic). This guides the UI design.");
            dialogs = new DialogListSection("Screen Dialogs");
            workflow = new WorkflowSection("Workflow Items",
                "Define the core workflows, user journeys, and critical logic that the generated application must implement.",
                parentFrame);
            workflow.setDialogListSection(dialogs);

            JPanel form = FormLayoutBuilder.vertical(
                overview.getComponent(),
                themeDescription.getComponent(),
                dialogs.getComponent(),
                workflow.getComponent());

            setContent(form);
        }

        @Override
        public void load(InitialConfig config, MainTaskData data) {
            overview.setText(data.getProjectOverview());
            themeDescription.setText(data.getThemeDescription());
            dialogs.setDialogs(data.getDialogs());
            dialogs.setMainWindowName(data.getMainWindowName());
            dialogs.setVisible(config.getProjectStyle() != ProjectStyle.SCRIPT);
            workflow.setCurrentDialogs(data.getDialogs());
            workflow.setWorkflowItems(data.getWorkflowItems());
        }

        @Override
        public MainTaskData save(InitialConfig config) {
            MainTaskData data = new MainTaskData();
            data.setProjectOverview(overview.getText());
            data.setThemeDescription(themeDescription.getText());
            data.setDialogs(dialogs.getDialogs());
            data.setMainWindowName(dialogs.getMainWindowName());
            data.setWorkflowItems(workflow.getWorkflowItems());
            return data;
        }
    }

    static final class FixCodingErrorsForm extends AbstractTaskPanel {
        private final ProjectOverviewSection overview;
        private final MultiLineSection expectedBehavior;
        private final MultiLineSection actualBehavior;
        private final MultiLineSection errorDetails;

        FixCodingErrorsForm() {
            super(new JPanel(new BorderLayout()));
            overview = new ProjectOverviewSection();
            expectedBehavior = new MultiLineSection(
                "Expected Behaviour",
                "Explain what the project should do when functioning correctly. Include user flows and success criteria.");
            actualBehavior = new MultiLineSection(
                "Actual Behaviour",
                "Describe what currently happens, including symptoms, incorrect outputs, or crashes.");
            errorDetails = new MultiLineSection(
                "Errors / Logs",
                "Paste stack traces, compiler errors, or runtime logs that capture the failure.");

            JPanel form = FormLayoutBuilder.vertical(
                overview.getComponent(),
                expectedBehavior.getComponent(),
                actualBehavior.getComponent(),
                errorDetails.getComponent());

            setContent(form);
        }

        @Override
        public void load(InitialConfig config, MainTaskData data) {
            overview.setText(data.getProjectOverview());
            expectedBehavior.setText(data.getExpectedBehavior());
            actualBehavior.setText(data.getActualBehavior());
            errorDetails.setText(data.getErrorDetails());
        }

        @Override
        public MainTaskData save(InitialConfig config) {
            MainTaskData data = new MainTaskData();
            data.setProjectOverview(overview.getText());
            data.setExpectedBehavior(expectedBehavior.getText());
            data.setActualBehavior(actualBehavior.getText());
            data.setErrorDetails(errorDetails.getText());
            return data;
        }
    }

    static final class CreateModuleForm extends AbstractTaskPanel {
        private final ProjectOverviewSection overview;
        private final MultiLineSection themeDescription;
        private final DialogListSection dialogs;
        private final WorkflowSection workflow;
        private final WizardController controller;

    CreateModuleForm(Supplier<Path> chooser, WizardController controller) {
            super(new JPanel(new BorderLayout()));
            this.controller = controller;
            overview = new ProjectOverviewSection();
            themeDescription = new MultiLineSection(
                "App Theme & Appearance",
                "Describe the desired visual theme and appearance (e.g., color scheme, light/dark mode, button styles, fonts, overall aesthetic). This guides the UI design.");
            dialogs = new DialogListSection("Module Dialogs");
            workflow = new WorkflowSection(
                "Workflow / Integration Plan",
                "Describe the expected usage flow for the new module, including entry points and critical logic.",
                controller != null ? controller.getFrame() : null);
            workflow.setDialogListSection(dialogs);

            JPanel form = FormLayoutBuilder.vertical(
                overview.getComponent(),
                themeDescription.getComponent(),
                dialogs.getComponent(),
                workflow.getComponent());

            setContent(form);
        }
        
        // Constructor for IDE usage
        CreateModuleForm(Supplier<Path> chooser, JFrame parentFrame) {
            super(new JPanel(new BorderLayout()));
            this.controller = null;
            overview = new ProjectOverviewSection();
            themeDescription = new MultiLineSection(
                "App Theme & Appearance",
                "Describe the desired visual theme and appearance (e.g., color scheme, light/dark mode, button styles, fonts, overall aesthetic). This guides the UI design.");
            dialogs = new DialogListSection("Module Dialogs");
            workflow = new WorkflowSection(
                "Workflow / Integration Plan",
                "Describe the expected usage flow for the new module, including entry points and critical logic.",
                parentFrame);
            workflow.setDialogListSection(dialogs);

            JPanel form = FormLayoutBuilder.vertical(
                overview.getComponent(),
                themeDescription.getComponent(),
                dialogs.getComponent(),
                workflow.getComponent());

            setContent(form);
        }

        @Override
        public void load(InitialConfig config, MainTaskData data) {
            overview.setText(data.getProjectOverview());
            themeDescription.setText(data.getThemeDescription());
            dialogs.setDialogs(data.getDialogs());
            dialogs.setMainWindowName(data.getMainWindowName());
            dialogs.setVisible(config.getProjectStyle() != ProjectStyle.SCRIPT);
            workflow.setCurrentDialogs(data.getDialogs());
            workflow.setWorkflowItems(data.getWorkflowItems());
        }

        @Override
        public MainTaskData save(InitialConfig config) {
            MainTaskData data = new MainTaskData();
            data.setProjectOverview(overview.getText());
            data.setThemeDescription(themeDescription.getText());
            data.setDialogs(dialogs.getDialogs());
            data.setMainWindowName(dialogs.getMainWindowName());
            data.setWorkflowItems(workflow.getWorkflowItems());
            return data;
        }
    }

    static final class CreateAlgorithmForm extends AbstractTaskPanel {
        private final MultiLineSection algorithmDescription;

        CreateAlgorithmForm() {
            super(new JPanel(new BorderLayout()));
            algorithmDescription = new MultiLineSection(
                "Algorithm Description",
                "Describe the goal, inputs, outputs, constraints, and performance considerations for the algorithm snippet.");

            JPanel form = FormLayoutBuilder.vertical(algorithmDescription.getComponent());
            setContent(form);
        }

        @Override
        public void load(InitialConfig config, MainTaskData data) {
            algorithmDescription.setText(data.getAlgorithmDescription());
        }

        @Override
        public MainTaskData save(InitialConfig config) {
            MainTaskData data = new MainTaskData();
            data.setAlgorithmDescription(algorithmDescription.getText());
            return data;
        }
    }

    static final class ModifySoftwareForm extends AbstractTaskPanel {
        private final ProjectOverviewSection overview;
        private final MultiLineSection themeDescription;
        private final MultiLineSection changeDescription;
        private final MultiLineSection involvedFiles;
        private final DialogListSection dialogs;
        private final WorkflowSection workflow;
        private final WizardController controller;

    ModifySoftwareForm(Supplier<Path> chooser, WizardController controller) {
            super(new JPanel(new BorderLayout()));
            this.controller = controller;
            overview = new ProjectOverviewSection();
            themeDescription = new MultiLineSection(
                "App Theme & Appearance",
                "Describe the desired visual theme and appearance (e.g., color scheme, light/dark mode, button styles, fonts, overall aesthetic). This guides the UI design.");
            changeDescription = new MultiLineSection(
                "Change Description",
                "Outline the modifications, refactors, or feature updates that must be applied to the existing software.");
            involvedFiles = new MultiLineSection(
                "Files / Modules Involved",
                "List the files, services, or modules that require updates, including notable constraints.");
            dialogs = new DialogListSection("Updated Dialogs");
            workflow = new WorkflowSection(
                "Updated Workflow",
                "Describe how the user journey or system workflow changes after the modifications.",
                controller != null ? controller.getFrame() : null);
            workflow.setDialogListSection(dialogs);

            JPanel form = FormLayoutBuilder.vertical(
                overview.getComponent(),
                themeDescription.getComponent(),
                changeDescription.getComponent(),
                involvedFiles.getComponent(),
                dialogs.getComponent(),
                workflow.getComponent());

            setContent(form);
        }
        
        // Constructor for IDE usage
        ModifySoftwareForm(Supplier<Path> chooser, JFrame parentFrame) {
            super(new JPanel(new BorderLayout()));
            this.controller = null;
            overview = new ProjectOverviewSection();
            themeDescription = new MultiLineSection(
                "App Theme & Appearance",
                "Describe the desired visual theme and appearance (e.g., color scheme, light/dark mode, button styles, fonts, overall aesthetic). This guides the UI design.");
            changeDescription = new MultiLineSection(
                "Change Description",
                "Outline the modifications, refactors, or feature updates that must be applied to the existing software.");
            involvedFiles = new MultiLineSection(
                "Files / Modules Involved",
                "List the files, services, or modules that require updates, including notable constraints.");
            dialogs = new DialogListSection("Updated Dialogs");
            workflow = new WorkflowSection(
                "Updated Workflow",
                "Describe how the user journey or system workflow changes after the modifications.",
                parentFrame);
            workflow.setDialogListSection(dialogs);

            JPanel form = FormLayoutBuilder.vertical(
                overview.getComponent(),
                themeDescription.getComponent(),
                changeDescription.getComponent(),
                involvedFiles.getComponent(),
                dialogs.getComponent(),
                workflow.getComponent());

            setContent(form);
        }

        @Override
        public void load(InitialConfig config, MainTaskData data) {
            overview.setText(data.getProjectOverview());
            themeDescription.setText(data.getThemeDescription());
            changeDescription.setText(data.getChangeDescription());
            involvedFiles.setText(data.getInvolvedFiles());
            dialogs.setDialogs(data.getDialogs());
            dialogs.setMainWindowName(data.getMainWindowName());
            dialogs.setVisible(config.getProjectStyle() != ProjectStyle.SCRIPT);
            workflow.setCurrentDialogs(data.getDialogs());
            workflow.setWorkflowItems(data.getWorkflowItems());
        }

        @Override
        public MainTaskData save(InitialConfig config) {
            MainTaskData data = new MainTaskData();
            data.setProjectOverview(overview.getText());
            data.setThemeDescription(themeDescription.getText());
            data.setChangeDescription(changeDescription.getText());
            data.setInvolvedFiles(involvedFiles.getText());
            data.setDialogs(dialogs.getDialogs());
            data.setMainWindowName(dialogs.getMainWindowName());
            data.setWorkflowItems(workflow.getWorkflowItems());
            return data;
        }
    }

    private static final class ProjectOverviewSection extends MultiLineSection {
        ProjectOverviewSection() {
            super("Module Overview",
                "Provide a concise overview of the project, including goals, stakeholders, and target audience.");
        }
    }

    private static final class WorkflowSection {
        private final JPanel component;
        private final javax.swing.DefaultListModel<WorkflowItem> listModel = new javax.swing.DefaultListModel<>();
        private final javax.swing.JList<WorkflowItem> list = new javax.swing.JList<>(listModel);
        private DialogListSection dialogListSection;
        private final JFrame parentFrame;

        WorkflowSection(String caption, String help, JFrame parentFrame) {
            this.parentFrame = parentFrame;
            
            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(list);
            scrollPane.setPreferredSize(new java.awt.Dimension(100, 120));
            scrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xDDE4ED)));
            
            JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
            buttonPanel.setOpaque(false);
            
            javax.swing.JButton addButton = new javax.swing.JButton("Add");
            addButton.addActionListener(event -> addWorkflowItem());
            
            javax.swing.JButton editButton = new javax.swing.JButton("Edit");
            editButton.addActionListener(event -> editWorkflowItem());
            
            javax.swing.JButton deleteButton = new javax.swing.JButton("Delete");
            deleteButton.addActionListener(event -> deleteWorkflowItem());
            
            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            
            JPanel listPanel = new JPanel(new java.awt.BorderLayout());
            listPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
            listPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
            listPanel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
            listPanel.setOpaque(false);

            this.component = UiUtils.createFieldSection(caption, caption, help, listPanel);
            component.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        }
        
        public JPanel getComponent() {
            return component;
        }
        
        public void setWorkflowItems(List<WorkflowItem> items) {
            listModel.clear();
            if (items != null) {
                for (WorkflowItem item : items) {
                    listModel.addElement(item);
                }
            }
        }
        
        public List<WorkflowItem> getWorkflowItems() {
            List<WorkflowItem> items = new ArrayList<>();
            for (int i = 0; i < listModel.size(); i++) {
                items.add(listModel.get(i));
            }
            return items;
        }
        
        public void setCurrentDialogs(List<DialogDefinition> dialogs) {
            // This method is kept for backward compatibility but is no longer used
            // The WorkflowSection now gets dialogs directly from the DialogListSection
        }
        
        public void setDialogListSection(DialogListSection section) {
            this.dialogListSection = section;
        }
        
        private void addWorkflowItem() {
            List<DialogDefinition> currentDialogs = dialogListSection != null 
                ? dialogListSection.getDialogs() 
                : new ArrayList<>();
            WorkflowItemDialog dialog = new WorkflowItemDialog(parentFrame, null, currentDialogs);
            dialog.setVisible(true);
            WorkflowItem item = dialog.getResult();
            if (item != null) {
                // Check for duplicate names
                for (int i = 0; i < listModel.size(); i++) {
                    if (listModel.get(i).getName().equalsIgnoreCase(item.getName())) {
                        UiUtils.showWarning(parentFrame, "A workflow item with this name already exists.", "Duplicate Name");
                        return;
                    }
                }
                listModel.addElement(item);
            }
        }
        
        private void editWorkflowItem() {
            int index = list.getSelectedIndex();
            if (index < 0) {
                UiUtils.showWarning(parentFrame, "Please select a workflow item to edit.", "No Selection");
                return;
            }
            WorkflowItem existing = listModel.get(index);
            List<DialogDefinition> currentDialogs = dialogListSection != null 
                ? dialogListSection.getDialogs() 
                : new ArrayList<>();
            WorkflowItemDialog dialog = new WorkflowItemDialog(parentFrame, existing, currentDialogs);
            dialog.setVisible(true);
            WorkflowItem updated = dialog.getResult();
            if (updated != null) {
                // Check for duplicate names (excluding current item)
                for (int i = 0; i < listModel.size(); i++) {
                    if (i != index && listModel.get(i).getName().equalsIgnoreCase(updated.getName())) {
                        UiUtils.showWarning(parentFrame, "A workflow item with this name already exists.", "Duplicate Name");
                        return;
                    }
                }
                listModel.set(index, updated);
            }
        }
        
        private void deleteWorkflowItem() {
            int index = list.getSelectedIndex();
            if (index < 0) {
                UiUtils.showWarning(parentFrame, "Please select a workflow item to delete.", "No Selection");
                return;
            }
            listModel.remove(index);
        }
    }
}

