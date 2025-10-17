/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.canvas;

import com.example.formeditor.model.GuiElementModel;
import com.example.formeditor.registry.ElementRegistry;
import javafx.collections.MapChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.EnumMap;
import java.util.Map;

/**
 * Visual representation of a {@link GuiElementModel} with selection and resize affordances.
 */
class DesignerElementNode extends Pane {

    private static final double HANDLE_SIZE = 8;
    private static final double MIN_SIZE = 20;

    private final FormEditorCanvas canvas;
    private final GuiElementModel model;
    private final Node content;
    private final SelectionOverlay selectionOverlay;

    private Point2D dragAnchor;
    private double initialX;
    private double initialY;

    DesignerElementNode(FormEditorCanvas canvas, GuiElementModel model) {
        this.canvas = canvas;
        this.model = model;
        this.content = ElementRegistry.createNode(model);
    this.selectionOverlay = new SelectionOverlay();

    content.setMouseTransparent(true);
    getChildren().add(content);
        getChildren().add(selectionOverlay);
        selectionOverlay.setVisible(false);
        selectionOverlay.setManaged(false);
        setPickOnBounds(true);
        setCursor(Cursor.HAND);

        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseReleased(event -> dragAnchor = null);
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                canvas.selectElement(model);
                event.consume();
            }
        });

        model.propsProperty().addListener((MapChangeListener<String, Object>) change -> refreshFromModel());
        refreshFromModel();
    }

    GuiElementModel getModel() {
        return model;
    }

    void setSelected(boolean selected) {
        selectionOverlay.setVisible(selected);
        selectionOverlay.toFront();
        setCursor(selected ? Cursor.MOVE : Cursor.HAND);
    }

    void refreshFromModel() {
        ElementRegistry.applyProperties(model, content);
        double width = model.getWidth();
        double height = model.getHeight();
    setPrefSize(width, height);
    setMinSize(width, height);
    setMaxSize(width, height);
    resize(width, height);
        relocate(model.getLayoutX(), model.getLayoutY());
        if (content instanceof javafx.scene.Parent parent) {
            parent.requestLayout();
        }
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        content.resizeRelocate(0, 0, w, h);
        selectionOverlay.resizeRelocate(0, 0, w, h);
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        // Don't handle if a resize handle was clicked
        if (event.getTarget() instanceof ResizeHandle) {
            return;
        }
        canvas.selectElement(model);
        dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
        initialX = getLayoutX();
        initialY = getLayoutY();
        event.consume();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (dragAnchor == null) {
            return;
        }
        // Don't handle if we're resizing
        if (event.getTarget() instanceof ResizeHandle) {
            return;
        }
        double deltaX = event.getSceneX() - dragAnchor.getX();
        double deltaY = event.getSceneY() - dragAnchor.getY();
        double newX = initialX + deltaX;
        double newY = initialY + deltaY;
        double width = getWidth() > 0 ? getWidth() : model.getWidth();
        double height = getHeight() > 0 ? getHeight() : model.getHeight();
        canvas.updateElementBounds(model, newX, newY, width, height);
        event.consume();
    }

    private class SelectionOverlay extends Pane {

        private final Map<HandlePosition, ResizeHandle> handles = new EnumMap<>(HandlePosition.class);

        SelectionOverlay() {
            setPickOnBounds(false);

            Rectangle border = new Rectangle();
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.DODGERBLUE);
            border.getStrokeDashArray().setAll(6d, 4d);
            border.setStrokeWidth(1.0);
            border.setMouseTransparent(true);
            border.widthProperty().bind(widthProperty());
            border.heightProperty().bind(heightProperty());
            getChildren().add(border);

            for (HandlePosition position : HandlePosition.values()) {
                ResizeHandle handle = new ResizeHandle(position);
                handles.put(position, handle);
                getChildren().add(handle);
            }
        }

        @Override
        protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            
            // Relocate and resize each handle
            ResizeHandle topLeft = handles.get(HandlePosition.TOP_LEFT);
            topLeft.resize(HANDLE_SIZE, HANDLE_SIZE);
            topLeft.relocate(-HANDLE_SIZE / 2, -HANDLE_SIZE / 2);
            
            ResizeHandle topCenter = handles.get(HandlePosition.TOP_CENTER);
            topCenter.resize(HANDLE_SIZE, HANDLE_SIZE);
            topCenter.relocate(w / 2 - HANDLE_SIZE / 2, -HANDLE_SIZE / 2);
            
            ResizeHandle topRight = handles.get(HandlePosition.TOP_RIGHT);
            topRight.resize(HANDLE_SIZE, HANDLE_SIZE);
            topRight.relocate(w - HANDLE_SIZE / 2, -HANDLE_SIZE / 2);
            
            ResizeHandle centerLeft = handles.get(HandlePosition.CENTER_LEFT);
            centerLeft.resize(HANDLE_SIZE, HANDLE_SIZE);
            centerLeft.relocate(-HANDLE_SIZE / 2, h / 2 - HANDLE_SIZE / 2);
            
            ResizeHandle centerRight = handles.get(HandlePosition.CENTER_RIGHT);
            centerRight.resize(HANDLE_SIZE, HANDLE_SIZE);
            centerRight.relocate(w - HANDLE_SIZE / 2, h / 2 - HANDLE_SIZE / 2);
            
            ResizeHandle bottomLeft = handles.get(HandlePosition.BOTTOM_LEFT);
            bottomLeft.resize(HANDLE_SIZE, HANDLE_SIZE);
            bottomLeft.relocate(-HANDLE_SIZE / 2, h - HANDLE_SIZE / 2);
            
            ResizeHandle bottomCenter = handles.get(HandlePosition.BOTTOM_CENTER);
            bottomCenter.resize(HANDLE_SIZE, HANDLE_SIZE);
            bottomCenter.relocate(w / 2 - HANDLE_SIZE / 2, h - HANDLE_SIZE / 2);
            
            ResizeHandle bottomRight = handles.get(HandlePosition.BOTTOM_RIGHT);
            bottomRight.resize(HANDLE_SIZE, HANDLE_SIZE);
            bottomRight.relocate(w - HANDLE_SIZE / 2, h - HANDLE_SIZE / 2);
        }
    }

    private class ResizeHandle extends Region {

        private final HandlePosition position;
        private Point2D anchor;
        private double startX;
        private double startY;
        private double startWidth;
        private double startHeight;

        ResizeHandle(HandlePosition position) {
            this.position = position;
            setPrefSize(HANDLE_SIZE, HANDLE_SIZE);
            setMinSize(HANDLE_SIZE, HANDLE_SIZE);
            setMaxSize(HANDLE_SIZE, HANDLE_SIZE);
            setStyle("-fx-background-color: white; -fx-border-color: dodgerblue; -fx-border-width: 2; -fx-border-radius: 1;");
            setCursor(position.cursor);
            setOnMousePressed(this::onMousePressed);
            setOnMouseDragged(this::onMouseDragged);
            setOnMouseReleased(e -> anchor = null);
            setPickOnBounds(true);
            setMouseTransparent(false);
            setManaged(false);  // We manually position handles in layoutChildren
        }

        private void onMousePressed(MouseEvent event) {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            anchor = new Point2D(event.getSceneX(), event.getSceneY());
            startX = DesignerElementNode.this.getLayoutX();
            startY = DesignerElementNode.this.getLayoutY();
            startWidth = DesignerElementNode.this.getWidth() > 0 ? DesignerElementNode.this.getWidth() : model.getWidth();
            startHeight = DesignerElementNode.this.getHeight() > 0 ? DesignerElementNode.this.getHeight() : model.getHeight();
            event.consume();
        }

        private void onMouseDragged(MouseEvent event) {
            if (anchor == null) {
                return;
            }
            double deltaX = event.getSceneX() - anchor.getX();
            double deltaY = event.getSceneY() - anchor.getY();

            double newX = DesignerElementNode.this.getLayoutX();
            double newY = DesignerElementNode.this.getLayoutY();
            double newWidth = startWidth;
            double newHeight = startHeight;

            switch (position) {
                case TOP_LEFT -> {
                    newX = startX + deltaX;
                    newY = startY + deltaY;
                    newWidth = startWidth - deltaX;
                    newHeight = startHeight - deltaY;
                }
                case TOP_CENTER -> {
                    newY = startY + deltaY;
                    newHeight = startHeight - deltaY;
                }
                case TOP_RIGHT -> {
                    newY = startY + deltaY;
                    newWidth = startWidth + deltaX;
                    newHeight = startHeight - deltaY;
                }
                case CENTER_LEFT -> {
                    newX = startX + deltaX;
                    newWidth = startWidth - deltaX;
                }
                case CENTER_RIGHT -> newWidth = startWidth + deltaX;
                case BOTTOM_LEFT -> {
                    newX = startX + deltaX;
                    newWidth = startWidth - deltaX;
                    newHeight = startHeight + deltaY;
                }
                case BOTTOM_CENTER -> newHeight = startHeight + deltaY;
                case BOTTOM_RIGHT -> {
                    newWidth = startWidth + deltaX;
                    newHeight = startHeight + deltaY;
                }
            }

            if (newWidth < MIN_SIZE) {
                newWidth = MIN_SIZE;
                if (position == HandlePosition.TOP_LEFT || position == HandlePosition.BOTTOM_LEFT) {
                    newX = DesignerElementNode.this.getLayoutX() + DesignerElementNode.this.getWidth() - MIN_SIZE;
                }
            }
            if (newHeight < MIN_SIZE) {
                newHeight = MIN_SIZE;
                if (position == HandlePosition.TOP_LEFT || position == HandlePosition.TOP_RIGHT || position == HandlePosition.TOP_CENTER) {
                    newY = DesignerElementNode.this.getLayoutY() + DesignerElementNode.this.getHeight() - MIN_SIZE;
                }
            }

            canvas.updateElementBounds(model, newX, newY, newWidth, newHeight);
            event.consume();
        }
    }

    private enum HandlePosition {
        TOP_LEFT(Cursor.NW_RESIZE),
        TOP_CENTER(Cursor.N_RESIZE),
        TOP_RIGHT(Cursor.NE_RESIZE),
        CENTER_LEFT(Cursor.W_RESIZE),
        CENTER_RIGHT(Cursor.E_RESIZE),
        BOTTOM_LEFT(Cursor.SW_RESIZE),
        BOTTOM_CENTER(Cursor.S_RESIZE),
        BOTTOM_RIGHT(Cursor.SE_RESIZE);

        private final Cursor cursor;

        HandlePosition(Cursor cursor) {
            this.cursor = cursor;
        }
    }
}
