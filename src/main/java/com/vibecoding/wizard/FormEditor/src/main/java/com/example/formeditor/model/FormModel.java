/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the form along with all of its elements.
 */
public class FormModel {

    private final StringProperty id = new SimpleStringProperty(this, "id", UUID.randomUUID().toString());
    private final StringProperty name = new SimpleStringProperty(this, "name", "MainForm");
    private final DoubleProperty width = new SimpleDoubleProperty(this, "width", 400);
    private final DoubleProperty height = new SimpleDoubleProperty(this, "height", 400);
    private final StringProperty background = new SimpleStringProperty(this, "background", "#F0F0F0");
    private final ObservableList<GuiElementModel> elements = FXCollections.observableArrayList();
    private final ObservableList<EventDefinition> events = FXCollections.observableArrayList();

    public FormModel() {
    }

    public FormModel(String id, String name, double width, double height, String background, List<GuiElementModel> elements) {
        this.id.set(Objects.requireNonNull(id, "id"));
        this.name.set(Objects.requireNonNull(name, "name"));
        this.width.set(width);
        this.height.set(height);
        this.background.set(Optional.ofNullable(background).orElse("#FFFFFF"));
        this.elements.setAll(elements);
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(Objects.requireNonNull(id));
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(Objects.requireNonNull(name));
    }

    public StringProperty nameProperty() {
        return name;
    }

    public double getWidth() {
        return width.get();
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public double getHeight() {
        return height.get();
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public String getBackground() {
        return background.get();
    }

    public void setBackground(String background) {
        this.background.set(Optional.ofNullable(background).orElse("transparent"));
    }

    public StringProperty backgroundProperty() {
        return background;
    }

    public ObservableList<GuiElementModel> getElements() {
        return elements;
    }

    public void addElement(GuiElementModel model) {
        elements.add(Objects.requireNonNull(model));
    }

    public void removeElement(GuiElementModel model) {
        elements.remove(model);
    }

    public GuiElementModel findElementById(String elementId) {
        return elements.stream()
                .filter(e -> Objects.equals(e.getId(), elementId))
                .findFirst()
                .orElse(null);
    }

    public void clearElements() {
        elements.clear();
    }

    public ObservableList<EventDefinition> getEvents() {
        return events;
    }

    public void setEvents(List<EventDefinition> eventList) {
        events.clear();
        if (eventList != null) {
            events.addAll(eventList);
        }
    }

    public FormModel copy() {
        FormModel copy = new FormModel();
        copy.setId(getId());
        copy.setName(getName());
        copy.setWidth(getWidth());
        copy.setHeight(getHeight());
        copy.setBackground(getBackground());
        elements.forEach(element ->
                copy.addElement(new GuiElementModel(element.getId(), element.getType(), element.toPersistentProperties())));
        events.forEach(event -> copy.events.add(event.copy()));
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormModel that)) return false;
        return Double.compare(that.getWidth(), getWidth()) == 0 &&
                Double.compare(that.getHeight(), getHeight()) == 0 &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getBackground(), that.getBackground()) &&
                Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getWidth(), getHeight(), getBackground(), elements);
    }
}
