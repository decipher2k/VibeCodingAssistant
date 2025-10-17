/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.TaskType;

/**
 * Builder for the Inventory Management module.
 */
public class InventoryModuleBuilder {
    
    public static Module build() {
        Module module = new Module("InventoryManagement", TaskType.CREATE_MODULE);
        
        MainTaskData taskData = new MainTaskData();
        
        taskData.setProjectOverview(
            "Inventory Management Module\n\n" +
            "This module provides complete inventory control and warehouse management functionality. " +
            "It tracks product stock levels, handles stock adjustments, manages product information, " +
            "and provides alerts for low stock situations. The module supports barcode scanning for " +
            "quick product lookup and includes comprehensive reporting capabilities.\n\n" +
            "Key Features:\n" +
            "- Product catalog management with SKU tracking\n" +
            "- Real-time stock level monitoring\n" +
            "- Stock adjustment tracking (Receive, Issue, Count, Damage, Return)\n" +
            "- Low stock alerts and reorder recommendations\n" +
            "- Barcode scanner integration\n" +
            "- Multiple warehouse location support\n" +
            "- Product image management\n" +
            "- Category and supplier organization\n" +
            "- Stock valuation reports\n" +
            "- Inventory movement history\n" +
            "- Automated reorder level monitoring"
        );
        
        taskData.setThemeDescription(
            "Consistent business theme. Use traffic light colors for stock indicators: " +
            "Red for out of stock or critically low, Orange for below reorder level, " +
            "Green for adequate stock. Product images displayed as thumbnails with hover zoom. " +
            "Barcode scanner dialog has large input field with scan animation. " +
            "Stock adjustment forms use calculator-style numeric input. " +
            "Reports use tables with alternating row colors and summary rows in bold. " +
            "Category badges use subtle background colors. Warehouse locations use icon indicators."
        );
        
        // Set dialogs
        taskData.setDialogs(DialogFactory.createInventoryDialogs());
        taskData.setMainWindowName("InventoryListDialog");
        
        // Set workflows
        taskData.setWorkflowItems(WorkflowFactory.createInventoryWorkflows());
        
        module.setTaskData(taskData);
        module.setModuleVariables(VariableFactory.createInventoryModuleVariables());
        
        return module;
    }
}
