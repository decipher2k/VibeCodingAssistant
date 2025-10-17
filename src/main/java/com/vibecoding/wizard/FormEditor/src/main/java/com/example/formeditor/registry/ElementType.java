/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.registry;

import com.example.formeditor.model.GuiElementModel;
import com.example.formeditor.properties.PropertyDescriptor;
import javafx.scene.Node;

import java.util.List;

/**
 * Defines behaviour for a specific element type supported by the editor.
 */
public interface ElementType {

    String type();

    /**
     * Creates a JavaFX node instance for the given element.
     */
    Node createNode(GuiElementModel model);

    /**
     * Applies the model properties to the given JavaFX node.
     */
    void applyProperties(GuiElementModel model, Node node);

    /**
     * Creates a new model with sensible defaults at the requested position.
     */
    GuiElementModel createDefaultModel(double x, double y);

    /**
     * Property descriptors for this element type.
     */
    List<PropertyDescriptor> propertyDescriptors();
}
