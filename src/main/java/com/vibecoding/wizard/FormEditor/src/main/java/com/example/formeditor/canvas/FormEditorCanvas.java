/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.canvas;

import com.example.formeditor.model.FormModel;
import com.example.formeditor.model.GuiElementModel;
import com.example.formeditor.registry.ElementRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Canvas displaying and manipulating GUI elements in a WYSIWYG fashion.
 */
public class FormEditorCanvas extends Pane {

    private static final double MIN_ELEMENT_SIZE = 20;
    private static final double MIN_CANVAS_SIZE = 200;
    private static final double HANDLE_SIZE = 10;
    private static final double HANDLE_OFFSET = 2;

    private final ObjectProperty<FormModel> model = new SimpleObjectProperty<>();
    private final ObjectProperty<GuiElementModel> selectedElement = new SimpleObjectProperty<>();

    private final Map<GuiElementModel, DesignerElementNode> nodeMap = new HashMap<>();
    private final Map<ResizePosition, CanvasResizeHandle> canvasResizeHandles = new EnumMap<>(ResizePosition.class);

    private final ListChangeListener<GuiElementModel> elementListListener = change -> {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.getRemoved().forEach(this::removeElementNode);
            }
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::createElementNode);
            }
        }
    };

    public FormEditorCanvas() {
        setFocusTraversable(true);
        setPadding(new Insets(0));
        setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        getStyleClass().add("form-editor-canvas");

        // Initialize canvas resize handles
        initializeCanvasResizeHandles();

        model.addListener((obs, oldModel, newModel) -> {
            if (oldModel != null) {
                oldModel.getElements().removeListener(elementListListener);
            }
            // Remove only element nodes, not resize handles
            nodeMap.values().forEach(node -> getChildren().remove(node));
            nodeMap.clear();
            if (newModel != null) {
                newModel.getElements().addListener(elementListListener);
                newModel.getElements().forEach(this::createElementNode);
                updateFormAppearance(newModel);
                updateCanvasResizeHandles();
                // Ensure resize handles are always on top
                canvasResizeHandles.values().forEach(handle -> handle.toFront());
            }
        });

        selectedElement.addListener((obs, oldValue, newValue) -> nodeMap.values()
                .forEach(node -> node.setSelected(node.getModel() == newValue)));

        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvents);
    }

    public ObjectProperty<FormModel> modelProperty() {
        return model;
    }

    public FormModel getModel() {
        return model.get();
    }

    public void setModel(FormModel model) {
        this.model.set(model);
    }

    public ObjectProperty<GuiElementModel> selectedElementProperty() {
        return selectedElement;
    }

    public GuiElementModel getSelectedElement() {
        return selectedElement.get();
    }

    public void selectElement(GuiElementModel element) {
        selectedElement.set(element);
    }

    public void clearSelection() {
        selectedElement.set(null);
    }

    public GuiElementModel addElement(String type, double x, double y) {
        FormModel formModel = requireModel();
        GuiElementModel element = ElementRegistry.createDefault(type, x, y);
        clampToCanvas(element);
        formModel.addElement(element);
        selectElement(element);
        return element;
    }

    public GuiElementModel duplicateElement(GuiElementModel source, double offset) {
        Map<String, Object> props = new HashMap<>(source.toPersistentProperties());
        props.put("x", source.getLayoutX() + offset);
        props.put("y", source.getLayoutY() + offset);
        GuiElementModel copy = new GuiElementModel(source.getType());
        copy.setProps(props);
        requireModel().addElement(copy);
        selectElement(copy);
        return copy;
    }

    public void updateElementBounds(GuiElementModel element, double x, double y, double width, double height) {
        width = Math.max(width, MIN_ELEMENT_SIZE);
        height = Math.max(height, MIN_ELEMENT_SIZE);
        FormModel form = requireModel();
        x = Math.max(0, Math.min(form.getWidth() - width, x));
        y = Math.max(0, Math.min(form.getHeight() - height, y));
        element.setLayoutBounds(x, y, width, height);
        DesignerElementNode node = nodeMap.get(element);
        if (node != null) {
            node.refreshFromModel();
        }
    }

    public void refreshAll() {
        nodeMap.values().forEach(DesignerElementNode::refreshFromModel);
        if (model.get() != null) {
            updateFormAppearance(model.get());
        }
    }

    public void removeElement(GuiElementModel element) {
        requireModel().removeElement(element);
    }

    private void createElementNode(GuiElementModel element) {
        DesignerElementNode node = new DesignerElementNode(this, element);
        nodeMap.put(element, node);
        getChildren().add(node);
        node.toFront();
        // Keep resize handles on top
        canvasResizeHandles.values().forEach(handle -> handle.toFront());
    }

    private void removeElementNode(GuiElementModel element) {
        DesignerElementNode node = nodeMap.remove(element);
        if (node != null) {
            getChildren().remove(node);
        }
        if (element.equals(selectedElement.get())) {
            clearSelection();
        }
    }

    private void updateFormAppearance(FormModel model) {
        setPrefSize(model.getWidth(), model.getHeight());
        setMinSize(model.getWidth(), model.getHeight());
        setMaxSize(model.getWidth(), model.getHeight());
        String background = model.getBackground();
        if (background == null || background.isBlank()) {
            setBackground(Background.EMPTY);
        } else {
            if ("transparent".equalsIgnoreCase(background)) {
                setBackground(Background.EMPTY);
                return;
            }
            try {
                Color color = Color.web(background);
                setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            } catch (IllegalArgumentException ex) {
                setBackground(new Background(new BackgroundFill(Color.web("#FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    private void clampToCanvas(GuiElementModel element) {
        FormModel formModel = requireModel();
        double width = element.getWidth();
        double height = element.getHeight();
        double x = Math.max(0, Math.min(formModel.getWidth() - width, element.getLayoutX()));
        double y = Math.max(0, Math.min(formModel.getHeight() - height, element.getLayoutY()));
        element.setLayoutBounds(x, y, width, height);
    }

    private FormModel requireModel() {
        return Objects.requireNonNull(model.get(), "No form model set");
    }

    private void handleKeyEvents(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            clearSelection();
            event.consume();
        }
    }

    private void initializeCanvasResizeHandles() {
        for (ResizePosition position : ResizePosition.values()) {
            CanvasResizeHandle handle = new CanvasResizeHandle(position);
            canvasResizeHandles.put(position, handle);
            getChildren().add(handle);
            handle.toFront();
        }
    }

    private void updateCanvasResizeHandles() {
        if (model.get() == null) {
            canvasResizeHandles.values().forEach(handle -> handle.setVisible(false));
            return;
        }

        double width = getWidth();
        double height = getHeight();

        canvasResizeHandles.values().forEach(handle -> {
            handle.setVisible(true);
            handle.toFront();
        });

        // Position the handles at the canvas edges
        CanvasResizeHandle topLeft = canvasResizeHandles.get(ResizePosition.TOP_LEFT);
        topLeft.relocate(-HANDLE_SIZE - HANDLE_OFFSET, -HANDLE_SIZE - HANDLE_OFFSET);

        CanvasResizeHandle topCenter = canvasResizeHandles.get(ResizePosition.TOP_CENTER);
        topCenter.relocate(width / 2 - HANDLE_SIZE / 2, -HANDLE_SIZE - HANDLE_OFFSET);

        CanvasResizeHandle topRight = canvasResizeHandles.get(ResizePosition.TOP_RIGHT);
        topRight.relocate(width + HANDLE_OFFSET, -HANDLE_SIZE - HANDLE_OFFSET);

        CanvasResizeHandle centerLeft = canvasResizeHandles.get(ResizePosition.CENTER_LEFT);
        centerLeft.relocate(-HANDLE_SIZE - HANDLE_OFFSET, height / 2 - HANDLE_SIZE / 2);

        CanvasResizeHandle centerRight = canvasResizeHandles.get(ResizePosition.CENTER_RIGHT);
        centerRight.relocate(width + HANDLE_OFFSET, height / 2 - HANDLE_SIZE / 2);

        CanvasResizeHandle bottomLeft = canvasResizeHandles.get(ResizePosition.BOTTOM_LEFT);
        bottomLeft.relocate(-HANDLE_SIZE - HANDLE_OFFSET, height + HANDLE_OFFSET);

        CanvasResizeHandle bottomCenter = canvasResizeHandles.get(ResizePosition.BOTTOM_CENTER);
        bottomCenter.relocate(width / 2 - HANDLE_SIZE / 2, height + HANDLE_OFFSET);

        CanvasResizeHandle bottomRight = canvasResizeHandles.get(ResizePosition.BOTTOM_RIGHT);
        bottomRight.relocate(width + HANDLE_OFFSET, height + HANDLE_OFFSET);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        updateCanvasResizeHandles();
    }

    /**
     * Inner class for canvas resize handles
     */
    private class CanvasResizeHandle extends Region {
        private final ResizePosition position;
        private Point2D anchor;
        private double startWidth;
        private double startHeight;

        CanvasResizeHandle(ResizePosition position) {
            this.position = position;
            setPrefSize(HANDLE_SIZE, HANDLE_SIZE);
            setMinSize(HANDLE_SIZE, HANDLE_SIZE);
            setMaxSize(HANDLE_SIZE, HANDLE_SIZE);
            // Make handles visible with red color and shadow for better visibility
            setStyle("-fx-background-color: #FF6B6B; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 3; -fx-background-radius: 3; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 1);");
            setCursor(position.cursor);
            setOnMousePressed(this::onMousePressed);
            setOnMouseDragged(this::onMouseDragged);
            setOnMouseReleased(e -> anchor = null);
            setPickOnBounds(true);
            setMouseTransparent(false);
            setManaged(false);
            setVisible(true);
        }

        private void onMousePressed(MouseEvent event) {
            if (event.getButton() != MouseButton.PRIMARY || model.get() == null) {
                return;
            }
            anchor = new Point2D(event.getSceneX(), event.getSceneY());
            FormModel formModel = model.get();
            startWidth = formModel.getWidth();
            startHeight = formModel.getHeight();
            event.consume();
        }

        private void onMouseDragged(MouseEvent event) {
            if (anchor == null || model.get() == null) {
                return;
            }

            double deltaX = event.getSceneX() - anchor.getX();
            double deltaY = event.getSceneY() - anchor.getY();

            FormModel formModel = model.get();
            double newWidth = startWidth;
            double newHeight = startHeight;

            switch (position) {
                case TOP_LEFT -> {
                    // For top-left, we don't resize; it would require moving all elements
                    // Just resize from opposite corner
                    newWidth = startWidth - deltaX;
                    newHeight = startHeight - deltaY;
                }
                case TOP_CENTER -> {
                    newHeight = startHeight - deltaY;
                }
                case TOP_RIGHT -> {
                    newWidth = startWidth + deltaX;
                    newHeight = startHeight - deltaY;
                }
                case CENTER_LEFT -> {
                    newWidth = startWidth - deltaX;
                }
                case CENTER_RIGHT -> {
                    newWidth = startWidth + deltaX;
                }
                case BOTTOM_LEFT -> {
                    newWidth = startWidth - deltaX;
                    newHeight = startHeight + deltaY;
                }
                case BOTTOM_CENTER -> {
                    newHeight = startHeight + deltaY;
                }
                case BOTTOM_RIGHT -> {
                    newWidth = startWidth + deltaX;
                    newHeight = startHeight + deltaY;
                }
            }

            // Apply minimum size constraint
            newWidth = Math.max(MIN_CANVAS_SIZE, newWidth);
            newHeight = Math.max(MIN_CANVAS_SIZE, newHeight);

            // Update the form model dimensions
            formModel.setWidth(newWidth);
            formModel.setHeight(newHeight);
            updateFormAppearance(formModel);

            event.consume();
        }
    }

    /**
     * Enum for resize handle positions on the canvas border
     */
    private enum ResizePosition {
        TOP_LEFT(Cursor.NW_RESIZE),
        TOP_CENTER(Cursor.N_RESIZE),
        TOP_RIGHT(Cursor.NE_RESIZE),
        CENTER_LEFT(Cursor.W_RESIZE),
        CENTER_RIGHT(Cursor.E_RESIZE),
        BOTTOM_LEFT(Cursor.SW_RESIZE),
        BOTTOM_CENTER(Cursor.S_RESIZE),
        BOTTOM_RIGHT(Cursor.SE_RESIZE);

        private final Cursor cursor;

        ResizePosition(Cursor cursor) {
            this.cursor = cursor;
        }
    }
}
