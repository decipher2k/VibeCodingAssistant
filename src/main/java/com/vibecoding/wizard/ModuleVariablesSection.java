/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public final class ModuleVariablesSection {
    private final JPanel component;
    private final DefaultListModel<ModuleVariable> variableModel = new DefaultListModel<>();
    private final JList<ModuleVariable> variableList = new JList<>(variableModel);
    private final JFrame parentFrame;
    private List<GlobalVariable> globalVariables = new ArrayList<>();
    
    public ModuleVariablesSection(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.component = createPanel();
    }
    
    private JPanel createPanel() {
        JPanel panel = UiUtils.createFieldSection(
            "Module Variables",
            "Module Variables",
            "Define module-specific variables that can be referenced in prompts for this module. " +
            "Module variables are scoped to this module only and can override or supplement global variables.",
            createVariablesContent());
        return panel;
    }
    
    private JPanel createVariablesContent() {
        JScrollPane listScroll = new JScrollPane(variableList);
        listScroll.setPreferredSize(new java.awt.Dimension(100, 120));
        listScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0xDDE4ED)));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        
        JButton addButton = new JButton("Add Variable");
        addButton.addActionListener(event -> addVariable());
        
        JButton editButton = new JButton("Edit Variable");
        editButton.addActionListener(event -> editVariable());
        
        JButton deleteButton = new JButton("Delete Variable");
        deleteButton.addActionListener(event -> deleteVariable());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.add(listScroll, java.awt.BorderLayout.CENTER);
        panel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        panel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        panel.setOpaque(false);
        
        return panel;
    }
    
    public JPanel getComponent() {
        return component;
    }
    
    public void setVariables(List<ModuleVariable> variables) {
        variableModel.clear();
        if (variables != null) {
            for (ModuleVariable var : variables) {
                variableModel.addElement(var);
            }
        }
    }
    
    public List<ModuleVariable> getVariables() {
        List<ModuleVariable> variables = new ArrayList<>();
        for (int i = 0; i < variableModel.size(); i++) {
            variables.add(variableModel.get(i));
        }
        return variables;
    }
    
    public void setGlobalVariables(List<GlobalVariable> globalVariables) {
        this.globalVariables = globalVariables != null ? globalVariables : new ArrayList<>();
    }
    
    private void addVariable() {
        ModuleVariableDialog dialog = new ModuleVariableDialog(
            parentFrame,
            null,
            getVariables(),
            globalVariables
        );
        dialog.setVisible(true);
        
        ModuleVariable newVar = dialog.getResult();
        if (newVar != null) {
            variableModel.addElement(newVar);
        }
    }
    
    private void editVariable() {
        ModuleVariable selected = variableList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        ModuleVariableDialog dialog = new ModuleVariableDialog(
            parentFrame,
            selected,
            getVariables(),
            globalVariables
        );
        dialog.setVisible(true);
        
        ModuleVariable edited = dialog.getResult();
        if (edited != null) {
            int index = variableList.getSelectedIndex();
            variableModel.set(index, edited);
        }
    }
    
    private void deleteVariable() {
        int selectedIndex = variableList.getSelectedIndex();
        if (selectedIndex >= 0) {
            variableModel.remove(selectedIndex);
        }
    }
}
