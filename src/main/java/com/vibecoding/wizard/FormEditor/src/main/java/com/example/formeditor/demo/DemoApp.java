/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.demo;

import com.example.formeditor.FormEditorView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Demonstration application embedding the {@link FormEditorView}.
 */
public class DemoApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        FormEditorView editorView = new FormEditorView();
        Scene scene = new Scene(editorView, 1200, 800);
        primaryStage.setTitle("Form Editor Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
