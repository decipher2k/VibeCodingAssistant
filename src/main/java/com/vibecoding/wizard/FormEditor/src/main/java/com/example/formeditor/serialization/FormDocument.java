/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DTO representing the persisted form document.
 */
public record FormDocument(
        String schemaVersion,
        FormDefinition form,
        List<ElementDefinition> elements
) {
    @JsonCreator
    public FormDocument(
            @JsonProperty(value = "schemaVersion", required = true) String schemaVersion,
            @JsonProperty(value = "form", required = true) FormDefinition form,
            @JsonProperty(value = "elements") List<ElementDefinition> elements) {
        this.schemaVersion = schemaVersion;
        this.form = form;
        this.elements = elements == null ? List.of() : List.copyOf(elements);
    }

    public record FormDefinition(
            String id,
            String name,
            double width,
            double height,
            String background,
            List<EventDto> events
    ) {
        @JsonCreator
        public FormDefinition(
                @JsonProperty(value = "id", required = true) String id,
                @JsonProperty(value = "name", required = true) String name,
                @JsonProperty(value = "width", required = true) double width,
                @JsonProperty(value = "height", required = true) double height,
                @JsonProperty(value = "background") String background,
                @JsonProperty(value = "events") List<EventDto> events) {
            this.id = id;
            this.name = name;
            this.width = width;
            this.height = height;
            this.background = background;
            this.events = events == null ? List.of() : List.copyOf(events);
        }
    }

    public record ElementDefinition(
            String id,
            String type,
            Map<String, Object> props,
            List<EventDto> events
    ) {
        @JsonCreator
        public ElementDefinition(
                @JsonProperty(value = "id", required = true) String id,
                @JsonProperty(value = "type", required = true) String type,
                @JsonProperty(value = "props") Map<String, Object> props,
                @JsonProperty(value = "events") List<EventDto> events) {
            this.id = id;
            this.type = type;
            this.props = props == null ? Map.of() : Collections.unmodifiableMap(props);
            this.events = events == null ? List.of() : List.copyOf(events);
        }
    }

    public record EventDto(
            String name,
            String triggerDescription,
            String actionDescription
    ) {
        @JsonCreator
        public EventDto(
                @JsonProperty(value = "name", required = true) String name,
                @JsonProperty(value = "triggerDescription") String triggerDescription,
                @JsonProperty(value = "actionDescription") String actionDescription) {
            this.name = name;
            this.triggerDescription = triggerDescription == null ? "" : triggerDescription;
            this.actionDescription = actionDescription == null ? "" : actionDescription;
        }
    }
}
