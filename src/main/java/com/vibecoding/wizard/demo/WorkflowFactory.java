/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.WorkflowItem;
import com.vibecoding.wizard.WorkflowStep;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory for creating workflow definitions for demo modules.
 */
public class WorkflowFactory {
    
    /**
     * Creates customer module workflows.
     */
    public static List<WorkflowItem> createCustomerWorkflows() {
        List<WorkflowItem> workflows = new ArrayList<>();
        
        workflows.add(new WorkflowItem(
            "AddNewCustomer",
            "CustomerListDialog",
            "User clicks 'Add New' button",
            Arrays.asList(
                new WorkflowStep("Open CustomerDetailsDialog in create mode", "", false),
                new WorkflowStep("User fills in required fields (First Name, Last Name, Email)", "All required fields must be filled", true),
                new WorkflowStep("System validates email format and checks for duplicates", "Email must be unique", true),
                new WorkflowStep("User clicks 'Save' button", "", false),
                new WorkflowStep("System saves customer to database and generates Customer ID", "", false),
                new WorkflowStep("Close CustomerDetailsDialog and refresh CustomerListDialog", "", false),
                new WorkflowStep("Select and highlight the newly created customer in the list", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "SearchAndEditCustomer",
            "CustomerListDialog",
            "User enters search criteria and selects a customer",
            Arrays.asList(
                new WorkflowStep("User types in search box", "", false),
                new WorkflowStep("System filters customer list in real-time", "", false),
                new WorkflowStep("User double-clicks on a customer row", "", false),
                new WorkflowStep("Open CustomerDetailsDialog in edit mode with customer data loaded", "", false),
                new WorkflowStep("User modifies fields and clicks 'Save'", "", false),
                new WorkflowStep("System validates changes", "Modified data must be valid", true),
                new WorkflowStep("Update database and refresh CustomerListDialog", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "ViewCustomerHistory",
            "CustomerDetailsDialog",
            "User clicks 'View History' button",
            Arrays.asList(
                new WorkflowStep("Open CustomerHistoryDialog with customer ID", "", false),
                new WorkflowStep("Load customer's order history from database", "", false),
                new WorkflowStep("Display orders in grid with calculated summaries", "", false),
                new WorkflowStep("User can double-click order to view details (opens OrderDetailsDialog from Order module)", "", false)
            )
        ));
        
        return workflows;
    }
    
    /**
     * Creates order module workflows.
     */
    public static List<WorkflowItem> createOrderWorkflows() {
        List<WorkflowItem> workflows = new ArrayList<>();
        
        workflows.add(new WorkflowItem(
            "CreateNewOrder",
            "OrderListDialog",
            "User clicks 'New Order' button",
            Arrays.asList(
                new WorkflowStep("Open CreateOrderDialog wizard", "", false),
                new WorkflowStep("Step 1: User selects customer from dropdown", "Customer must be selected", true),
                new WorkflowStep("Step 2: User searches and adds products with quantities", "At least one product must be added", true),
                new WorkflowStep("System calculates line totals and order total", "", false),
                new WorkflowStep("Step 3: User enters shipping information", "Shipping address required", true),
                new WorkflowStep("System calculates shipping cost and updates total", "", false),
                new WorkflowStep("Step 4: User selects payment method and enters details", "Valid payment information required", true),
                new WorkflowStep("Step 5: User reviews all information", "", false),
                new WorkflowStep("User clicks 'Submit Order'", "", false),
                new WorkflowStep("System creates order, reduces inventory (calls Inventory module)", "", false),
                new WorkflowStep("Close CreateOrderDialog and refresh OrderListDialog", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "ProcessPayment",
            "OrderDetailsDialog",
            "User clicks 'Record Payment' on unpaid order",
            Arrays.asList(
                new WorkflowStep("Verify order status allows payment", "Order must be confirmed and not cancelled", true),
                new WorkflowStep("Display payment dialog with order amount", "", false),
                new WorkflowStep("User enters payment details", "Valid payment information required", true),
                new WorkflowStep("System processes payment through payment gateway", "", false),
                new WorkflowStep("Update order payment status to 'Paid'", "", false),
                new WorkflowStep("Generate and display invoice (OrderInvoiceDialog)", "", false),
                new WorkflowStep("Send email notification to customer", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "CancelOrder",
            "OrderDetailsDialog",
            "User clicks 'Cancel Order' button",
            Arrays.asList(
                new WorkflowStep("Check if order can be cancelled", "Order must not be shipped or delivered", true),
                new WorkflowStep("Display confirmation dialog with reason input", "", false),
                new WorkflowStep("User confirms cancellation", "", false),
                new WorkflowStep("Update order status to 'Cancelled'", "", false),
                new WorkflowStep("Restore inventory quantities (calls Inventory module)", "", false),
                new WorkflowStep("Process refund if payment was received", "", false),
                new WorkflowStep("Send cancellation email to customer", "", false),
                new WorkflowStep("Log cancellation in audit trail", "", false)
            )
        ));
        
        return workflows;
    }
    
    /**
     * Creates inventory module workflows.
     */
    public static List<WorkflowItem> createInventoryWorkflows() {
        List<WorkflowItem> workflows = new ArrayList<>();
        
        workflows.add(new WorkflowItem(
            "AddNewProduct",
            "InventoryListDialog",
            "User clicks 'Add Product' button",
            Arrays.asList(
                new WorkflowStep("Open ProductDetailsDialog in create mode", "", false),
                new WorkflowStep("User enters product information", "SKU and Name are required", true),
                new WorkflowStep("System validates SKU is unique", "SKU must be unique", true),
                new WorkflowStep("User uploads product images", "", false),
                new WorkflowStep("User clicks 'Save'", "", false),
                new WorkflowStep("System saves product to database", "", false),
                new WorkflowStep("Close ProductDetailsDialog and refresh InventoryListDialog", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "ReceiveStock",
            "InventoryListDialog",
            "User clicks 'Stock Adjustment' button",
            Arrays.asList(
                new WorkflowStep("Open StockAdjustmentDialog", "", false),
                new WorkflowStep("User scans or selects product", "Product must be selected", true),
                new WorkflowStep("Display current stock level", "", false),
                new WorkflowStep("User selects 'Receive' adjustment type", "", false),
                new WorkflowStep("User enters quantity received", "Quantity must be positive", true),
                new WorkflowStep("User enters reference number (PO number)", "", false),
                new WorkflowStep("System calculates new stock level", "", false),
                new WorkflowStep("User clicks 'Apply'", "", false),
                new WorkflowStep("Update inventory database", "", false),
                new WorkflowStep("Log transaction in inventory movement history", "", false),
                new WorkflowStep("Check if product was below reorder level and send notification if now restocked", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "LowStockAlert",
            "InventoryListDialog",
            "System checks inventory levels (scheduled task)",
            Arrays.asList(
                new WorkflowStep("Query all products where quantity <= reorder level", "", false),
                new WorkflowStep("Generate low stock report", "", false),
                new WorkflowStep("Display notification badge on InventoryListDialog", "", false),
                new WorkflowStep("Send email alert to inventory manager", "", false),
                new WorkflowStep("For each low stock item, suggest reorder quantity", "", false)
            )
        ));
        
        return workflows;
    }
    
    /**
     * Creates reporting module workflows.
     */
    public static List<WorkflowItem> createReportingWorkflows() {
        List<WorkflowItem> workflows = new ArrayList<>();
        
        workflows.add(new WorkflowItem(
            "ViewDashboard",
            "DashboardDialog",
            "User opens application or clicks Dashboard menu",
            Arrays.asList(
                new WorkflowStep("Load dashboard with default date range (last 30 days)", "", false),
                new WorkflowStep("Query sales data from Order module", "", false),
                new WorkflowStep("Query customer data from Customer module", "", false),
                new WorkflowStep("Calculate KPIs: Today's Sales, Monthly Revenue, Active Customers, Pending Orders", "", false),
                new WorkflowStep("Render charts: Sales trend, Revenue by category, Top performers", "", false),
                new WorkflowStep("Display widget grid with drag-drop positioning", "", false),
                new WorkflowStep("Start auto-refresh timer if enabled", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "GenerateSalesReport",
            "SalesReportDialog",
            "User clicks 'Generate' button",
            Arrays.asList(
                new WorkflowStep("Validate filter selections", "Date range is required", true),
                new WorkflowStep("Show progress indicator", "", false),
                new WorkflowStep("Query sales data based on filters from Order module", "", false),
                new WorkflowStep("Aggregate data by selected grouping (Product/Customer/Date/Region)", "", false),
                new WorkflowStep("Calculate totals, averages, and percentages", "", false),
                new WorkflowStep("Generate charts for visual representation", "", false),
                new WorkflowStep("Display report in tabs", "", false),
                new WorkflowStep("Enable export buttons", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "ExportReport",
            "SalesReportDialog",
            "User clicks 'Export to Excel' button",
            Arrays.asList(
                new WorkflowStep("Prompt user for export options (include charts, format)", "", false),
                new WorkflowStep("Show file save dialog", "", false),
                new WorkflowStep("User selects file location", "Valid file path required", true),
                new WorkflowStep("Generate Excel file with data and formatting", "", false),
                new WorkflowStep("Include charts if option selected", "", false),
                new WorkflowStep("Save file to disk", "", false),
                new WorkflowStep("Show success message with option to open file", "", false)
            )
        ));
        
        return workflows;
    }
    
    /**
     * Creates administration module workflows.
     */
    public static List<WorkflowItem> createAdminWorkflows() {
        List<WorkflowItem> workflows = new ArrayList<>();
        
        workflows.add(new WorkflowItem(
            "CreateUser",
            "UserManagementDialog",
            "User clicks 'Add User' button",
            Arrays.asList(
                new WorkflowStep("Open UserDetailsDialog in create mode", "", false),
                new WorkflowStep("Admin enters username, full name, and email", "All required fields must be filled", true),
                new WorkflowStep("System validates username is unique", "Username must be unique", true),
                new WorkflowStep("System validates email format", "Valid email required", true),
                new WorkflowStep("Admin selects role from dropdown", "Role must be selected", true),
                new WorkflowStep("System loads role permissions", "", false),
                new WorkflowStep("Admin can override specific permissions", "", false),
                new WorkflowStep("System generates temporary password", "", false),
                new WorkflowStep("Admin clicks 'Save & Send Invitation'", "", false),
                new WorkflowStep("Create user account in database", "", false),
                new WorkflowStep("Send invitation email with temporary password", "", false),
                new WorkflowStep("Log user creation in audit log", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "ModifyPermissions",
            "UserDetailsDialog",
            "Admin modifies user permissions",
            Arrays.asList(
                new WorkflowStep("Admin opens user for editing", "", false),
                new WorkflowStep("Navigate to Permissions tab", "", false),
                new WorkflowStep("View inherited permissions from role (read-only)", "", false),
                new WorkflowStep("Admin checks/unchecks additional permission checkboxes", "", false),
                new WorkflowStep("Admin adds permission exceptions if needed", "", false),
                new WorkflowStep("System validates permission combination", "Must have at least basic access", true),
                new WorkflowStep("Admin clicks 'Save'", "", false),
                new WorkflowStep("Update user permissions in database", "", false),
                new WorkflowStep("Force user to re-login if currently active", "", false),
                new WorkflowStep("Log permission changes in audit log", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "PerformBackup",
            "BackupRestoreDialog",
            "Admin clicks 'Backup Now' button",
            Arrays.asList(
                new WorkflowStep("Validate backup destination is accessible", "Backup destination must be writable", true),
                new WorkflowStep("Show confirmation dialog with backup details", "", false),
                new WorkflowStep("Admin confirms backup", "", false),
                new WorkflowStep("Lock database tables for consistency", "", false),
                new WorkflowStep("Export database schema and data", "", false),
                new WorkflowStep("Include file attachments if option selected", "", false),
                new WorkflowStep("Compress backup file", "", false),
                new WorkflowStep("Unlock database tables", "", false),
                new WorkflowStep("Verify backup file integrity", "Backup must be valid", true),
                new WorkflowStep("Update backup history log", "", false),
                new WorkflowStep("Show success message with backup size and location", "", false)
            )
        ));
        
        return workflows;
    }
}
