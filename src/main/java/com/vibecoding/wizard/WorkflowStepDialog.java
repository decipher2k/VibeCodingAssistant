/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class WorkflowStepDialog extends JDialog {
    private final JTextArea descriptionArea = new JTextArea(6, 40);
    private final JTextArea requirementsArea = new JTextArea(4, 40);
    private final JCheckBox stopIfNotMetCheckbox = new JCheckBox("Stop workflow if requirements are not met");
    private final JCheckBox waitForRequirementCheckbox = new JCheckBox("Wait for requirement to be met before continuing rest of workflow");
    private WorkflowStep result;

    public WorkflowStepDialog(Window owner, WorkflowStep step) {
        super(owner, "Workflow Step", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8, 8));
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Step description
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        JPanel descPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        descPanel.setOpaque(false);
        descPanel.add(new JLabel("Step description:"));
        descPanel.add(UiUtils.createHelpButton(
            "Step Description",
            "Describe what happens in this step of the workflow. Be specific about the action or process that occurs. For example: 'User enters their email address' or 'System validates the input data'.",
            this));
        contentPanel.add(descPanel, gbc);
        
        gbc.gridy = 1;
        gbc.weighty = 0.5;
        if (step != null) {
            descriptionArea.setText(step.getDescription());
        }
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        contentPanel.add(descScrollPane, gbc);
        
        // Requirements
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        JPanel reqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        reqPanel.setOpaque(false);
        reqPanel.add(new JLabel("Requirements for this step (optional):"));
        reqPanel.add(UiUtils.createHelpButton(
            "Step Requirements",
            "Specify any preconditions or requirements that must be met for this step to execute successfully. For example: 'User must be logged in' or 'File must exist in the specified directory'. Leave empty if there are no specific requirements.",
            this));
        contentPanel.add(reqPanel, gbc);
        
        gbc.gridy = 3;
        gbc.weighty = 0.4;
        if (step != null) {
            requirementsArea.setText(step.getRequirements());
        }
        requirementsArea.setLineWrap(true);
        requirementsArea.setWrapStyleWord(true);
        JScrollPane reqScrollPane = new JScrollPane(requirementsArea);
        contentPanel.add(reqScrollPane, gbc);
        
        // Checkbox for stopping workflow
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        if (step != null) {
            stopIfNotMetCheckbox.setSelected(step.isStopIfRequirementNotMet());
        }
        stopIfNotMetCheckbox.setOpaque(false);
        JPanel stopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        stopPanel.setOpaque(false);
        stopPanel.add(stopIfNotMetCheckbox);
        stopPanel.add(UiUtils.createHelpButton(
            "Stop If Requirements Not Met",
            "When checked, the entire workflow will stop immediately if the requirements for this step are not satisfied. Use this for critical requirements that must be met to continue.",
            this));
        contentPanel.add(stopPanel, gbc);
        
        // Checkbox for waiting for requirement
        gbc.gridy = 5;
        if (step != null) {
            waitForRequirementCheckbox.setSelected(step.isWaitForRequirement());
        }
        waitForRequirementCheckbox.setOpaque(false);
        JPanel waitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        waitPanel.setOpaque(false);
        waitPanel.add(waitForRequirementCheckbox);
        waitPanel.add(UiUtils.createHelpButton(
            "Wait For Requirement",
            "When checked, the workflow will pause at this step and wait until the requirements are met before continuing. This is useful for steps that depend on external conditions or user actions.",
            this));
        contentPanel.add(waitPanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(event -> {
            String desc = descriptionArea.getText();
            if (desc == null || desc.isBlank()) {
                UiUtils.showWarning(this, "Please enter a step description.", "Missing Description");
                return;
            }
            result = new WorkflowStep(
                desc.trim(), 
                requirementsArea.getText().trim(),
                stopIfNotMetCheckbox.isSelected(),
                waitForRequirementCheckbox.isSelected()
            );
            dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(owner);
    }
    
    public WorkflowStep getResult() {
        return result;
    }
}
