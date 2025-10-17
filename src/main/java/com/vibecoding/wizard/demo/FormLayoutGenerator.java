/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class to generate form layout JSON for demo dialogs.
 * Creates programmatically defined forms with standard controls.
 */
public class FormLayoutGenerator {
    
    private static int nextElementId = 1;
    
    /**
     * Generates a unique element ID.
     */
    private static String generateElementId() {
        return "elem_" + (nextElementId++);
    }
    
    /**
     * Creates a simple list/grid dialog layout.
     */
    public static String createListDialog(String formName, String gridLabel, String[] columns, int formWidth, int formHeight) {
        List<Map<String, Object>> elements = new ArrayList<>();
        
        // Add label for grid title
        Map<String, Object> titleProps = new LinkedHashMap<>();
        titleProps.put("x", 10.0);
        titleProps.put("y", 10.0);
        titleProps.put("width", (double)(formWidth - 120));
        titleProps.put("height", 30.0);
        titleProps.put("text", gridLabel);
        elements.add(createElementMap(generateElementId(), "Label", titleProps));
        
        // Add ListView control (closest to DataGrid)
        Map<String, Object> listProps = new LinkedHashMap<>();
        listProps.put("x", 10.0);
        listProps.put("y", 50.0);
        listProps.put("width", (double)(formWidth - 20));
        listProps.put("height", (double)(formHeight - 120));
        listProps.put("text", "");
        elements.add(createElementMap(generateElementId(), "ListView", listProps));
        
        // Add search textbox
        Map<String, Object> searchProps = new LinkedHashMap<>();
        searchProps.put("x", (double)(formWidth - 110));
        searchProps.put("y", 10.0);
        searchProps.put("width", 100.0);
        searchProps.put("height", 30.0);
        searchProps.put("text", "");
        elements.add(createElementMap(generateElementId(), "TextField", searchProps));
        
        // Add buttons
        int buttonY = formHeight - 60;
        elements.add(createButton("Add", 10, buttonY, 80, 30));
        elements.add(createButton("Edit", 100, buttonY, 80, 30));
        elements.add(createButton("Delete", 190, buttonY, 80, 30));
        elements.add(createButton("Refresh", 280, buttonY, 80, 30));
        elements.add(createButton("Close", formWidth - 100, buttonY, 80, 30));
        
        return buildFormJson(UUID.randomUUID().toString(), formName, formWidth, formHeight, elements);
    }
    
    /**
     * Creates a detailed form dialog layout.
     */
    public static String createDetailFormDialog(String formName, String[] fieldLabels, int formWidth, int formHeight) {
        List<Map<String, Object>> elements = new ArrayList<>();
        
        int yPos = 20;
        int labelWidth = 120;
        int fieldWidth = formWidth - labelWidth - 40;
        int fieldHeight = 25;
        int spacing = 35;
        
        for (String label : fieldLabels) {
            // Add label
            Map<String, Object> labelProps = new LinkedHashMap<>();
            labelProps.put("x", 10.0);
            labelProps.put("y", (double)yPos);
            labelProps.put("width", (double)labelWidth);
            labelProps.put("height", (double)fieldHeight);
            labelProps.put("text", label + ":");
            elements.add(createElementMap(generateElementId(), "Label", labelProps));
            
            // Add textbox
            Map<String, Object> textProps = new LinkedHashMap<>();
            textProps.put("x", (double)(labelWidth + 20));
            textProps.put("y", (double)yPos);
            textProps.put("width", (double)fieldWidth);
            textProps.put("height", (double)fieldHeight);
            textProps.put("text", "");
            elements.add(createElementMap(generateElementId(), "TextField", textProps));
            
            yPos += spacing;
        }
        
        // Add buttons at bottom
        int buttonY = formHeight - 60;
        elements.add(createButton("Save", formWidth - 280, buttonY, 80, 30));
        elements.add(createButton("Save & Close", formWidth - 190, buttonY, 100, 30));
        elements.add(createButton("Cancel", formWidth - 90, buttonY, 80, 30));
        
        return buildFormJson(UUID.randomUUID().toString(), formName, formWidth, formHeight, elements);
    }
    
    /**
     * Creates a search/filter dialog layout.
     */
    public static String createSearchDialog(String formName, String[] filterLabels, int formWidth, int formHeight) {
        List<Map<String, Object>> elements = new ArrayList<>();
        
        int yPos = 20;
        int labelWidth = 120;
        int fieldWidth = formWidth - labelWidth - 40;
        int fieldHeight = 25;
        int spacing = 35;
        
        for (String label : filterLabels) {
            // Add label
            Map<String, Object> labelProps = new LinkedHashMap<>();
            labelProps.put("x", 10.0);
            labelProps.put("y", (double)yPos);
            labelProps.put("width", (double)labelWidth);
            labelProps.put("height", (double)fieldHeight);
            labelProps.put("text", label + ":");
            elements.add(createElementMap(generateElementId(), "Label", labelProps));
            
            // Add textbox for filter
            Map<String, Object> textProps = new LinkedHashMap<>();
            textProps.put("x", (double)(labelWidth + 20));
            textProps.put("y", (double)yPos);
            textProps.put("width", (double)fieldWidth);
            textProps.put("height", (double)fieldHeight);
            textProps.put("text", "");
            elements.add(createElementMap(generateElementId(), "TextField", textProps));
            
            yPos += spacing;
        }
        
        // Add buttons at bottom
        int buttonY = formHeight - 60;
        elements.add(createButton("Search", formWidth - 280, buttonY, 80, 30));
        elements.add(createButton("Clear", formWidth - 190, buttonY, 80, 30));
        elements.add(createButton("Close", formWidth - 90, buttonY, 80, 30));
        
        return buildFormJson(UUID.randomUUID().toString(), formName, formWidth, formHeight, elements);
    }
    
    /**
     * Creates a dashboard layout with panels.
     */
    public static String createDashboardDialog(String formName, int formWidth, int formHeight) {
        List<Map<String, Object>> elements = new ArrayList<>();
        
        int panelWidth = (formWidth - 40) / 2;
        int panelHeight = (formHeight - 40) / 2;
        
        // Top-left panel with label
        elements.add(createPanel("Sales Overview", 10, 10, panelWidth, panelHeight));
        elements.add(createLabel("Sales Overview", 20, 20, panelWidth - 20, 30));
        elements.add(createLabel("Total Sales: $125,450", 20, 60, panelWidth - 20, 25));
        elements.add(createLabel("Orders: 342", 20, 90, panelWidth - 20, 25));
        
        // Top-right panel with label
        elements.add(createPanel("Revenue Chart", panelWidth + 20, 10, panelWidth, panelHeight));
        elements.add(createLabel("Revenue Chart", panelWidth + 30, 20, panelWidth - 20, 30));
        elements.add(createLabel("Monthly Trend", panelWidth + 30, 60, panelWidth - 20, 25));
        
        // Bottom-left panel with label
        elements.add(createPanel("Recent Orders", 10, panelHeight + 20, panelWidth, panelHeight));
        elements.add(createLabel("Recent Orders", 20, panelHeight + 30, panelWidth - 20, 30));
        
        // Bottom-right panel with label
        elements.add(createPanel("Top Customers", panelWidth + 20, panelHeight + 20, panelWidth, panelHeight));
        elements.add(createLabel("Top Customers", panelWidth + 30, panelHeight + 30, panelWidth - 20, 30));
        
        return buildFormJson(UUID.randomUUID().toString(), formName, formWidth, formHeight, elements);
    }
    
    /**
     * Creates a label element map.
     */
    private static Map<String, Object> createLabel(String text, int x, int y, int width, int height) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("x", (double)x);
        props.put("y", (double)y);
        props.put("width", (double)width);
        props.put("height", (double)height);
        props.put("text", text);
        return createElementMap(generateElementId(), "Label", props);
    }
    
    /**
     * Creates a button element map.
     */
    private static Map<String, Object> createButton(String text, int x, int y, int width, int height) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("x", (double)x);
        props.put("y", (double)y);
        props.put("width", (double)width);
        props.put("height", (double)height);
        props.put("text", text);
        return createElementMap(generateElementId(), "Button", props);
    }
    
    /**
     * Creates a panel element map.
     */
    private static Map<String, Object> createPanel(String text, int x, int y, int width, int height) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("x", (double)x);
        props.put("y", (double)y);
        props.put("width", (double)width);
        props.put("height", (double)height);
        props.put("text", text);
        return createElementMap(generateElementId(), "WindowHost", props);
    }
    
    /**
     * Creates an element map.
     */
    private static Map<String, Object> createElementMap(String id, String type, Map<String, Object> props) {
        Map<String, Object> element = new LinkedHashMap<>();
        element.put("id", id);
        element.put("type", type);
        element.put("props", props);
        element.put("events", new ArrayList<>());
        return element;
    }
    
    /**
     * Builds the complete form JSON.
     */
    private static String buildFormJson(String formId, String formName, int width, int height, List<Map<String, Object>> elements) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schemaVersion\": \"1.0\",\n");
        json.append("  \"form\": {\n");
        json.append("    \"id\": \"").append(formId).append("\",\n");
        json.append("    \"name\": \"").append(escapeJson(formName)).append("\",\n");
        json.append("    \"width\": ").append(width).append(",\n");
        json.append("    \"height\": ").append(height).append(",\n");
        json.append("    \"background\": \"#F0F0F0\",\n");
        json.append("    \"events\": []\n");
        json.append("  },\n");
        json.append("  \"elements\": [\n");
        
        for (int i = 0; i < elements.size(); i++) {
            Map<String, Object> elem = elements.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(elem.get("id")).append("\",\n");
            json.append("      \"type\": \"").append(elem.get("type")).append("\",\n");
            json.append("      \"props\": {\n");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> props = (Map<String, Object>) elem.get("props");
            List<String> propKeys = new ArrayList<>(props.keySet());
            for (int j = 0; j < propKeys.size(); j++) {
                String key = propKeys.get(j);
                Object value = props.get(key);
                json.append("        \"").append(key).append("\": ");
                if (value instanceof String) {
                    json.append("\"").append(escapeJson((String)value)).append("\"");
                } else {
                    json.append(value);
                }
                if (j < propKeys.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("      },\n");
            json.append("      \"events\": []\n");
            json.append("    }");
            if (i < elements.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Escapes special characters in JSON strings.
     */
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
