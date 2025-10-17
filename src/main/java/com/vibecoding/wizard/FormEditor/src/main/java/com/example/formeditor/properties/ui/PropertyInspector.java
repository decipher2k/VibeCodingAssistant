/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.properties.ui;

import com.example.formeditor.model.FormModel;
import com.example.formeditor.model.GuiElementModel;
import com.example.formeditor.properties.FormPropertyDescriptors;
import com.example.formeditor.properties.PropertyDescriptor;
import com.example.formeditor.properties.PropertyKind;
import com.example.formeditor.registry.ElementRegistry;
import com.example.formeditor.ui.EventListPanel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Displays and edits properties for the selected element or the form itself.
 */
public class PropertyInspector extends VBox {

    private final Label header = new Label("Form");
    private final GridPane grid = new GridPane();
    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox contentBox = new VBox(5);
    private final EventListPanel eventListPanel = new EventListPanel();

    private final ObjectProperty<FormModel> formModel = new SimpleObjectProperty<>();
    private GuiElementModel selectedElement;

    private final Map<String, TextField> editors = new HashMap<>();
    private final Map<String, Label> errorLabels = new HashMap<>();

    private MapChangeListener<String, Object> elementListener;
    private javafx.beans.value.ChangeListener<String> idListener;
    private Runnable propertyCommittedHandler;

    public PropertyInspector() {
        getStyleClass().add("property-inspector");
        setPadding(new Insets(10));
        setSpacing(8);

        header.getStyleClass().add("property-inspector-header");
        
        // Setup content box with grid and event panel
        contentBox.setPadding(new Insets(5));
        contentBox.getChildren().addAll(grid, new Separator(), eventListPanel);
        VBox.setVgrow(eventListPanel, Priority.ALWAYS);
        
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        grid.setHgap(8);
        grid.setVgap(6);
        ColumnConstraints nameColumn = new ColumnConstraints();
        nameColumn.setPercentWidth(35);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setPercentWidth(65);
        valueColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(nameColumn, valueColumn);

        getChildren().addAll(header, scrollPane);

        formModel.addListener((obs, oldModel, newModel) -> rebuild());
    }

    public void setFormModel(FormModel model) {
        this.formModel.set(model);
        rebuild();
    }

    public void setSelectedElement(GuiElementModel element) {
        if (this.selectedElement != null && elementListener != null) {
            this.selectedElement.propsProperty().removeListener(elementListener);
        }
        if (this.selectedElement != null && idListener != null) {
            this.selectedElement.idProperty().removeListener(idListener);
        }
        this.selectedElement = element;
        if (element != null) {
            elementListener = change -> refreshValue(change.getKey());
            element.propsProperty().addListener(elementListener);
            
            idListener = (obs, oldId, newId) -> refreshIdField();
            element.idProperty().addListener(idListener);
        }
        rebuild();
    }

    public void refresh() {
        rebuild();
    }

    public void setOnPropertyCommitted(Runnable handler) {
        this.propertyCommittedHandler = handler;
    }

    private void rebuild() {
        grid.getChildren().clear();
        editors.clear();
        errorLabels.clear();

        if (selectedElement != null) {
            header.setText(selectedElement.getType() + " Properties");
            renderElementProperties();
            eventListPanel.setEvents(selectedElement.getEvents());
        } else if (formModel.get() != null) {
            header.setText("Form Properties");
            renderFormProperties();
            eventListPanel.setEvents(formModel.get().getEvents());
        } else {
            header.setText("No selection");
            eventListPanel.setEvents(null);
        }
    }

    private void renderFormProperties() {
        List<PropertyDescriptor> descriptors = FormPropertyDescriptors.descriptors();
        for (int i = 0; i < descriptors.size(); i++) {
            PropertyDescriptor descriptor = descriptors.get(i);
            String propertyName = descriptor.getName();
            addRow(i, propertyName, descriptor, () -> readFormProperty(propertyName));
        }
    }

    private void renderElementProperties() {
        // Add ID editor as the first row
        addIdRow(0);
        
        List<PropertyDescriptor> descriptors = ElementRegistry.get(selectedElement.getType()).propertyDescriptors();
        for (int i = 0; i < descriptors.size(); i++) {
            PropertyDescriptor descriptor = descriptors.get(i);
            String propertyName = descriptor.getName();
            addRow(i + 1, propertyName, descriptor, () -> Optional.ofNullable(selectedElement.getProperty(propertyName)).map(String::valueOf).orElse(""));
        }
    }

    private void addRow(int rowIndex, String propertyName, PropertyDescriptor descriptor, Supplier<String> supplier) {
        Label nameLabel = new Label(propertyName);
        nameLabel.getStyleClass().add("property-name");
        grid.add(nameLabel, 0, rowIndex);

        VBox valueBox = new VBox();
        valueBox.setSpacing(2);
        TextField editor = new TextField();
        editor.setText(supplier.get());
        editor.setDisable(descriptor.isReadOnly());
        editor.setOnAction(event -> commitValue(propertyName, descriptor, editor.getText()));
        editor.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitValue(propertyName, descriptor, editor.getText());
            }
        });

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.CRIMSON);
        errorLabel.getStyleClass().add("property-error");
        errorLabel.setVisible(false);

        valueBox.getChildren().addAll(editor, errorLabel);
        grid.add(valueBox, 1, rowIndex);

        editors.put(propertyName, editor);
        errorLabels.put(propertyName, errorLabel);
    }

    private void addIdRow(int rowIndex) {
        Label nameLabel = new Label("id");
        nameLabel.getStyleClass().add("property-name");
        nameLabel.setStyle("-fx-font-weight: bold;");
        grid.add(nameLabel, 0, rowIndex);

        VBox valueBox = new VBox();
        valueBox.setSpacing(2);
        TextField editor = new TextField();
        editor.setText(selectedElement.getId());
        editor.setPromptText("Unique element ID");
        
        editor.setOnAction(event -> commitIdValue(editor.getText()));
        
        // Store the current element for this editor instance
        final GuiElementModel elementForThisEditor = selectedElement;
        
        editor.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            // Only commit if we're losing focus and the element hasn't changed
            if (!isFocused && selectedElement == elementForThisEditor) {
                commitIdValue(editor.getText());
            }
        });

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.CRIMSON);
        errorLabel.getStyleClass().add("property-error");
        errorLabel.setVisible(false);

        valueBox.getChildren().addAll(editor, errorLabel);
        grid.add(valueBox, 1, rowIndex);

        editors.put("id", editor);
        errorLabels.put("id", errorLabel);
    }

    private void commitIdValue(String newId) {
        if (selectedElement == null || formModel.get() == null) {
            return;
        }
        
        // Validate ID is not empty
        if (newId == null || newId.trim().isEmpty()) {
            showError("id", "ID cannot be empty");
            // Revert to current ID
            TextField editor = editors.get("id");
            if (editor != null) {
                editor.setText(selectedElement.getId());
            }
            return;
        }
        
        newId = newId.trim();
        
        // Check if ID changed
        if (newId.equals(selectedElement.getId())) {
            clearError("id");
            return;
        }
        
        // Validate ID is unique
        FormModel form = formModel.get();
        for (GuiElementModel element : form.getElements()) {
            if (element != selectedElement && newId.equals(element.getId())) {
                showError("id", "ID must be unique. '" + newId + "' is already used.");
                // Revert to current ID
                TextField editor = editors.get("id");
                if (editor != null) {
                    editor.setText(selectedElement.getId());
                }
                return;
            }
        }
        
        // ID is valid and unique, apply it
        selectedElement.setId(newId);
        clearError("id");
        
        if (propertyCommittedHandler != null) {
            propertyCommittedHandler.run();
        }
    }

    private void commitValue(String propertyName, PropertyDescriptor descriptor, String rawValue) {
        if (descriptor.isReadOnly()) {
            return;
        }
        FormModel form = formModel.get();
        try {
            Object parsed = parseValue(descriptor.getKind(), rawValue);
            if (selectedElement == null) {
                applyFormProperty(propertyName, parsed, form);
            } else {
                selectedElement.setProperty(propertyName, parsed);
            }
            clearError(propertyName);
            syncEditor(propertyName);
            if (propertyCommittedHandler != null) {
                propertyCommittedHandler.run();
            }
        } catch (IllegalArgumentException ex) {
            showError(propertyName, ex.getMessage());
        }
    }

    private void applyFormProperty(String propertyName, Object value, FormModel form) {
        if (form == null) {
            return;
        }
        switch (propertyName) {
            case "name" -> form.setName(String.valueOf(value));
            case "width" -> form.setWidth(((Number) value).doubleValue());
            case "height" -> form.setHeight(((Number) value).doubleValue());
            case "background" -> form.setBackground(String.valueOf(value));
            default -> {
                // ignore unsupported properties for now
            }
        }
    }

    private Object parseValue(PropertyKind kind, String value) {
        String trimmed = value == null ? "" : value.trim();
        return switch (kind) {
            case STRING, ENUM, COLOR -> trimmed;
            case NUMBER -> {
                if (trimmed.isBlank()) {
                    throw new IllegalArgumentException("Number required");
                }
                try {
                    yield Double.parseDouble(trimmed);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number");
                }
            }
            case BOOLEAN -> {
                if (trimmed.isBlank()) {
                    yield Boolean.FALSE;
                }
                yield Boolean.parseBoolean(trimmed);
            }
            case LIST, OBJECT -> trimmed;
        };
    }

    private void refreshValue(String propertyName) {
        TextField editor = editors.get(propertyName);
        if (editor == null) {
            return;
        }
        
        // Special handling for ID field
        if ("id".equals(propertyName)) {
            if (selectedElement != null) {
                editor.setText(selectedElement.getId());
            }
            return;
        }
        
        // Regular properties
        if (selectedElement != null) {
            Object value = selectedElement.getProperty(propertyName);
            editor.setText(value == null ? "" : String.valueOf(value));
        } else {
            editor.setText(readFormProperty(propertyName));
        }
    }
    
    private void refreshIdField() {
        refreshValue("id");
    }

    private void syncEditor(String propertyName) {
        refreshValue(propertyName);
    }

    private void clearError(String propertyName) {
        Label label = errorLabels.get(propertyName);
        if (label != null) {
            label.setVisible(false);
            label.setText("");
        }
    }

    private void showError(String propertyName, String message) {
        Label label = errorLabels.get(propertyName);
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
        }
    }

    private String readFormProperty(String propertyName) {
        FormModel form = formModel.get();
        if (form == null) {
            return "";
        }
        return switch (propertyName) {
            case "name" -> form.getName();
            case "width" -> Double.toString(form.getWidth());
            case "height" -> Double.toString(form.getHeight());
            case "background" -> form.getBackground();
            default -> "";
        };
    }

}
