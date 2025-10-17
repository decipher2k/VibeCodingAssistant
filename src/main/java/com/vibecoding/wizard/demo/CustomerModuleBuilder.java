/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.TaskType;

/**
 * Builder for the Customer Management module.
 */
public class CustomerModuleBuilder {
    
    public static Module build() {
        Module module = new Module("CustomerManagement", TaskType.CREATE_MODULE);
        
        MainTaskData taskData = new MainTaskData();
        
        taskData.setProjectOverview(
            "Customer Management Module\n\n" +
            "This module provides comprehensive customer relationship management functionality. " +
            "It handles customer data storage, search, and management with support for both individual " +
            "and business customers. The module tracks customer information including contact details, " +
            "addresses, credit limits, and status. It integrates with the Order Management module to " +
            "display customer purchase history and with the Reporting module for customer analytics.\n\n" +
            "Key Features:\n" +
            "- Customer CRUD operations (Create, Read, Update, Delete)\n" +
            "- Real-time search and filtering\n" +
            "- Customer categorization (Individual/Business)\n" +
            "- Credit limit management\n" +
            "- Order history integration\n" +
            "- Bulk import from CSV/Excel\n" +
            "- Export capabilities\n" +
            "- Advanced search with multiple criteria"
        );
        
        taskData.setThemeDescription(
            "Professional business theme with clean, modern aesthetics. Primary color: Dark Blue (#2C3E50), " +
            "Secondary color: Light Gray (#ECF0F1), Accent color: Green (#27AE60) for positive actions. " +
            "Use Segoe UI font family. Grid rows alternate between white and light gray (#F8F9FA). " +
            "Buttons have rounded corners (4px radius) with hover effects. Required field labels " +
            "marked with red asterisk. Use icons from Material Design Icons set. Status indicators " +
            "use color coding: Green for Active, Orange for Inactive, Red for Suspended."
        );
        
        // Set dialogs
        taskData.setDialogs(DialogFactory.createCustomerDialogs());
        taskData.setMainWindowName("CustomerListDialog");
        
        // Set workflows
        taskData.setWorkflowItems(WorkflowFactory.createCustomerWorkflows());
        
        module.setTaskData(taskData);
        module.setModuleVariables(VariableFactory.createCustomerModuleVariables());
        
        return module;
    }
}
