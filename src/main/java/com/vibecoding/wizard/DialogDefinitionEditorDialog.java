/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public final class DialogDefinitionEditorDialog extends JDialog {
    private final JTextField nameField = new JTextField(30);
    private final JTextField windowTitleField = new JTextField(30);
    private final JTextArea descriptionArea = new JTextArea(6, 30);
    private final JCheckBox modalCheckBox = new JCheckBox("Run as modal dialog");
    private final JCheckBox showInMdiMenuCheckBox = new JCheckBox("Show in MDI menu");
    private final JLabel layoutStatusLabel = new JLabel();
    private Optional<DialogDefinition> result = Optional.empty();
    private String formLayoutJson;

    public DialogDefinitionEditorDialog(JComponent parent, DialogDefinition existing) {
        super(resolveParent(parent), "Dialog Details", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel();
        form.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(6, 6, 6, 6);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Dialog Name with help button
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        namePanel.setOpaque(false);
        namePanel.add(new JLabel("Dialog Name"));
        namePanel.add(UiUtils.createHelpButton(
            "Dialog Name",
            "Specify a unique identifier for this dialog. This name will be used internally to reference the dialog in your code.",
            this));
        form.add(namePanel, gbc);
        gbc.gridx = 1;
        form.add(nameField, gbc);

        // Window Title with help button
        gbc.gridx = 0;
        gbc.gridy++;
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(new JLabel("Window Title"));
        titlePanel.add(UiUtils.createHelpButton(
            "Window Title",
            "The text that will appear in the title bar of the dialog window when it is displayed to the user.",
            this));
        form.add(titlePanel, gbc);
        gbc.gridx = 1;
        form.add(windowTitleField, gbc);

        // Description with help button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel descPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        descPanel.setOpaque(false);
        descPanel.add(new JLabel("Description"));
        descPanel.add(UiUtils.createHelpButton(
            "Description",
            "Provide a detailed description of the dialog's purpose and the information it will collect or display. This helps with code generation and documentation.",
            this));
        form.add(descPanel, gbc);

        gbc.gridy++;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        form.add(new JScrollPane(descriptionArea), gbc);

        // Modal checkbox with help button
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        JPanel modalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        modalPanel.setOpaque(false);
        modalCheckBox.setOpaque(false);
        modalPanel.add(modalCheckBox);
        modalPanel.add(UiUtils.createHelpButton(
            "Modal Dialog",
            "If checked, the dialog will be modal, meaning it blocks interaction with other windows until closed. Uncheck for modeless dialogs that allow interaction with other windows.",
            this));
        form.add(modalPanel, gbc);

        // Show in MDI menu checkbox with help button
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        JPanel mdiMenuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        mdiMenuPanel.setOpaque(false);
        showInMdiMenuCheckBox.setOpaque(false);
        showInMdiMenuCheckBox.setSelected(true); // Checked by default
        mdiMenuPanel.add(showInMdiMenuCheckBox);
        mdiMenuPanel.add(UiUtils.createHelpButton(
            "Show in MDI Menu",
            "If checked (in MDI mode), this window will appear in the MDI parent's menu and can be opened by the user. " +
            "Uncheck for windows that are only shown as subwindows within other windows (e.g., using WindowHost controls).",
            this));
        form.add(mdiMenuPanel, gbc);

        content.add(form, BorderLayout.CENTER);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dispose());

        JButton defineDialogButton = new JButton("Visual Editor");
        defineDialogButton.addActionListener(event -> handleDefineDialog());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(event -> handleSave());

        // Layout status label
        layoutStatusLabel.setForeground(new Color(0, 128, 0));
        layoutStatusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        JPanel footer = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(defineDialogButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        footer.add(layoutStatusLabel, BorderLayout.WEST);
        footer.add(buttonPanel, BorderLayout.EAST);

        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(parent);

        if (existing != null) {
            nameField.setText(existing.getName());
            windowTitleField.setText(existing.getWindowTitle());
            descriptionArea.setText(existing.getDescription());
            modalCheckBox.setSelected(existing.isModal());
            showInMdiMenuCheckBox.setSelected(existing.isShowInMdiMenu());
            formLayoutJson = existing.getFormLayoutJson();
            updateLayoutStatus();
        }
    }

    private static Window resolveParent(JComponent component) {
        return component == null ? null : SwingUtilities.getWindowAncestor(component);
    }
    
    private void updateLayoutStatus() {
        if (formLayoutJson != null && !formLayoutJson.trim().isEmpty()) {
            layoutStatusLabel.setText("✓ Layout defined");
        } else {
            layoutStatusLabel.setText("");
        }
    }

    private void handleDefineDialog() {
        String dialogName = nameField.getText().trim();
        if (dialogName.isEmpty()) {
            dialogName = "Dialog";
        }
        
        System.out.println("handleDefineDialog called with name: " + dialogName);
        
        // Launch the form editor asynchronously to avoid blocking the EDT
        final String finalDialogName = dialogName;
        FormEditorLauncher.openFormEditor(
            formLayoutJson, 
            finalDialogName,
            this  // Pass this dialog as the parent window
        ).thenAccept(resultJson -> {
            System.out.println("FormEditor returned with JSON: " + (resultJson != null ? "Yes" : "null"));
            // This runs on a different thread, so we need to use SwingUtilities.invokeLater
            SwingUtilities.invokeLater(() -> {
                if (resultJson != null) {
                    formLayoutJson = resultJson;
                    updateLayoutStatus();
                    JOptionPane.showMessageDialog(this,
                        "Dialog layout has been defined successfully.\n\n" +
                        "Don't forget to click 'Save' to add this window to the windows list!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }).exceptionally(e -> {
            System.err.println("FormEditor exception: " + e.getMessage());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "Failed to open form editor: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            });
            return null;
        });
        
        System.out.println("handleDefineDialog completed, FormEditor should be launching");
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String title = windowTitleField.getText().trim();
        String description = descriptionArea.getText().trim();

        System.out.println("handleSave called - name: '" + name + "', title: '" + title + "'");
        System.out.println("formLayoutJson present: " + (formLayoutJson != null && !formLayoutJson.trim().isEmpty()));

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please provide a dialog name.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please provide a window title.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        result = Optional.of(new DialogDefinition(name, title, description, modalCheckBox.isSelected(), formLayoutJson, showInMdiMenuCheckBox.isSelected()));
        System.out.println("DialogDefinition created and result set");
        dispose();
    }

    public Optional<DialogDefinition> getResult() {
        return result;
    }
}
