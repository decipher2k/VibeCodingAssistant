/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.model;

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single GUI element on the form canvas.
 */
public class GuiElementModel {

    private final StringProperty id = new SimpleStringProperty(this, "id", UUID.randomUUID().toString());
    private final StringProperty type = new SimpleStringProperty(this, "type");
    private final MapProperty<String, Object> props = new SimpleMapProperty<>(this, "props", FXCollections.observableHashMap());
    private final ObservableList<EventDefinition> events = FXCollections.observableArrayList();

    public GuiElementModel(String type) {
        this(UUID.randomUUID().toString(), type, new LinkedHashMap<>());
    }

    public GuiElementModel(String id, String type, Map<String, Object> props) {
        this.id.set(Objects.requireNonNull(id, "id"));
        this.type.set(Objects.requireNonNull(type, "type"));
    ObservableMap<String, Object> observableProps = FXCollections.observableMap(new LinkedHashMap<>(Objects.requireNonNull(props, "props")));
    this.props.clear();
    observableProps.forEach((key, value) -> this.props.put(key, normaliseValue(value)));
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(Objects.requireNonNull(id));
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(Objects.requireNonNull(type));
    }

    public ObservableMap<String, Object> getProps() {
        return props.get();
    }

    public MapProperty<String, Object> propsProperty() {
        return props;
    }

    public void setProps(Map<String, Object> props) {
    this.props.clear();
    props.forEach((key, value) -> this.props.put(key, normaliseValue(value)));
    }

    public Object getProperty(String name) {
        return props.get(name);
    }

    public void setProperty(String name, Object value) {
        if (value == null) {
            props.remove(name);
        } else {
            props.put(name, normaliseValue(value));
        }
    }

    public double getLayoutX() {
        return getNumberProperty("x", 0d);
    }

    public double getLayoutY() {
        return getNumberProperty("y", 0d);
    }

    public double getWidth() {
        return getNumberProperty("width", 80d);
    }

    public double getHeight() {
        return getNumberProperty("height", 24d);
    }

    public void setLayoutBounds(double x, double y, double width, double height) {
        setProperty("x", x);
        setProperty("y", y);
        setProperty("width", width);
        setProperty("height", height);
    }

    private double getNumberProperty(String key, double defaultValue) {
        Object value = props.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                // fallback to default if parsing fails
            }
        }
        return defaultValue;
    }

    public Map<String, Object> toPersistentProperties() {
        return new LinkedHashMap<>(props);
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

    private Object normaliseValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuiElementModel that)) return false;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(props, that.props);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), props);
    }

    @Override
    public String toString() {
        return "GuiElementModel{" +
                "id='" + getId() + '\'' +
                ", type='" + getType() + '\'' +
                ", props=" + props +
                '}';
    }
}
