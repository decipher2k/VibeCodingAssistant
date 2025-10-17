/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.awt.Component;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.vibecoding.wizard.DialogDefinition;
import com.vibecoding.wizard.DialogListSection;
import com.vibecoding.wizard.FormLayoutBuilder;
import com.vibecoding.wizard.MultiLineSection;
import com.vibecoding.wizard.UiUtils;

public final class UiComponentTests {
    private UiComponentTests() {
    }

    public static void run(TestContext ctx) {
        MultiLineSection section = new MultiLineSection("Caption", "Help text");
        section.setText("  Hello World  \n");
        ctx.assertEquals("MultiLineSection getText trims", "Hello World", section.getText());

        JPanel panel = FormLayoutBuilder.vertical(new JPanel(), new JPanel());
        ctx.assertNotNull("FormLayoutBuilder returns panel", panel);
        ctx.assertEquals("FormLayoutBuilder child count", 2, panel.getComponentCount());

        JComponent child = new JPanel();
        JPanel fieldSection = UiUtils.createFieldSection("Name", "Help title", "Detailed help", child);
        ctx.assertEquals("Field section child count", 2, fieldSection.getComponentCount());
        Component header = fieldSection.getComponent(0);
        ctx.assertTrue("Header is JPanel", header instanceof JPanel);
        JPanel headerPanel = (JPanel) header;
        ctx.assertTrue("Header has label", headerPanel.getComponent(0) instanceof JLabel);
        ctx.assertEquals("Label text", "Name", ((JLabel) headerPanel.getComponent(0)).getText());
        ctx.assertTrue("Header has button", headerPanel.getComponent(1) instanceof JButton);
        ctx.assertEquals("Help button text", "?", ((JButton) headerPanel.getComponent(1)).getText());

        JScrollPane scrollPane = UiUtils.wrapInScrollPane(child);
        ctx.assertNotNull("wrapInScrollPane returns value", scrollPane);
        ctx.assertEquals("Scroll pane viewport", child, scrollPane.getViewport().getView());

        DialogListSection dialogList = new DialogListSection("Dialogs");
        dialogList.setDialogs(List.of(new DialogDefinition("Login", "Login", "Desc")));
        ctx.assertEquals("Dialog list getDialogs size", 1, dialogList.getDialogs().size());
        dialogList.setVisible(true);
    }
}
