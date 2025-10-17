# Form Editor Library

Form Editor Library is an embeddable JavaFX component that delivers a production-ready WYSIWYG form builder. Drop the editor into any Java 17+ application to design WinAPI-style interfaces with drag & drop, live property editing, keyboard shortcuts, and JSON-based persistence.

## Features

- JavaFX-based palette ➜ canvas ➜ property inspector layout.
- Drag elements from the palette to the canvas, move with the mouse, or resize with corner handles.
- Property grid shows form- or element-level fields; text entries update the canvas immediately.
- File menu (New/Load/Save/Exit) and Edit menu (Copy/Cut/Paste) with standard shortcuts (Ctrl/Cmd+C/X/V and Ctrl/Cmd+S).
- JSON persistence using schema v1.0 (data-only; no UI serialization).
- Extensible element registry with WinAPI-like controls: Label, TextField, TextArea, Button, CheckBox, RadioButton, ComboBox, ListView, ProgressBar, Slider, DatePicker, Separator, and ImageView.
- Demo desktop app showcasing the editor out of the box.

## Project Structure

```
├── build.gradle
├── gradlew / gradlew.bat / gradle-wrapper.properties
├── src
│   ├── main
│   │   └── java
│   │       └── com/example/formeditor
│   │           ├── FormEditorView.java
│   │           ├── canvas/...
│   │           ├── model/...
│   │           ├── palette/...
│   │           ├── properties/...
│   │           └── registry/...
│   └── test
│       └── java
│           └── com/example/formeditor
│               ├── serialization/FormPersistenceTest.java
│               ├── model/GuiElementModelTest.java
│               └── registry/ElementRenderingTest.java
├── examples
│   ├── blank.json
│   └── sample-login.json
└── README.md
```

## Build & Run

```bash
./gradlew run
./gradlew jar
```

> **Note**: Gradle’s toolchain expects a Java 17+ JDK. If none is installed, configure a JDK or provide toolchain download repositories (see Gradle docs).

## Embedding in Your App

```java
FormEditorView editor = new FormEditorView();
editor.newForm(800, 600, "MyForm");

// Load from JSON
try (InputStream in = Files.newInputStream(Path.of("myform.json"))) {
    editor.loadJson(in);
}

// Host app can get/save JSON
String json = editor.toJson();
Files.writeString(Path.of("myform.json"), json);

// Add to your JavaFX scene
Scene scene = new Scene(editor, 1200, 800);
stage.setScene(scene);
stage.show();
```

### Library API Highlights

- `FormEditorView` – full editor UI (`getCanvas()`, `getModel()`, `setModel()`, `newForm(...)`, `loadJson(...)`, `toJson()`, `copy()`, `cut()`, `paste()`, selection accessors, `dirtyProperty()`).
- `FormEditorCanvas` – exposed for advanced integrations (custom overlays, event hooks).
- `ElementRegistry` / `BuiltInElementTypes` – register new control types with descriptors, renderers, and default factories.
- `FormPersistence` – JSON serialize/deserialize; validates schema v1.0.

## JSON Schema v1.0

```json
{
  "schemaVersion": "1.0",
  "form": {
    "id": "form-uuid",
    "name": "MainForm",
    "width": 800,
    "height": 600,
    "background": "#F0F0F0"
  },
  "elements": [
    {
      "id": "elem-uuid-1",
      "type": "Label",
      "props": {
        "text": "Hello",
        "x": 40,
        "y": 30,
        "width": 100,
        "height": 24
      }
    }
  ]
}
```

- Persist **data only** (ids, types, and property values). UI nodes are recreated on load.
- Required layout props per element: `x`, `y`, `width`, `height`.
- Additional props vary per element and are described by `PropertyDescriptor`s in the registry.
- Schema validation: mismatched `schemaVersion` throws a `FormSerializationException`.

## Extending the Editor

1. Implement a custom `ElementType` (or reuse `SimpleElementType`) providing:
   - Default property map and creation logic.
   - JavaFX node factory.
   - Property descriptors (name, kind, default, layout flag).
   - Renderer that applies props to the node.
2. Register with `ElementRegistry.register(...)` during application startup.
3. Refresh the palette: `paletteView.refreshItems()` (callable via `editor.getCanvas()` or maintain direct reference).

Property grid edits are string-based; ensure your renderer parses values appropriately (numbers, booleans, colors, etc.).

## Testing

Automated tests cover:

- JSON round-trip (`FormPersistenceTest`).
- Layout updates on the model when moving/resizing (`GuiElementModelTest`).
- Rendering fidelity, ensuring property changes appear on JavaFX nodes (`ElementRenderingTest`).

Run all tests with `./gradlew test` (requires a JavaFX-capable JDK).

## Examples

- `examples/blank.json` – minimal scaffold for new projects.
- `examples/sample-login.json` – sample login form illustrating multiple control types.

## Troubleshooting

- **JavaFX runtime not found**: ensure Gradle resolves OpenJFX modules (internet access required for dependency download).
- **No Java 17 toolchain**: install a JDK manually or configure Gradle toolchains with download repositories.
- **Invalid property values**: the inspector validates numbers/booleans; colors and dates fall back gracefully when invalid but may not render as expected.

Happy form building!
