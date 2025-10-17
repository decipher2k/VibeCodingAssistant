/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.registry;

import com.example.formeditor.model.GuiElementModel;
import com.example.formeditor.properties.PropertyDescriptor;
import javafx.scene.Node;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Convenience implementation of {@link ElementType} for simple JavaFX nodes.
 */
public class SimpleElementType implements ElementType {

    private final String type;
    private final Supplier<Node> nodeSupplier;
    private final BiConsumer<GuiElementModel, Node> applier;
    private final List<PropertyDescriptor> descriptors;
    private final Map<String, Object> defaults;

    public SimpleElementType(String type,
                              Supplier<Node> nodeSupplier,
                              BiConsumer<GuiElementModel, Node> applier,
                              List<PropertyDescriptor> descriptors,
                              Map<String, Object> defaults) {
        this.type = Objects.requireNonNull(type, "type");
        this.nodeSupplier = Objects.requireNonNull(nodeSupplier, "nodeSupplier");
        this.applier = Objects.requireNonNull(applier, "applier");
        this.descriptors = List.copyOf(descriptors);
        this.defaults = new LinkedHashMap<>(Objects.requireNonNull(defaults, "defaults"));
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Node createNode(GuiElementModel model) {
        Node node = nodeSupplier.get();
        applyProperties(model, node);
        return node;
    }

    @Override
    public void applyProperties(GuiElementModel model, Node node) {
        applier.accept(model, node);
    }

    @Override
    public GuiElementModel createDefaultModel(double x, double y) {
        Map<String, Object> props = new LinkedHashMap<>(defaults);
        props.putIfAbsent("width", 120d);
        props.putIfAbsent("height", 32d);
        props.put("x", x);
        props.put("y", y);
        GuiElementModel model = new GuiElementModel(type);
        model.setProps(props);
        return model;
    }

    @Override
    public List<PropertyDescriptor> propertyDescriptors() {
        return descriptors;
    }
}
