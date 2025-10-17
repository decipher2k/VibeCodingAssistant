/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.properties;

import java.util.List;

/**
 * Collection of commonly used property descriptors.
 */
public final class StandardProperties {

    public static final PropertyDescriptor X = PropertyDescriptor.builder("x", PropertyKind.NUMBER)
            .defaultValue(40)
            .layoutProperty(true)
            .build();

    public static final PropertyDescriptor Y = PropertyDescriptor.builder("y", PropertyKind.NUMBER)
            .defaultValue(40)
            .layoutProperty(true)
            .build();

    public static final PropertyDescriptor WIDTH = PropertyDescriptor.builder("width", PropertyKind.NUMBER)
            .defaultValue(120)
            .layoutProperty(true)
            .build();

    public static final PropertyDescriptor HEIGHT = PropertyDescriptor.builder("height", PropertyKind.NUMBER)
            .defaultValue(32)
            .layoutProperty(true)
            .build();

    public static final PropertyDescriptor TEXT = PropertyDescriptor.builder("text", PropertyKind.STRING)
            .defaultValue("Text")
            .build();

    public static final PropertyDescriptor FONT_FAMILY = PropertyDescriptor.builder("fontFamily", PropertyKind.STRING)
            .defaultValue("System")
            .build();

    public static final PropertyDescriptor FONT_SIZE = PropertyDescriptor.builder("fontSize", PropertyKind.NUMBER)
            .defaultValue(14)
            .build();

    public static final PropertyDescriptor FOREGROUND = PropertyDescriptor.builder("foreground", PropertyKind.COLOR)
            .defaultValue("#000000")
            .build();

    public static final PropertyDescriptor BACKGROUND = PropertyDescriptor.builder("background", PropertyKind.COLOR)
            .defaultValue("transparent")
            .build();

    private StandardProperties() {
    }

    public static List<PropertyDescriptor> layoutDescriptors() {
        return List.of(X, Y, WIDTH, HEIGHT);
    }
}
