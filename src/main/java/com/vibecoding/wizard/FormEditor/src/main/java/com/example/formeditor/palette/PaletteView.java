/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.palette;

import com.example.formeditor.registry.ElementRegistry;
import com.example.formeditor.registry.ElementType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Displays available components that can be dragged onto the canvas.
 */
public class PaletteView extends VBox {

    public static final DataFormat ELEMENT_FORMAT = new DataFormat("application/x-formeditor-element");

    public PaletteView() {
        getStyleClass().add("palette-view");
        setSpacing(6);
        setPadding(new Insets(10));
        refreshItems();
    }

    public void refreshItems() {
        getChildren().clear();
        ElementRegistry.all().stream()
                .sorted((a, b) -> a.type().compareToIgnoreCase(b.type()))
                .forEach(elementType -> getChildren().add(createItem(elementType)));
    }

    private HBox createItem(ElementType elementType) {
        Rectangle icon = new Rectangle(16, 16, Color.DODGERBLUE);
        icon.setArcWidth(4);
        icon.setArcHeight(4);

        Label label = new Label(elementType.type());
        label.getStyleClass().add("palette-item-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox container = new HBox(8, icon, label, spacer);
        container.getStyleClass().add("palette-item");
        container.setPadding(new Insets(6));

        container.setOnDragDetected(event -> {
            Dragboard dragboard = container.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(elementType.type());
            content.put(ELEMENT_FORMAT, elementType.type());
            dragboard.setContent(content);
            dragboard.setDragView(container.snapshot(null, null));
            event.consume();
        });

        return container;
    }
}
