/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.ui;

import com.example.formeditor.model.EventDefinition;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Optional;

/**
 * Dialog for creating or editing an EventDefinition.
 */
public class EventEditorDialog extends Dialog<EventDefinition> {

    private final TextField nameField = new TextField();
    private final TextArea triggerArea = new TextArea();
    private final TextArea actionArea = new TextArea();

    public EventEditorDialog(EventDefinition existing) {
        setTitle(existing != null && !existing.getName().isEmpty() ? "Edit Event" : "Add Event");
        setHeaderText("Define a custom event for this element");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        // Name field
        nameField.setPromptText("Event name (e.g., onSubmitClicked, onDataLoaded)");
        Label nameLabel = new Label("Event Name:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        GridPane.setHgrow(nameField, Priority.ALWAYS);

        // Trigger description
        triggerArea.setPromptText("Describe what causes this event to start...");
        triggerArea.setWrapText(true);
        triggerArea.setPrefRowCount(4);
        Label triggerLabel = new Label("What causes this event to start?");
        triggerLabel.setStyle("-fx-font-weight: bold;");
        grid.add(triggerLabel, 0, 1);
        grid.add(triggerArea, 1, 1);
        GridPane.setHgrow(triggerArea, Priority.ALWAYS);
        GridPane.setVgrow(triggerArea, Priority.ALWAYS);

        // Action description
        actionArea.setPromptText("Describe what happens when the event is triggered...");
        actionArea.setWrapText(true);
        actionArea.setPrefRowCount(4);
        Label actionLabel = new Label("What happens if the event is caused?");
        actionLabel.setStyle("-fx-font-weight: bold;");
        grid.add(actionLabel, 0, 2);
        grid.add(actionArea, 1, 2);
        GridPane.setHgrow(actionArea, Priority.ALWAYS);
        GridPane.setVgrow(actionArea, Priority.ALWAYS);

        getDialogPane().setContent(grid);
        getDialogPane().setPrefWidth(600);
        getDialogPane().setPrefHeight(450);

        // Load existing data if provided
        if (existing != null) {
            nameField.setText(existing.getName());
            triggerArea.setText(existing.getTriggerDescription());
            actionArea.setText(existing.getActionDescription());
        }

        // Enable/disable save button based on name field
        javafx.scene.Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(existing == null || existing.getName().isEmpty());
        nameField.textProperty().addListener((observable, oldValue, newValue) ->
                saveButton.setDisable(newValue.trim().isEmpty()));

        // Focus on name field
        javafx.application.Platform.runLater(() -> nameField.requestFocus());

        // Convert the result when save is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new EventDefinition(
                        nameField.getText().trim(),
                        triggerArea.getText().trim(),
                        actionArea.getText().trim()
                );
            }
            return null;
        });
    }

    /**
     * Shows the dialog and returns the result.
     */
    public static Optional<EventDefinition> showDialog(EventDefinition existing) {
        EventEditorDialog dialog = new EventEditorDialog(existing);
        return dialog.showAndWait();
    }
}
