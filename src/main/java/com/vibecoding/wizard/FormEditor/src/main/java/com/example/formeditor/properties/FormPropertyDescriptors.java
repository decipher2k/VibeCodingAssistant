/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.properties;

import java.util.List;

/**
 * Descriptor definitions for form-level properties.
 */
public final class FormPropertyDescriptors {

    public static final PropertyDescriptor NAME = PropertyDescriptor.builder("name", PropertyKind.STRING)
            .defaultValue("Form")
            .build();

    public static final PropertyDescriptor WIDTH = PropertyDescriptor.builder("width", PropertyKind.NUMBER)
            .defaultValue(800)
            .layoutProperty(true)
            .build();

    public static final PropertyDescriptor HEIGHT = PropertyDescriptor.builder("height", PropertyKind.NUMBER)
            .defaultValue(600)
            .layoutProperty(true)
            .build();

    public static final PropertyDescriptor BACKGROUND = PropertyDescriptor.builder("background", PropertyKind.COLOR)
            .defaultValue("#F0F0F0")
            .build();

    private FormPropertyDescriptors() {
    }

    public static List<PropertyDescriptor> descriptors() {
        return List.of(NAME, WIDTH, HEIGHT, BACKGROUND);
    }
}
