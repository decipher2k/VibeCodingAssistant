/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.properties;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes a property that can be edited within the form editor.
 */
public final class PropertyDescriptor {

    private final String name;
    private final PropertyKind kind;
    private final Object defaultValue;
    private final boolean layoutProperty;
    private final boolean readOnly;

    private PropertyDescriptor(Builder builder) {
        this.name = builder.name;
        this.kind = builder.kind;
        this.defaultValue = builder.defaultValue;
        this.layoutProperty = builder.layoutProperty;
        this.readOnly = builder.readOnly;
    }

    public String getName() {
        return name;
    }

    public PropertyKind getKind() {
        return kind;
    }

    public Optional<Object> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public boolean isLayoutProperty() {
        return layoutProperty;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public static Builder builder(String name, PropertyKind kind) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(kind, "kind");
        return new Builder(name, kind);
    }

    public static final class Builder {
        private final String name;
        private final PropertyKind kind;
        private Object defaultValue;
        private boolean layoutProperty;
        private boolean readOnly;

        private Builder(String name, PropertyKind kind) {
            this.name = name;
            this.kind = kind;
        }

        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder layoutProperty(boolean layoutProperty) {
            this.layoutProperty = layoutProperty;
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public PropertyDescriptor build() {
            return new PropertyDescriptor(this);
        }
    }
}
