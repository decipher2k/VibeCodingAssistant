/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor;

import com.example.formeditor.canvas.FormEditorCanvas;
import com.example.formeditor.model.FormModel;
import com.example.formeditor.model.GuiElementModel;
import com.example.formeditor.palette.PaletteView;
import com.example.formeditor.properties.ui.PropertyInspector;
import com.example.formeditor.registry.BuiltInElementTypes;
import com.example.formeditor.serialization.FormPersistence;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Main entry point for hosting applications to embed the form editor.
 */
public class FormEditorView extends BorderPane {

    private static final double PASTE_OFFSET = 20;

    private final PaletteView paletteView = new PaletteView();
    private final FormEditorCanvas canvas = new FormEditorCanvas();
    private final PropertyInspector propertyInspector = new PropertyInspector();
    private final StackPane canvasWrapper = new StackPane();
    private final ScrollPane canvasScroller = new ScrollPane(canvasWrapper);
    private final MenuBar menuBar = new MenuBar();

    private final FormPersistence persistence = new FormPersistence();

    private final ObjectProperty<FormModel> model = new SimpleObjectProperty<>();
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    private GuiElementModel clipboardElement;
    private Path currentFile;

    private final Map<GuiElementModel, MapChangeListener<String, Object>> elementListeners = new HashMap<>();
    private ListChangeListener<GuiElementModel> elementsListener;

    private final ChangeListener<Number> formSizeListener = (obs, oldVal, newVal) -> {
        canvas.refreshAll();
        propertyInspector.refresh();
    };
    private final ChangeListener<String> formNameListener = (obs, oldVal, newVal) -> propertyInspector.refresh();
    private final ChangeListener<String> formBackgroundListener = (obs, oldVal, newVal) -> canvas.refreshAll();

    public FormEditorView() {
        BuiltInElementTypes.registerDefaults();
        initialiseLayout();
        bindInteractions();
        newForm(400, 400, "MainForm");
    }

    private void initialiseLayout() {
        // Setup canvas wrapper to allow resize handles to extend beyond canvas
        canvasWrapper.getChildren().add(canvas);
        canvasWrapper.setPadding(new Insets(7));
        canvasWrapper.setStyle("-fx-background-color: #E0E0E0;");
        canvas.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            canvasWrapper.setPrefSize(newBounds.getWidth() + 14, newBounds.getHeight() + 14);
        });
        StackPane.setAlignment(canvas, javafx.geometry.Pos.CENTER);
        
        canvasScroller.setFitToHeight(false);
        canvasScroller.setFitToWidth(false);
        canvasScroller.setPannable(true);
        canvasScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        canvasScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox leftPane = new VBox(paletteView);
        VBox.setVgrow(paletteView, Priority.ALWAYS);

        setLeft(leftPane);
        setCenter(canvasScroller);
        setRight(propertyInspector);
        setTop(menuBar);

        createMenus();
    }

    private void createMenus() {
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        newItem.setOnAction(e -> handleNewAction());
        MenuItem loadItem = new MenuItem("Load");
        loadItem.setOnAction(e -> handleLoadAction());
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> handleSaveAction());
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> handleExitAction());

        fileMenu.getItems().addAll(newItem, loadItem, saveItem, exitItem);

        Menu editMenu = new Menu("Edit");
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> copy());
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setOnAction(e -> cut());
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));
        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> paste());
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));

        editMenu.getItems().addAll(copyItem, cutItem, pasteItem);

        menuBar.getMenus().setAll(fileMenu, editMenu);
    }

    private void bindInteractions() {
        canvas.modelProperty().bind(model);
        canvas.selectedElementProperty().addListener((obs, oldSel, newSel) -> propertyInspector.setSelectedElement(newSel));
        propertyInspector.setOnPropertyCommitted(this::markDirty);

        model.addListener((obs, oldModel, newModel) -> {
            if (oldModel != null) {
                detachModelListeners(oldModel);
            }
            propertyInspector.setFormModel(newModel);
            if (newModel != null) {
                attachModelListeners(newModel);
                paletteView.refreshItems();
            }
            dirty.set(false);
        });

        canvas.addEventFilter(KeyEvent.KEY_PRESSED, this::handleCanvasKeyPressed);

        canvas.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(PaletteView.ELEMENT_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY);
                event.consume();
            }
        });

        canvas.setOnDragDropped(event -> {
            if (event.getDragboard().hasContent(PaletteView.ELEMENT_FORMAT)) {
                String type = (String) event.getDragboard().getContent(PaletteView.ELEMENT_FORMAT);
                Point2D dropPoint = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());
                canvas.addElement(type, dropPoint.getX(), dropPoint.getY());
                markDirty();
                event.setDropCompleted(true);
                event.consume();
            }
        });

        canvas.setOnMousePressed(event -> {
            if (event.getTarget() == canvas) {
                canvas.clearSelection();
            }
            canvas.requestFocus();
        });

        addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcutKeys);
    }

    public FormModel getModel() {
        return model.get();
    }

    public void setModel(FormModel formModel) {
        model.set(formModel);
    }

    public void newForm(int width, int height, String name) {
        FormModel newModel = new FormModel();
        newModel.setWidth(width);
        newModel.setHeight(height);
        newModel.setName(name);
        newModel.setBackground("#F0F0F0");
        setModel(newModel);
        currentFile = null;
        dirty.set(false);
    }

    public void newForm() {
        newForm(400, 400, "MainForm");
    }

    public void loadJson(InputStream inputStream) {
        FormModel loaded = persistence.fromJson(inputStream);
        setModel(loaded);
        dirty.set(false);
    }

    public String toJson() {
        return persistence.toJson(getModel());
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public FormEditorCanvas getCanvas() {
        return canvas;
    }

    public GuiElementModel getSelectedElement() {
        return canvas.getSelectedElement();
    }

    public void selectElement(GuiElementModel element) {
        canvas.selectElement(element);
    }

    public void copy() {
        GuiElementModel selected = canvas.getSelectedElement();
        if (selected == null) {
            return;
        }
        clipboardElement = deepCopy(selected);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(persistence.toJson(createTemporaryModel(selected)));
        clipboard.setContent(content);
    }

    public void cut() {
        GuiElementModel selected = canvas.getSelectedElement();
        if (selected == null) {
            return;
        }
        clipboardElement = deepCopy(selected);
        canvas.removeElement(selected);
        canvas.clearSelection();
        markDirty();
    }

    public void paste() {
        if (clipboardElement == null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                try (InputStream in = new java.io.ByteArrayInputStream(clipboard.getString().getBytes(StandardCharsets.UTF_8))) {
                    FormModel clipModel = persistence.fromJson(in);
                    if (!clipModel.getElements().isEmpty()) {
                        clipboardElement = deepCopy(clipModel.getElements().get(0));
                    }
                } catch (Exception ignored) {
                    // ignore invalid clipboard content
                }
            }
        }
        if (clipboardElement == null) {
            return;
        }
        GuiElementModel pasted = canvas.duplicateElement(clipboardElement, PASTE_OFFSET);
        attachElementListener(pasted);
        markDirty();
    }

    private void handleNewAction() {
        if (!promptDiscardChanges()) {
            return;
        }
        newForm();
    }

    private void handleLoadAction() {
        if (!promptDiscardChanges()) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Form JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        if (currentFile != null && currentFile.getParent() != null) {
            chooser.setInitialDirectory(currentFile.getParent().toFile());
        }
        Window window = getScene() != null ? getScene().getWindow() : null;
        java.io.File file = chooser.showOpenDialog(window);
        if (file != null) {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                loadJson(in);
                currentFile = file.toPath();
            } catch (IOException ex) {
                showError("Failed to load form", ex.getMessage());
            }
        }
    }

    private void handleSaveAction() {
        Path target;
        if (currentFile != null) {
            target = currentFile;
        } else {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Form JSON");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            chooser.setInitialFileName("form.json");
            Window window = getScene() != null ? getScene().getWindow() : null;
            java.io.File file = chooser.showSaveDialog(window);
            if (file == null) {
                return;
            }
            target = file.toPath();
        }

        try {
            Files.writeString(target, toJson(), StandardCharsets.UTF_8);
            currentFile = target;
            dirty.set(false);
        } catch (IOException ex) {
            showError("Failed to save form", ex.getMessage());
        }
    }

    private void handleExitAction() {
        if (!promptDiscardChanges()) {
            return;
        }
        Scene scene = getScene();
        if (scene != null && scene.getWindow() != null) {
            scene.getWindow().hide();
        }
    }

    private void handleShortcutKeys(KeyEvent event) {
        if (event.getTarget() instanceof javafx.scene.control.TextInputControl textInput && textInput.isEditable()) {
            return;
        }
        if (new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN).match(event)) {
            copy();
            event.consume();
        } else if (new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN).match(event)) {
            cut();
            event.consume();
        } else if (new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN).match(event)) {
            paste();
            event.consume();
        } else if (new KeyCodeCombination(KeyCode.DELETE).match(event)) {
            deleteSelection();
            event.consume();
        }
    }

    private void handleCanvasKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            deleteSelection();
            event.consume();
        }
    }

    private void deleteSelection() {
        GuiElementModel selected = canvas.getSelectedElement();
        if (selected != null) {
            canvas.removeElement(selected);
            canvas.clearSelection();
            markDirty();
        }
    }

    private boolean promptDiscardChanges() {
        if (!isDirty()) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("Discard unsaved changes?");
        alert.setContentText("Your changes will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void attachModelListeners(FormModel formModel) {
        propertyInspector.setFormModel(formModel);
        formModel.nameProperty().addListener(formNameListener);
        formModel.widthProperty().addListener(formSizeListener);
        formModel.heightProperty().addListener(formSizeListener);
        formModel.backgroundProperty().addListener(formBackgroundListener);

        ObservableList<GuiElementModel> elements = formModel.getElements();
        elementsListener = change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(this::attachElementListener);
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(this::detachElementListener);
                }
                markDirty();
            }
            propertyInspector.refresh();
        };
        elements.addListener(elementsListener);
        elements.forEach(this::attachElementListener);
        canvas.refreshAll();
    }

    private void detachModelListeners(FormModel formModel) {
        formModel.nameProperty().removeListener(formNameListener);
        formModel.widthProperty().removeListener(formSizeListener);
        formModel.heightProperty().removeListener(formSizeListener);
        formModel.backgroundProperty().removeListener(formBackgroundListener);
        if (elementsListener != null) {
            formModel.getElements().removeListener(elementsListener);
            elementsListener = null;
        }
        formModel.getElements().forEach(this::detachElementListener);
    }

    private void attachElementListener(GuiElementModel element) {
        MapChangeListener<String, Object> listener = change -> {
            markDirty();
            GuiElementModel selected = canvas.getSelectedElement();
            if (selected == element) {
                propertyInspector.refresh();
            }
        };
        element.propsProperty().addListener(listener);
        elementListeners.put(element, listener);
    }

    private void detachElementListener(GuiElementModel element) {
        MapChangeListener<String, Object> listener = elementListeners.remove(element);
        if (listener != null) {
            element.propsProperty().removeListener(listener);
        }
    }

    private void markDirty() {
        dirty.set(true);
    }

    private GuiElementModel deepCopy(GuiElementModel source) {
        GuiElementModel copy = new GuiElementModel(source.getType());
        copy.setProps(source.toPersistentProperties());
        return copy;
    }

    private FormModel createTemporaryModel(GuiElementModel element) {
        FormModel temp = new FormModel();
        temp.clearElements();
        temp.addElement(deepCopy(element));
        return temp;
    }
}
