/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.registry;

import com.example.formeditor.model.GuiElementModel;
import com.example.formeditor.util.FxTestSupport;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementRenderingTest extends FxTestSupport {

    @BeforeAll
    static void registerElements() {
        BuiltInElementTypes.registerDefaults();
    }

    @Test
    void labelPropertiesReflectOnNode() {
        GuiElementModel labelModel = new GuiElementModel("Label");
        labelModel.setProps(Map.of(
                "text", "Hello",
                "fontFamily", "System",
                "fontSize", 18,
                "foreground", "#336699",
                "background", "transparent",
                "x", 50,
                "y", 60,
                "width", 180,
                "height", 40
        ));

        Node node = ElementRegistry.createNode(labelModel);
        assertTrue(node instanceof Label);
        Label label = (Label) node;
        assertEquals("Hello", label.getText());
        assertEquals(18, label.getFont().getSize(), 0.001);
        assertEquals("0x336699ff", label.getTextFill().toString());
        assertEquals(180, label.getPrefWidth(), 0.001);
        assertEquals(40, label.getPrefHeight(), 0.001);
    }
}
