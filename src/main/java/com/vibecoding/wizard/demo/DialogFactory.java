/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.DialogDefinition;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating dialog definitions for demo modules.
 */
public class DialogFactory {
    
    /**
     * Creates customer management dialogs.
     */
    public static List<DialogDefinition> createCustomerDialogs() {
        List<DialogDefinition> dialogs = new ArrayList<>();
        
        dialogs.add(new DialogDefinition(
            "CustomerListDialog",
            "Customer Management",
            "Main window displaying a searchable list of customers with columns: ID, Name, Email, Phone, Status. " +
            "Includes toolbar with buttons: Add New, Edit, Delete, Refresh, Export to Excel. " +
            "Double-click on a row opens the customer details dialog. " +
            "Search box at top filters the list in real-time.",
            false,
            FormLayoutGenerator.createListDialog("CustomerList", "Customers", 
                new String[]{"ID", "Name", "Email", "Phone", "Status"}, 800, 600),
            true
        ));
        
        dialogs.add(new DialogDefinition(
            "CustomerDetailsDialog",
            "Customer Details",
            "Form for viewing/editing customer information. Fields: Customer ID (read-only), First Name*, Last Name*, " +
            "Email*, Phone, Address, City, State, Zip Code, Country dropdown, Customer Type (Individual/Business radio buttons), " +
            "Credit Limit numeric field, Status dropdown (Active/Inactive/Suspended), Notes multi-line text. " +
            "Buttons: Save, Save & Close, Cancel. * = required fields with validation.",
            true,
            FormLayoutGenerator.createDetailFormDialog("CustomerDetails", 
                new String[]{"Customer ID", "First Name", "Last Name", "Email", "Phone", 
                            "Address", "City", "State", "Zip Code", "Country"}, 600, 500),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "CustomerSearchDialog",
            "Advanced Customer Search",
            "Advanced search form with filters: Name contains, Email contains, Phone contains, " +
            "Status multi-select, Customer Type checkboxes, Credit Limit range (min/max), " +
            "Registration Date range (from/to date pickers). Buttons: Search, Clear Filters, Export Results.",
            true,
            FormLayoutGenerator.createSearchDialog("CustomerSearch", 
                new String[]{"Name", "Email", "Phone", "Status", "Customer Type", "Min Credit Limit", "Max Credit Limit"}, 500, 400),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "CustomerHistoryDialog",
            "Customer Order History",
            "Shows customer's order history in a grid: Order ID, Date, Total Amount, Status, Payment Method. " +
            "Includes summary panel showing: Total Orders, Total Revenue, Average Order Value, Last Order Date. " +
            "Double-click to view order details.",
            false,
            FormLayoutGenerator.createListDialog("CustomerHistory", "Order History",
                new String[]{"Order ID", "Date", "Total Amount", "Status", "Payment Method"}, 700, 500),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "CustomerImportDialog",
            "Import Customers from File",
            "Dialog for bulk importing customers. File selector for CSV/Excel files, " +
            "Preview grid showing first 10 rows, Column mapping section to map file columns to customer fields, " +
            "Options: Skip duplicates checkbox, Update existing checkbox, Validation rules dropdown. " +
            "Progress bar during import. Buttons: Import, Cancel, Download Template.",
            true,
            FormLayoutGenerator.createDetailFormDialog("CustomerImport",
                new String[]{"File Path", "File Type", "Skip Duplicates", "Update Existing", "Validation Rules"}, 600, 400),
            false
        ));
        
        return dialogs;
    }
    
    /**
     * Creates order management dialogs.
     */
    public static List<DialogDefinition> createOrderDialogs() {
        List<DialogDefinition> dialogs = new ArrayList<>();
        
        dialogs.add(new DialogDefinition(
            "OrderListDialog",
            "Order Management",
            "Main order list with filters in sidebar. Grid columns: Order #, Customer Name, Date, Status, " +
            "Total Amount, Payment Status. Filter sidebar: Date Range picker, Status multi-select, " +
            "Payment Status checkboxes, Amount Range sliders. Toolbar: New Order, View Details, Cancel Order, " +
            "Print Invoice, Send Email. Status indicators with color coding.",
            false,
            FormLayoutGenerator.createListDialog("OrderList", "Orders",
                new String[]{"Order #", "Customer", "Date", "Status", "Total", "Payment"}, 900, 650),
            true
        ));
        
        dialogs.add(new DialogDefinition(
            "CreateOrderDialog",
            "Create New Order",
            "Multi-step order creation wizard. Step 1: Select Customer (dropdown with search). " +
            "Step 2: Add Products (product search, quantity spinners, price display, running total). " +
            "Step 3: Shipping Information (address form, shipping method dropdown, estimated delivery date). " +
            "Step 4: Payment (payment method radio buttons, payment details fields). " +
            "Step 5: Review & Submit (summary of all information). Navigation: Previous, Next, Submit, Cancel.",
            true,
            FormLayoutGenerator.createDetailFormDialog("CreateOrder",
                new String[]{"Customer", "Product", "Quantity", "Shipping Method", "Payment Method"}, 650, 450),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "OrderDetailsDialog",
            "Order Details & Tracking",
            "Detailed order view with tabs: Order Info, Line Items, Shipping, Payment, History. " +
            "Order Info tab: All order fields (read-only/editable based on status), Status change dropdown. " +
            "Line Items: Grid showing products, quantities, prices, discounts with inline editing. " +
            "Shipping: Tracking information, carrier details, delivery timeline. " +
            "Payment: Payment transactions, refunds, credits. History: Audit log of all changes.",
            false,
            FormLayoutGenerator.createDetailFormDialog("OrderDetails",
                new String[]{"Order Number", "Customer", "Order Date", "Status", "Total Amount", "Payment Status"}, 700, 550),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "OrderInvoiceDialog",
            "Invoice Generator",
            "Invoice preview and generation. Company header section, Customer billing info, " +
            "Itemized list of products/services, Subtotal/Tax/Shipping/Total calculations, " +
            "Payment terms, Due date, Notes section. Actions: Print, Email, Save as PDF, " +
            "Mark as Paid, Record Payment.",
            true,
            FormLayoutGenerator.createDetailFormDialog("OrderInvoice",
                new String[]{"Invoice Number", "Customer", "Billing Address", "Subtotal", "Tax", "Shipping", "Total"}, 650, 500),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "OrderStatusDialog",
            "Bulk Status Update",
            "For updating multiple order statuses. Selected orders list (checkboxes), " +
            "New status dropdown, Reason text area, Notify customer checkbox, " +
            "Email template selector. Preview affected orders count. Buttons: Apply, Cancel.",
            true,
            FormLayoutGenerator.createDetailFormDialog("BulkStatusUpdate",
                new String[]{"New Status", "Reason", "Notify Customer", "Email Template"}, 500, 350),
            false
        ));
        
        return dialogs;
    }
    
    /**
     * Creates inventory management dialogs.
     */
    public static List<DialogDefinition> createInventoryDialogs() {
        List<DialogDefinition> dialogs = new ArrayList<>();
        
        dialogs.add(new DialogDefinition(
            "InventoryListDialog",
            "Inventory Overview",
            "Main inventory dashboard with product grid: SKU, Product Name, Category, Quantity on Hand, " +
            "Reorder Level, Supplier, Unit Price, Total Value. Visual indicators for low stock (red), " +
            "adequate stock (green). Summary cards at top: Total Products, Total Value, Low Stock Items, " +
            "Out of Stock. Toolbar: Add Product, Edit, Delete, Stock Adjustment, Generate Report.",
            false,
            FormLayoutGenerator.createListDialog("InventoryList", "Products",
                new String[]{"SKU", "Product Name", "Category", "Qty on Hand", "Reorder Level", "Price", "Value"}, 950, 700),
            true
        ));
        
        dialogs.add(new DialogDefinition(
            "ProductDetailsDialog",
            "Product Management",
            "Complete product form. Basic Info: SKU*, Name*, Description, Category dropdown, " +
            "Brand, Model. Pricing: Cost Price, Sale Price, MSRP, Tax Category. " +
            "Inventory: Current Stock, Reorder Level, Reorder Quantity, Location, Bin #. " +
            "Supplier: Supplier dropdown, Supplier SKU, Lead Time. " +
            "Images: Image upload area with preview. Buttons: Save, Duplicate, Delete, Cancel.",
            true,
            FormLayoutGenerator.createDetailFormDialog("ProductDetails",
                new String[]{"SKU", "Product Name", "Description", "Category", "Brand", 
                            "Cost Price", "Sale Price", "Current Stock", "Reorder Level", "Supplier"}, 650, 550),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "StockAdjustmentDialog",
            "Stock Adjustment",
            "For adjusting inventory levels. Product selector (with search), Current Stock (read-only), " +
            "Adjustment Type dropdown (Receive/Issue/Count/Damage/Return), Quantity numeric field, " +
            "New Stock Level (calculated), Reason dropdown, Notes text area, Reference # field. " +
            "Requires authorization for large adjustments. Buttons: Apply, Apply & Print, Cancel.",
            true,
            FormLayoutGenerator.createDetailFormDialog("StockAdjustment",
                new String[]{"Product", "Current Stock", "Adjustment Type", "Quantity", "New Stock Level", "Reason", "Notes"}, 550, 450),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "InventoryReportDialog",
            "Inventory Reports",
            "Report generation dialog. Report Type tabs: Stock Value, Movement, Low Stock, Aging Analysis. " +
            "Each tab has specific filters and parameters. Date range selectors, Category filters, " +
            "Location filters, Export format selection (PDF/Excel/CSV). " +
            "Report preview pane. Buttons: Generate, Export, Schedule, Print.",
            false,
            FormLayoutGenerator.createDetailFormDialog("InventoryReport",
                new String[]{"Report Type", "Date From", "Date To", "Category", "Location", "Export Format"}, 600, 400),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "BarcodeDialog",
            "Barcode Scanner & Lookup",
            "Barcode scanning interface. Large barcode entry field with auto-submit, " +
            "Recent scans list, Product quick-view panel (shows product details when scanned), " +
            "Quick actions: Add to Order, Check Stock, Adjust Quantity, Print Label. " +
            "Supports manual entry and USB scanner input.",
            false,
            FormLayoutGenerator.createDetailFormDialog("BarcodeScanner",
                new String[]{"Barcode", "Product Name", "Current Stock", "Quick Action"}, 500, 350),
            false
        ));
        
        return dialogs;
    }
    
    /**
     * Creates reporting dialogs.
     */
    public static List<DialogDefinition> createReportingDialogs() {
        List<DialogDefinition> dialogs = new ArrayList<>();
        
        dialogs.add(new DialogDefinition(
            "DashboardDialog",
            "Business Analytics Dashboard",
            "Main analytics dashboard with widgets: Sales chart (line/bar toggle), Revenue by category (pie chart), " +
            "Top customers table, Top products table, Recent orders timeline. KPI cards: Today's Sales, " +
            "Monthly Revenue, Active Customers, Pending Orders. Date range selector, Refresh button, " +
            "Auto-refresh toggle. Widget customization: drag to reorder, resize, show/hide.",
            false,
            FormLayoutGenerator.createDashboardDialog("Dashboard", 1000, 750),
            true
        ));
        
        dialogs.add(new DialogDefinition(
            "SalesReportDialog",
            "Sales Analytics",
            "Detailed sales reporting. Filter panel: Date Range, Customer, Product Category, Sales Rep, " +
            "Payment Method. Report display with tabs: Summary, By Product, By Customer, By Date, By Region. " +
            "Charts: Sales trend line, Category breakdown pie, Top performers bar chart. " +
            "Export options: Excel, PDF, Email. Schedule recurring reports.",
            false,
            FormLayoutGenerator.createDetailFormDialog("SalesReport",
                new String[]{"Date From", "Date To", "Customer", "Category", "Sales Rep", "Payment Method"}, 700, 500),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "CustomerAnalyticsDialog",
            "Customer Insights",
            "Customer behavior analytics. Segments: New Customers, Repeat Customers, At-Risk, VIP. " +
            "Metrics: Customer Lifetime Value, Purchase Frequency, Average Order Value, Retention Rate. " +
            "Cohort analysis grid, RFM analysis (Recency, Frequency, Monetary). " +
            "Customer segment comparison charts. Export segment lists for marketing.",
            true,
            FormLayoutGenerator.createDetailFormDialog("CustomerAnalytics",
                new String[]{"Segment", "Date Range", "Metric", "Comparison Period"}, 600, 400),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "FinancialReportDialog",
            "Financial Reports",
            "Financial reporting suite. Report types: Income Statement, Balance Sheet, Cash Flow, " +
            "Tax Report, Profit & Loss. Period selectors: Monthly, Quarterly, Yearly, Custom Range. " +
            "Comparison view: Compare periods, Show variance, Percentage change. " +
            "Drill-down capability: Click values to see transactions. Export for accounting software.",
            true,
            FormLayoutGenerator.createDetailFormDialog("FinancialReport",
                new String[]{"Report Type", "Period", "Start Date", "End Date", "Comparison", "Format"}, 650, 450),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "CustomReportDialog",
            "Custom Report Builder",
            "Drag-and-drop report builder. Data source selector (tables/views), " +
            "Available fields list (drag to report), Report canvas (drop zones for columns/filters/sorts), " +
            "Filter builder (visual query builder), Formatting options: Fonts, Colors, Logos, Headers/Footers. " +
            "Preview tab, Save as template option. Buttons: Run Report, Save Template, Export, Schedule.",
            true,
            FormLayoutGenerator.createDetailFormDialog("CustomReport",
                new String[]{"Data Source", "Fields", "Filters", "Sort By", "Format Options"}, 700, 550),
            false
        ));
        
        return dialogs;
    }
    
    /**
     * Creates administration dialogs.
     */
    public static List<DialogDefinition> createAdminDialogs() {
        List<DialogDefinition> dialogs = new ArrayList<>();
        
        dialogs.add(new DialogDefinition(
            "UserManagementDialog",
            "User & Role Management",
            "User administration interface. User list grid: Username, Full Name, Email, Role, Status, Last Login. " +
            "Sidebar for quick filters: By Role, By Status, Search. Toolbar: Add User, Edit, Deactivate, " +
            "Reset Password, Send Invitation. Role management section: Create/Edit roles, " +
            "Permission matrix (checkboxes for module/action combinations).",
            false,
            FormLayoutGenerator.createListDialog("UserManagement", "Users",
                new String[]{"Username", "Full Name", "Email", "Role", "Status", "Last Login"}, 900, 650),
            true
        ));
        
        dialogs.add(new DialogDefinition(
            "UserDetailsDialog",
            "User Profile Editor",
            "User account management form. Personal Info: Username*, Full Name*, Email*, Phone, Department. " +
            "Account Settings: Role dropdown, Status (Active/Inactive), Password change section, " +
            "Two-Factor Authentication toggle. Permissions: Inherited from role (read-only list), " +
            "Additional permissions (checkboxes), Permission exceptions (override role permissions). " +
            "Activity: Last Login, Login History button, Audit Log button.",
            true,
            FormLayoutGenerator.createDetailFormDialog("UserDetails",
                new String[]{"Username", "Full Name", "Email", "Phone", "Department", "Role", "Status", "Password"}, 600, 500),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "SystemSettingsDialog",
            "System Configuration",
            "Application settings with category tabs: General, Email, Payment, Security, Database, Logging. " +
            "General: Company Name, Logo upload, Time Zone, Date Format, Currency. " +
            "Email: SMTP settings, Email templates. Payment: Payment gateway configuration, API keys. " +
            "Security: Password policy, Session timeout, IP whitelist. " +
            "Database: Backup schedule, Maintenance window. Each setting has help tooltip.",
            false,
            FormLayoutGenerator.createDetailFormDialog("SystemSettings",
                new String[]{"Company Name", "Time Zone", "Date Format", "Currency", "SMTP Server", "Session Timeout"}, 650, 500),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "AuditLogDialog",
            "System Audit Log",
            "Comprehensive audit log viewer. Filter panel: Date Range, User, Module, Action Type, " +
            "Record ID, IP Address. Log grid: Timestamp, User, Action, Module, Details, IP Address, Status. " +
            "Detail view: When row selected, shows full before/after values for changes. " +
            "Export options: CSV, JSON, Send to SIEM. Archive old logs option.",
            false,
            FormLayoutGenerator.createListDialog("AuditLog", "Audit Logs",
                new String[]{"Timestamp", "User", "Action", "Module", "Details", "IP Address", "Status"}, 950, 650),
            false
        ));
        
        dialogs.add(new DialogDefinition(
            "BackupRestoreDialog",
            "Backup & Restore",
            "Database backup and restore interface. Backup section: Backup type (Full/Incremental), " +
            "Include files checkbox, Compression level, Destination (local/cloud), Schedule recurring backups. " +
            "Restore section: Available backups list with dates/sizes, Restore point selector, " +
            "Restore options (data/files/settings), Confirmation with warnings. " +
            "Backup history log. Buttons: Backup Now, Restore, Schedule, Verify Backup.",
            true,
            FormLayoutGenerator.createDetailFormDialog("BackupRestore",
                new String[]{"Backup Type", "Include Files", "Compression", "Destination", "Schedule"}, 600, 450),
            false
        ));
        
        return dialogs;
    }
}
