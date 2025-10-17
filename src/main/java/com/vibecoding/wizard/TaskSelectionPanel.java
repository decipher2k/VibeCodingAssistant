/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

public final class TaskSelectionPanel extends JPanel {
    private final WizardController controller;
    private final Map<TaskType, JRadioButton> taskButtons = new HashMap<>();
    private final ButtonGroup buttonGroup = new ButtonGroup();

    public TaskSelectionPanel(WizardController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 24, 16, 24));
        setOpaque(true);
        setBackground(ThemeManager.getBackgroundColor());

        JPanel groupContainer = new JPanel();
        groupContainer.setLayout(new BoxLayout(groupContainer, BoxLayout.Y_AXIS));
        groupContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    groupContainer.setOpaque(false);

        JLabel headerLabel = new JLabel("Select Task");
        JButton helpButton = UiUtils.createHelpButton(
            "Task Types",
            "Choose the automation you would like GitHub Copilot CLI to perform. Each task exposes a tailored main dialog to capture all required details.",
            this);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.add(headerLabel);
        header.add(helpButton);

        add(header, BorderLayout.NORTH);

        for (TaskType task : TaskType.values()) {
            JRadioButton button = new JRadioButton(task.toString());
            button.setOpaque(false);
            taskButtons.put(task, button);
            buttonGroup.add(button);
            groupContainer.add(button);
        }

        add(groupContainer, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> controller.goBackToInitial());

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(event -> handleNext());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(backButton);
        footer.add(nextButton);

        add(footer, BorderLayout.SOUTH);
    }

    void refreshSelection(TaskType taskType) {
        if (taskType == null) {
            buttonGroup.clearSelection();
            return;
        }
        JRadioButton button = taskButtons.get(taskType);
        if (button != null) {
            button.setSelected(true);
        }
    }

    private void handleNext() {
        TaskType selected = taskButtons.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);

        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a task to continue.",
                "Task Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        controller.submitTaskType(selected);
    }
}
