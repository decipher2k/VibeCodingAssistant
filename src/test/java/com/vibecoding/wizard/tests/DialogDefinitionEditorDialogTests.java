/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.vibecoding.wizard.DialogDefinition;
import com.vibecoding.wizard.DialogDefinitionEditorDialog;

public final class DialogDefinitionEditorDialogTests {
    private DialogDefinitionEditorDialogTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            ctx.skip("Graphics environment is headless");
            return;
        }

        DialogDefinition existing = new DialogDefinition("Login", "Login Window", "Description");

        SwingUtilities.invokeAndWait(() -> {
            DialogDefinitionEditorDialog dialog = new DialogDefinitionEditorDialog(new JPanel(), existing);
            try {
                ctx.assertEquals("Initial name", "Login", readText(dialog, "nameField"));
                ctx.assertEquals("Initial title", "Login Window", readText(dialog, "windowTitleField"));

                setText(dialog, "nameField", "Signup");
                setText(dialog, "windowTitleField", "Signup Window");
                setText(dialog, "descriptionArea", "Signup description");

                JButton saveButton = findButton((JComponent) dialog.getContentPane(), "Save");
                ctx.assertNotNull("Save button found", saveButton);
                saveButton.doClick();

                ctx.assertTrue("Dialog result present", dialog.getResult().isPresent());
                ctx.assertEquals("Result name", "Signup", dialog.getResult().get().getName());
            } finally {
                dialog.dispose();
            }
        });
    }

    private static String readText(DialogDefinitionEditorDialog dialog, String fieldName) {
        try {
            Field field = DialogDefinitionEditorDialog.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(dialog);
            if (value instanceof JTextComponent component) {
                return component.getText();
            }
            throw new IllegalStateException("Field " + fieldName + " is not a text component");
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void setText(DialogDefinitionEditorDialog dialog, String fieldName, String value) {
        try {
            Field field = DialogDefinitionEditorDialog.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object obj = field.get(dialog);
            if (obj instanceof JTextComponent component) {
                component.setText(value);
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static JButton findButton(JComponent parent, String label) {
        for (java.awt.Component child : parent.getComponents()) {
            if (child instanceof JButton button && label.equals(button.getText())) {
                return button;
            }
            if (child instanceof JComponent nested) {
                JButton candidate = findButton(nested, label);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }
}
