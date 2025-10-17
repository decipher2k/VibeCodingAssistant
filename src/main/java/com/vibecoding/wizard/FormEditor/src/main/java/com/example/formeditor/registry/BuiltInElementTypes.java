/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.registry;

import com.example.formeditor.model.GuiElementModel;
import com.example.formeditor.properties.PropertyDescriptor;
import com.example.formeditor.properties.PropertyKind;
import com.example.formeditor.properties.StandardProperties;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Registers and configures all built-in element types supported by the editor.
 */
public final class BuiltInElementTypes {

    private BuiltInElementTypes() {
    }

    public static void registerDefaults() {
        ElementRegistry.clear();
        registerLabel();
        registerTextField();
        registerTextArea();
        registerButton();
        registerCheckBox();
        registerRadioButton();
        registerComboBox();
        registerListView();
        registerProgressBar();
        registerSlider();
        registerDatePicker();
        registerSeparator();
        registerImageView();
        registerWindowHost();
    }

    private static void registerLabel() {
        ElementRegistry.register(new SimpleElementType(
                "Label",
                Label::new,
                (model, node) -> applyLabeled((Label) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(
                        StandardProperties.TEXT,
                        StandardProperties.FONT_FAMILY,
                        StandardProperties.FONT_SIZE,
                        StandardProperties.FOREGROUND,
                        StandardProperties.BACKGROUND
                )),
                defaults(Map.of(
                        "text", "Label",
                        "width", 120,
                        "height", 24
                ))
        ));
    }

    private static void registerTextField() {
        ElementRegistry.register(new SimpleElementType(
                "TextField",
                TextField::new,
                (model, node) -> applyTextInput((TextField) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(
                        StandardProperties.TEXT,
                        StandardProperties.FOREGROUND,
                        StandardProperties.BACKGROUND
                )),
                defaults(Map.of(
                        "text", "Text",
                        "width", 160,
                        "height", 28
                ))
        ));
    }

    private static void registerTextArea() {
        ElementRegistry.register(new SimpleElementType(
                "TextArea",
                TextArea::new,
                (model, node) -> applyTextArea((TextArea) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(
                        StandardProperties.TEXT,
                        StandardProperties.FOREGROUND,
                        StandardProperties.BACKGROUND
                )),
                defaults(Map.of(
                        "text", "Long text",
                        "width", 200,
                        "height", 120
                ))
        ));
    }

    private static void registerButton() {
        ElementRegistry.register(new SimpleElementType(
                "Button",
                Button::new,
                (model, node) -> applyButton((Button) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(
                        StandardProperties.TEXT,
                        StandardProperties.FONT_FAMILY,
                        StandardProperties.FONT_SIZE,
                        StandardProperties.FOREGROUND,
                        StandardProperties.BACKGROUND
                )),
                defaults(Map.of(
                        "text", "Button",
                        "width", 100,
                        "height", 32
                ))
        ));
    }

    private static void registerCheckBox() {
        PropertyDescriptor selected = PropertyDescriptor.builder("selected", PropertyKind.BOOLEAN)
                .defaultValue(false)
                .build();

        ElementRegistry.register(new SimpleElementType(
                "CheckBox",
                CheckBox::new,
                (model, node) -> applyToggle((CheckBox) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(
                        StandardProperties.TEXT,
                        selected
                )),
                defaults(Map.of(
                        "text", "CheckBox",
                        "width", 140,
                        "height", 24
                ))
        ));
    }

    private static void registerRadioButton() {
        PropertyDescriptor selected = PropertyDescriptor.builder("selected", PropertyKind.BOOLEAN)
                .defaultValue(false)
                .build();

        ElementRegistry.register(new SimpleElementType(
                "RadioButton",
                RadioButton::new,
                (model, node) -> applyToggle((RadioButton) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(
                        StandardProperties.TEXT,
                        selected
                )),
                defaults(Map.of(
                        "text", "RadioButton",
                        "width", 160,
                        "height", 24
                ))
        ));
    }

    private static void registerComboBox() {
        PropertyDescriptor items = PropertyDescriptor.builder("items", PropertyKind.STRING)
                .defaultValue("Option 1,Option 2,Option 3")
                .build();
        PropertyDescriptor selectedItem = PropertyDescriptor.builder("selectedItem", PropertyKind.STRING)
                .defaultValue("Option 1")
                .build();

        ElementRegistry.register(new SimpleElementType(
                "ComboBox",
                ComboBox::new,
                (model, node) -> applyComboBox((ComboBox<String>) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(
                        items,
                        selectedItem
                )),
                defaults(Map.of(
                        "items", "Option 1,Option 2,Option 3",
                        "selectedItem", "Option 1",
                        "width", 160,
                        "height", 28
                ))
        ));
    }

    private static void registerListView() {
        PropertyDescriptor items = PropertyDescriptor.builder("items", PropertyKind.STRING)
                .defaultValue("Item 1,Item 2,Item 3")
                .build();

        ElementRegistry.register(new SimpleElementType(
                "ListView",
                () -> {
                    ListView<String> listView = new ListView<>();
                    listView.setPrefSize(160, 120);
                    return listView;
                },
                (model, node) -> applyListView((ListView<String>) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(items)),
                defaults(Map.of(
                        "items", "Item 1,Item 2,Item 3",
                        "width", 160,
                        "height", 140
                ))
        ));
    }

    private static void registerProgressBar() {
        PropertyDescriptor progress = PropertyDescriptor.builder("progress", PropertyKind.NUMBER)
                .defaultValue(0.5)
                .build();

        ElementRegistry.register(new SimpleElementType(
                "ProgressBar",
                ProgressBar::new,
                (model, node) -> applyProgressBar((ProgressBar) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(progress)),
                defaults(Map.of(
                        "progress", 0.5,
                        "width", 160,
                        "height", 20
                ))
        ));
    }

    private static void registerSlider() {
        PropertyDescriptor min = PropertyDescriptor.builder("min", PropertyKind.NUMBER)
                .defaultValue(0)
                .build();
        PropertyDescriptor max = PropertyDescriptor.builder("max", PropertyKind.NUMBER)
                .defaultValue(100)
                .build();
        PropertyDescriptor value = PropertyDescriptor.builder("value", PropertyKind.NUMBER)
                .defaultValue(50)
                .build();

        ElementRegistry.register(new SimpleElementType(
                "Slider",
                Slider::new,
                (model, node) -> applySlider((Slider) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(min, max, value)),
                defaults(Map.of(
                        "min", 0,
                        "max", 100,
                        "value", 50,
                        "width", 200,
                        "height", 40
                ))
        ));
    }

    private static void registerDatePicker() {
        PropertyDescriptor value = PropertyDescriptor.builder("value", PropertyKind.STRING)
                .defaultValue("")
                .build();

        ElementRegistry.register(new SimpleElementType(
                "DatePicker",
                DatePicker::new,
                (model, node) -> applyDatePicker((DatePicker) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(value)),
                defaults(Map.of(
                        "width", 160,
                        "height", 28
                ))
        ));
    }

    private static void registerSeparator() {
        PropertyDescriptor orientation = PropertyDescriptor.builder("orientation", PropertyKind.ENUM)
                .defaultValue("HORIZONTAL")
                .build();

        ElementRegistry.register(new SimpleElementType(
                "Separator",
                Separator::new,
                (model, node) -> applySeparator((Separator) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(orientation)),
                defaults(Map.of(
                        "orientation", "HORIZONTAL",
                        "width", 200,
                        "height", 12
                ))
        ));
    }

    private static void registerImageView() {
        PropertyDescriptor imageUrl = PropertyDescriptor.builder("imageUrl", PropertyKind.STRING)
                .defaultValue("")
                .build();

        ElementRegistry.register(new SimpleElementType(
                "ImageView",
                ImageView::new,
                (model, node) -> applyImageView((ImageView) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(imageUrl)),
                defaults(Map.of(
                        "imageUrl", "",
                        "width", 64,
                        "height", 64
                ))
        ));
    }

    private static void registerWindowHost() {
        PropertyDescriptor initialWindow = PropertyDescriptor.builder("initialWindow", PropertyKind.STRING)
                .defaultValue("")
                .build();

        ElementRegistry.register(new SimpleElementType(
                "WindowHost",
                () -> {
                    StackPane pane = new StackPane();
                    Label placeholder = new Label("Window Host");
                    placeholder.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
                    pane.getChildren().add(placeholder);
                    pane.setAlignment(Pos.CENTER);
                    pane.setBorder(new Border(new BorderStroke(
                            Color.GRAY,
                            BorderStrokeStyle.DASHED,
                            new CornerRadii(4),
                            new BorderWidths(2)
                    )));
                    return pane;
                },
                (model, node) -> applyWindowHost((StackPane) node, model),
                concat(StandardProperties.layoutDescriptors(), List.of(
                        initialWindow,
                        StandardProperties.BACKGROUND
                )),
                defaults(Map.of(
                        "initialWindow", "",
                        "width", 400,
                        "height", 300
                ))
        ));
    }

    private static List<PropertyDescriptor> concat(List<PropertyDescriptor> first, List<PropertyDescriptor> second) {
        return Stream.concat(first.stream(), second.stream()).toList();
    }

    private static Map<String, Object> defaults(Map<String, Object> map) {
        return new LinkedHashMap<>(map);
    }

    private static void applyLabeled(Label label, GuiElementModel model) {
        label.setText(getString(model, "text", "Label"));
        applyFont(label, model);
        applyTextFill(label, model);
        applyBackground(label, model);
        label.setPrefWidth(model.getWidth());
        label.setPrefHeight(model.getHeight());
    }

    private static void applyButton(Button button, GuiElementModel model) {
        button.setText(getString(model, "text", "Button"));
        applyFont(button, model);
        applyTextFill(button, model);
        applyBackground(button, model);
        button.setPrefWidth(model.getWidth());
        button.setPrefHeight(model.getHeight());
    }

    private static void applyTextInput(TextField field, GuiElementModel model) {
        field.setText(getString(model, "text", ""));
        field.setPrefWidth(model.getWidth());
        field.setPrefHeight(model.getHeight());
        field.setStyle(colorStyle(model));
    }

    private static void applyTextArea(TextArea area, GuiElementModel model) {
        area.setText(getString(model, "text", ""));
        area.setPrefWidth(model.getWidth());
        area.setPrefHeight(model.getHeight());
        area.setStyle(colorStyle(model));
    }

    private static void applyToggle(CheckBox checkBox, GuiElementModel model) {
        checkBox.setText(getString(model, "text", "CheckBox"));
        checkBox.setSelected(getBoolean(model, "selected", false));
        checkBox.setPrefWidth(model.getWidth());
        checkBox.setPrefHeight(model.getHeight());
    }

    private static void applyToggle(RadioButton radioButton, GuiElementModel model) {
        radioButton.setText(getString(model, "text", "RadioButton"));
        radioButton.setSelected(getBoolean(model, "selected", false));
        radioButton.setPrefWidth(model.getWidth());
        radioButton.setPrefHeight(model.getHeight());
    }

    private static void applyComboBox(ComboBox<String> comboBox, GuiElementModel model) {
        comboBox.setItems(FXCollections.observableArrayList(parseItems(getString(model, "items", ""))));
        comboBox.setValue(getString(model, "selectedItem", null));
        comboBox.setPrefWidth(model.getWidth());
        comboBox.setPrefHeight(model.getHeight());
    }

    private static void applyListView(ListView<String> listView, GuiElementModel model) {
        listView.setItems(FXCollections.observableArrayList(parseItems(getString(model, "items", ""))));
        listView.setPrefWidth(model.getWidth());
        listView.setPrefHeight(model.getHeight());
    }

    private static void applyProgressBar(ProgressBar progressBar, GuiElementModel model) {
        progressBar.setProgress(clamp(getDouble(model, "progress", 0.5), 0, 1));
        progressBar.setPrefWidth(model.getWidth());
        progressBar.setPrefHeight(model.getHeight());
    }

    private static void applySlider(Slider slider, GuiElementModel model) {
        double min = getDouble(model, "min", 0);
        double max = getDouble(model, "max", 100);
        slider.setMin(min);
        slider.setMax(max);
        slider.setValue(clamp(getDouble(model, "value", min), min, max));
        slider.setPrefWidth(model.getWidth());
        slider.setPrefHeight(model.getHeight());
    }

    private static void applyDatePicker(DatePicker datePicker, GuiElementModel model) {
        String value = getString(model, "value", null);
        if (value == null || value.isBlank()) {
            datePicker.setValue(null);
        } else {
            try {
                datePicker.setValue(java.time.LocalDate.parse(value));
            } catch (Exception ignored) {
                datePicker.setValue(null);
            }
        }
        datePicker.setPrefWidth(model.getWidth());
        datePicker.setPrefHeight(model.getHeight());
    }

    private static void applySeparator(Separator separator, GuiElementModel model) {
        String orientation = getString(model, "orientation", "HORIZONTAL");
        separator.setOrientation("VERTICAL".equalsIgnoreCase(orientation) ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        separator.setPrefWidth(model.getWidth());
        separator.setPrefHeight(model.getHeight());
    }

    private static void applyImageView(ImageView imageView, GuiElementModel model) {
        String imageUrl = getString(model, "imageUrl", "");
        if (imageUrl != null && !imageUrl.isBlank()) {
            imageView.setImage(loadImage(imageUrl));
        } else {
            imageView.setImage(null);
        }
        imageView.setFitWidth(model.getWidth());
        imageView.setFitHeight(model.getHeight());
        imageView.setPreserveRatio(false);
    }

    private static void applyWindowHost(StackPane pane, GuiElementModel model) {
        String initialWindow = getString(model, "initialWindow", "");
        
        // Clear existing children
        pane.getChildren().clear();
        
        // Add label showing the initial window name
        Label label = new Label(initialWindow.isBlank() ? "Window Host" : "Host: " + initialWindow);
        label.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        pane.getChildren().add(label);
        pane.setAlignment(Pos.CENTER);
        
        // Apply border
        pane.setBorder(new Border(new BorderStroke(
                Color.GRAY,
                BorderStrokeStyle.DASHED,
                new CornerRadii(4),
                new BorderWidths(2)
        )));
        
        // Apply background if set
        String background = getString(model, "background", "transparent");
        if (background == null || background.isBlank() || "transparent".equalsIgnoreCase(background)) {
            pane.setStyle("");
        } else {
            pane.setStyle("-fx-background-color: " + background + ";");
        }
        
        pane.setPrefWidth(model.getWidth());
        pane.setPrefHeight(model.getHeight());
    }

    private static Image loadImage(String url) {
        try {
            if (url.startsWith("classpath:")) {
                String resource = url.substring("classpath:".length());
                InputStream stream = BuiltInElementTypes.class.getResourceAsStream(resource);
                if (stream != null) {
                    return new Image(stream);
                }
            }
            return new Image(url, true);
        } catch (Exception e) {
            return null;
        }
    }

    private static void applyFont(javafx.scene.control.Labeled labeled, GuiElementModel model) {
        String family = getString(model, "fontFamily", "System");
        double size = getDouble(model, "fontSize", 14);
        labeled.setFont(Font.font(family, size));
    }

    private static void applyTextFill(javafx.scene.control.Labeled labeled, GuiElementModel model) {
        String color = getString(model, "foreground", "#000000");
        if (color != null && !color.isBlank()) {
            try {
                labeled.setTextFill(Paint.valueOf(color));
            } catch (IllegalArgumentException ignored) {
                // Leave existing text fill when paint is invalid
            }
        }
    }

    private static void applyBackground(javafx.scene.control.Control control, GuiElementModel model) {
        String background = getString(model, "background", "transparent");
        if (background == null || background.isBlank() || "transparent".equalsIgnoreCase(background)) {
            control.setStyle("");
        } else {
            control.setStyle("-fx-background-color: " + background + ";");
        }
    }

    private static String getString(GuiElementModel model, String key, String defaultValue) {
        Object value = model.getProperty(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private static boolean getBoolean(GuiElementModel model, String key, boolean defaultValue) {
        Object value = model.getProperty(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return defaultValue;
    }

    private static double getDouble(GuiElementModel model, String key, double defaultValue) {
        Object value = model.getProperty(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static List<String> parseItems(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Stream.of(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String colorStyle(GuiElementModel model) {
        String foreground = getString(model, "foreground", null);
        String background = getString(model, "background", null);
        StringBuilder style = new StringBuilder();
        if (foreground != null && !foreground.isBlank()) {
            style.append("-fx-text-fill: ").append(foreground).append(";");
        }
        if (background != null && !background.isBlank()) {
            style.append("-fx-control-inner-background: ").append(background).append(";");
        }
        return style.toString();
    }
}
