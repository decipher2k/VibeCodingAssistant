/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.registry;

import com.example.formeditor.model.GuiElementModel;
import javafx.scene.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry responsible for mapping element type identifiers to their handlers.
 */
public final class ElementRegistry {

    private static final Map<String, ElementType> TYPES = new ConcurrentHashMap<>();

    private ElementRegistry() {
    }

    public static void register(ElementType type) {
        Objects.requireNonNull(type, "type");
        TYPES.put(type.type(), type);
    }

    public static ElementType get(String type) {
        ElementType elementType = TYPES.get(type);
        if (elementType == null) {
            throw new IllegalArgumentException("Unknown element type: " + type);
        }
        return elementType;
    }

    public static boolean isRegistered(String type) {
        return TYPES.containsKey(type);
    }

    public static Collection<ElementType> all() {
        return Collections.unmodifiableCollection(TYPES.values());
    }

    public static void clear() {
        TYPES.clear();
    }

    public static GuiElementModel createDefault(String type, double x, double y) {
        return get(type).createDefaultModel(x, y);
    }

    public static Node createNode(GuiElementModel model) {
        return get(model.getType()).createNode(model);
    }

    public static void applyProperties(GuiElementModel model, Node node) {
        get(model.getType()).applyProperties(model, node);
    }
}
