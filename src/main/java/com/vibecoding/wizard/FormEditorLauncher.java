/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import com.example.formeditor.FormEditorView;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * Utility class to launch the JavaFX FormEditor from a Swing application context.
 * Handles the JavaFX initialization and provides methods to exchange form data.
 */
public final class FormEditorLauncher {
    
    private static boolean javafxInitialized = false;
    
    private FormEditorLauncher() {
    }
    
    /**
     * Ensures JavaFX platform is initialized.
     */
    private static void ensureJavaFXInitialized() {
        if (!javafxInitialized) {
            // Create a JFXPanel to initialize JavaFX toolkit
            new JFXPanel();
            // Prevent JavaFX from exiting when the last Stage closes
            Platform.setImplicitExit(false);
            javafxInitialized = true;
            System.out.println("ensureJavaFXInitialized: SUCCESS (implicitExit set to false)");
        } else {
            System.out.println("ensureJavaFXInitialized: Already initialized");
        }
    }
    
    /**
     * Opens the Form Editor in a new window with optional initial JSON data.
     * Returns the JSON string of the edited form when the window is closed.
     * 
     * @param initialJson Optional initial JSON data to load, or null for a new form
     * @param dialogName The name of the dialog being edited
     * @param parentWindow The parent Swing window (JFrame or JDialog), used for positioning
     * @return A CompletableFuture that will contain the resulting JSON when the editor closes
     */
    public static CompletableFuture<String> openFormEditor(String initialJson, String dialogName, Window parentWindow) {
        ensureJavaFXInitialized();
        
        CompletableFuture<String> result = new CompletableFuture<>();
        
        // Store parent window visibility state
        final boolean wasVisible = parentWindow != null && parentWindow.isVisible();
        
        try {
            Platform.runLater(() -> {
                try {
                    Stage stage = new Stage();
                    
                    // Make the stage modal to block interaction with parent
                    stage.initModality(Modality.APPLICATION_MODAL);
                    
                    FormEditorView editorView = new FormEditorView();
                    
                    // Load initial JSON if provided
                    if (initialJson != null && !initialJson.trim().isEmpty()) {
                        try {
                            editorView.loadJson(new java.io.ByteArrayInputStream(
                                initialJson.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                        } catch (Exception e) {
                            System.err.println("Failed to load initial form JSON: " + e.getMessage());
                            e.printStackTrace();
                            // Continue with default form
                        }
                    } else {
                        // Create a new form with the dialog name
                        editorView.newForm(400, 400, dialogName != null ? dialogName : "DialogForm");
                    }
                    
                    Scene scene = new Scene(editorView, 1200, 800);
                    stage.setScene(scene);
                    stage.setTitle("Form Editor - " + (dialogName != null ? dialogName : "Dialog"));
                    
                    // Handle window close - capture the JSON and complete the future
                    stage.setOnCloseRequest(event -> {
                        try {
                            String json = editorView.toJson();
                            result.complete(json);
                            
                            // Don't need to restore parent visibility since we're not hiding it anymore
                            // Just ensure it comes back to front
                            if (parentWindow != null) {
                                SwingUtilities.invokeLater(() -> {
                                    parentWindow.toFront();
                                    parentWindow.requestFocus();
                                });
                            }
                        } catch (Exception e) {
                            System.err.println("Error getting JSON on close: " + e.getMessage());
                            e.printStackTrace();
                            result.completeExceptionally(e);
                        }
                    });
                    
                    // Position relative to parent if available
                    if (parentWindow != null) {
                        // Calculate position before showing
                        stage.setX(parentWindow.getX() + 50);
                        stage.setY(parentWindow.getY() + 50);
                        
                        // Don't hide the parent - JavaFX modal stage should handle blocking
                        // SwingUtilities.invokeLater(() -> parentWindow.setVisible(false));
                    }
                    
                    stage.show();
                    
                    // Bring to front and focus
                    Platform.runLater(() -> {
                        stage.toFront();
                        stage.requestFocus();
                    });
                    
                    System.out.println("FormEditor stage shown successfully");
                    
                } catch (Exception e) {
                    System.err.println("Error in Platform.runLater: " + e.getMessage());
                    e.printStackTrace();
                    result.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            System.err.println("Error calling Platform.runLater: " + e.getMessage());
            e.printStackTrace();
            result.completeExceptionally(e);
        }
        
        return result;
    }
    
    /**
     * Opens the Form Editor modally (blocking the calling thread until the editor closes).
     * 
     * @param initialJson Optional initial JSON data to load, or null for a new form
     * @param dialogName The name of the dialog being edited
     * @param parentWindow The parent Swing window (JFrame or JDialog), used for positioning
     * @return The resulting JSON string, or null if the user canceled
     */
    public static String openFormEditorModal(String initialJson, String dialogName, Window parentWindow) {
        try {
            return openFormEditor(initialJson, dialogName, parentWindow).get();
        } catch (Exception e) {
            System.err.println("Error opening form editor: " + e.getMessage());
            return null;
        }
    }
}
