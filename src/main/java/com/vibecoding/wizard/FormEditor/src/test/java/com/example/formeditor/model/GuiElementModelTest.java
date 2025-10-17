/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiElementModelTest {

    @Test
    void setLayoutBoundsUpdatesProperties() {
        GuiElementModel model = new GuiElementModel("Label");
        model.setProps(Map.of(
                "x", 10,
                "y", 15,
                "width", 100,
                "height", 30
        ));

        model.setLayoutBounds(25, 35, 200, 45);

        assertEquals(25, model.getLayoutX());
        assertEquals(35, model.getLayoutY());
        assertEquals(200, model.getWidth());
        assertEquals(45, model.getHeight());
    }
}
