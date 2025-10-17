/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.serialization;

import com.example.formeditor.model.FormModel;
import com.example.formeditor.model.GuiElementModel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormPersistenceTest {

    @Test
    void roundTripPreservesModel() {
        FormModel original = new FormModel();
        original.setId("form-uuid");
        original.setName("MainForm");
        original.setWidth(800);
        original.setHeight(600);
        original.setBackground("#F0F0F0");

        GuiElementModel label = new GuiElementModel("elem-1", "Label", Map.of(
                "text", "Hello",
                "fontFamily", "System",
                "fontSize", 14,
                "foreground", "#000000",
                "background", "transparent",
                "x", 40,
                "y", 30,
                "width", 100,
                "height", 24
        ));

        GuiElementModel textField = new GuiElementModel("elem-2", "TextField", Map.of(
                "text", "Type here",
                "foreground", "#111111",
                "background", "#FFFFFF",
                "x", 40,
                "y", 70,
                "width", 240,
                "height", 28
        ));

        original.addElement(label);
        original.addElement(textField);

        FormPersistence persistence = new FormPersistence();
        String json = persistence.toJson(original);
        FormModel restored = persistence.fromJson(json);

        assertEquals(original, restored);
    }
}
