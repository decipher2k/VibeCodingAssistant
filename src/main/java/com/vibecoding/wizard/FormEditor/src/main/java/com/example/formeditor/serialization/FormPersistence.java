/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.serialization;

import com.example.formeditor.model.EventDefinition;
import com.example.formeditor.model.FormModel;
import com.example.formeditor.model.GuiElementModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides JSON serialization and deserialization utilities for {@link FormModel}.
 */
public final class FormPersistence {

    public static final String SCHEMA_VERSION = "1.0";

    private final ObjectMapper mapper;

    public FormPersistence() {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String toJson(FormModel model) {
        Objects.requireNonNull(model, "model");
        FormDocument document = toDocument(model);
        try {
            return mapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            throw new FormSerializationException("Failed to serialize form to JSON", e);
        }
    }

    public void write(FormModel model, OutputStream outputStream) {
        Objects.requireNonNull(outputStream, "outputStream");
        String json = toJson(model);
        try {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new FormSerializationException("Failed to write JSON to output stream", e);
        }
    }

    public FormModel fromJson(String json) {
        Objects.requireNonNull(json, "json");
        try {
            FormDocument document = mapper.readValue(json, FormDocument.class);
            return fromDocument(document);
        } catch (IOException e) {
            throw new FormSerializationException("Failed to parse form JSON", e);
        }
    }

    public FormModel fromJson(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream");
        try {
            FormDocument document = mapper.readValue(inputStream, FormDocument.class);
            return fromDocument(document);
        } catch (IOException e) {
            throw new FormSerializationException("Failed to parse form JSON", e);
        }
    }

    private FormDocument toDocument(FormModel model) {
        List<FormDocument.EventDto> formEvents = model.getEvents().stream()
                .map(event -> new FormDocument.EventDto(
                        event.getName(),
                        event.getTriggerDescription(),
                        event.getActionDescription()))
                .collect(Collectors.toList());

        FormDocument.FormDefinition definition = new FormDocument.FormDefinition(
                model.getId(),
                model.getName(),
                model.getWidth(),
                model.getHeight(),
                model.getBackground(),
                formEvents
        );

        List<FormDocument.ElementDefinition> elements = model.getElements().stream()
                .map(element -> {
                    List<FormDocument.EventDto> elementEvents = element.getEvents().stream()
                            .map(event -> new FormDocument.EventDto(
                                    event.getName(),
                                    event.getTriggerDescription(),
                                    event.getActionDescription()))
                            .collect(Collectors.toList());
                    
                    return new FormDocument.ElementDefinition(
                            element.getId(),
                            element.getType(),
                            toSerializableProps(element),
                            elementEvents);
                })
                .collect(Collectors.toList());

        return new FormDocument(SCHEMA_VERSION, definition, elements);
    }

    private FormModel fromDocument(FormDocument document) {
        Objects.requireNonNull(document, "document");
        if (!SCHEMA_VERSION.equals(document.schemaVersion())) {
            throw new FormSerializationException("Unsupported schema version: " + document.schemaVersion());
        }

        FormDocument.FormDefinition formDef = document.form();
        if (formDef == null) {
            throw new FormSerializationException("Missing form definition in JSON document");
        }

        FormModel model = new FormModel(
                formDef.id(),
                formDef.name(),
                formDef.width(),
                formDef.height(),
                formDef.background(),
                List.of()
        );

        // Load form events
        if (formDef.events() != null) {
            formDef.events().forEach(eventDto -> {
                EventDefinition event = new EventDefinition(
                        eventDto.name(),
                        eventDto.triggerDescription(),
                        eventDto.actionDescription()
                );
                model.getEvents().add(event);
            });
        }

        // Load elements with their events
        document.elements().forEach(elementDef -> {
            Map<String, Object> props = new LinkedHashMap<>(elementDef.props());
            GuiElementModel element = new GuiElementModel(elementDef.id(), elementDef.type(), props);
            
            // Load element events
            if (elementDef.events() != null) {
                elementDef.events().forEach(eventDto -> {
                    EventDefinition event = new EventDefinition(
                            eventDto.name(),
                            eventDto.triggerDescription(),
                            eventDto.actionDescription()
                    );
                    element.getEvents().add(event);
                });
            }
            
            model.addElement(element);
        });

        return model;
    }

    private Map<String, Object> toSerializableProps(GuiElementModel element) {
        Map<String, Object> props = new LinkedHashMap<>();
        element.getProps().forEach((key, value) -> {
            if (value instanceof Number number) {
                props.put(key, normaliseNumber(number));
            } else {
                props.put(key, value);
            }
        });
        return props;
    }

    private Number normaliseNumber(Number number) {
        double asDouble = number.doubleValue();
        if (Math.floor(asDouble) == asDouble) {
            return (long) asDouble;
        }
        return asDouble;
    }
}
