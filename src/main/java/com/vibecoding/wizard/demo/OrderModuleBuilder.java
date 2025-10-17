/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.TaskType;

/**
 * Builder for the Order Management module.
 */
public class OrderModuleBuilder {
    
    public static Module build() {
        Module module = new Module("OrderManagement", TaskType.CREATE_MODULE);
        
        MainTaskData taskData = new MainTaskData();
        
        taskData.setProjectOverview(
            "Order Management Module\n\n" +
            "This module handles the complete order lifecycle from creation to fulfillment. " +
            "It provides a comprehensive interface for managing customer orders, tracking order status, " +
            "processing payments, and generating invoices. The module supports multi-step order creation, " +
            "inventory integration, and shipping management.\n\n" +
            "Key Features:\n" +
            "- Multi-step order creation wizard\n" +
            "- Order status tracking (Pending, Confirmed, Shipped, Delivered, Cancelled)\n" +
            "- Payment processing and recording\n" +
            "- Invoice generation and printing\n" +
            "- Order search and filtering\n" +
            "- Bulk status updates\n" +
            "- Integration with Customer Management for customer data\n" +
            "- Integration with Inventory Management for stock updates\n" +
            "- Email notifications to customers\n" +
            "- Order cancellation with automatic refunds"
        );
        
        taskData.setThemeDescription(
            "Consistent with application theme. Primary: Dark Blue (#2C3E50), Secondary: Light Gray (#ECF0F1). " +
            "Use color-coded status badges: Blue for Pending, Green for Confirmed/Paid, Orange for Processing, " +
            "Purple for Shipped, Gray for Delivered, Red for Cancelled. Order wizard uses step indicators at top. " +
            "Invoice layout uses professional formatting with company header and footer. " +
            "Grid cells for amounts are right-aligned with currency formatting. " +
            "Action buttons grouped logically: Primary actions on right, secondary on left."
        );
        
        // Set dialogs
        taskData.setDialogs(DialogFactory.createOrderDialogs());
        taskData.setMainWindowName("OrderListDialog");
        
        // Set workflows
        taskData.setWorkflowItems(WorkflowFactory.createOrderWorkflows());
        
        module.setTaskData(taskData);
        module.setModuleVariables(VariableFactory.createOrderModuleVariables());
        
        return module;
    }
}
