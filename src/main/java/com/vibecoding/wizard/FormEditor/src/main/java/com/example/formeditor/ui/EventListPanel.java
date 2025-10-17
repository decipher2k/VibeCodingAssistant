/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.ui;

import com.example.formeditor.model.EventDefinition;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * UI component for managing a list of events with CRUD operations.
 */
public class EventListPanel extends VBox {

    private final ListView<EventDefinition> eventListView = new ListView<>();
    private final Button addButton = new Button("Add");
    private final Button editButton = new Button("Edit");
    private final Button deleteButton = new Button("Delete");
    private final Button duplicateButton = new Button("Duplicate");

    private ObservableList<EventDefinition> events;

    public EventListPanel() {
        setSpacing(5);
        setPadding(new Insets(5));

        // Title label
        Label titleLabel = new Label("Events");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        // Configure list view
        eventListView.setPrefHeight(120);
        eventListView.setPlaceholder(new Label("No events defined"));
        VBox.setVgrow(eventListView, Priority.ALWAYS);

        // Button bar
        HBox buttonBar = new HBox(5);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        addButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        duplicateButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(addButton, Priority.ALWAYS);
        HBox.setHgrow(editButton, Priority.ALWAYS);
        HBox.setHgrow(deleteButton, Priority.ALWAYS);
        HBox.setHgrow(duplicateButton, Priority.ALWAYS);
        buttonBar.getChildren().addAll(addButton, editButton, deleteButton, duplicateButton);

        // Add components
        getChildren().addAll(titleLabel, eventListView, buttonBar);

        // Setup button actions
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        duplicateButton.setOnAction(e -> handleDuplicate());

        // Update button states based on selection
        eventListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> updateButtonStates());
        updateButtonStates();
    }

    /**
     * Sets the observable list of events to manage.
     */
    public void setEvents(ObservableList<EventDefinition> events) {
        this.events = events;
        eventListView.setItems(events);
        updateButtonStates();
    }

    private void handleAdd() {
        if (events == null) {
            return;
        }
        Optional<EventDefinition> result = EventEditorDialog.showDialog(null);
        result.ifPresent(events::add);
    }

    private void handleEdit() {
        EventDefinition selected = eventListView.getSelectionModel().getSelectedItem();
        if (selected == null || events == null) {
            return;
        }

        Optional<EventDefinition> result = EventEditorDialog.showDialog(selected);
        result.ifPresent(edited -> {
            int index = events.indexOf(selected);
            if (index >= 0) {
                events.set(index, edited);
                eventListView.getSelectionModel().select(edited);
            }
        });
    }

    private void handleDelete() {
        EventDefinition selected = eventListView.getSelectionModel().getSelectedItem();
        if (selected == null || events == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Event");
        confirm.setHeaderText("Delete event \"" + selected.getName() + "\"?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            events.remove(selected);
        }
    }

    private void handleDuplicate() {
        EventDefinition selected = eventListView.getSelectionModel().getSelectedItem();
        if (selected == null || events == null) {
            return;
        }

        EventDefinition duplicate = new EventDefinition(
                selected.getName() + " (copy)",
                selected.getTriggerDescription(),
                selected.getActionDescription()
        );
        events.add(duplicate);
        eventListView.getSelectionModel().select(duplicate);
    }

    private void updateButtonStates() {
        boolean hasSelection = eventListView.getSelectionModel().getSelectedItem() != null;
        boolean hasEvents = events != null && !events.isEmpty();
        
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        duplicateButton.setDisable(!hasSelection);
        addButton.setDisable(events == null);
    }
}
