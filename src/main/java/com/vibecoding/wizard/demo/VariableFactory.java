/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.GlobalVariable;
import com.vibecoding.wizard.ModuleVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating variable definitions for demo project.
 */
public class VariableFactory {
    
    /**
     * Creates global variables for the entire project.
     */
    public static List<GlobalVariable> createGlobalVariables() {
        List<GlobalVariable> variables = new ArrayList<>();
        
        variables.add(new GlobalVariable(
            "COMPANY_NAME",
            "Enterprise Solutions Inc."
        ));
        
        variables.add(new GlobalVariable(
            "APP_VERSION",
            "2.5.0"
        ));
        
        variables.add(new GlobalVariable(
            "DATABASE_CONNECTION",
            "Server=localhost;Database=EnterpriseDB;Trusted_Connection=True;"
        ));
        
        variables.add(new GlobalVariable(
            "API_BASE_URL",
            "https://api.enterprise-solutions.com/v2"
        ));
        
        variables.add(new GlobalVariable(
            "SUPPORT_EMAIL",
            "support@enterprise-solutions.com"
        ));
        
        variables.add(new GlobalVariable(
            "DEFAULT_CURRENCY",
            "USD"
        ));
        
        variables.add(new GlobalVariable(
            "TAX_RATE",
            "0.08"
        ));
        
        variables.add(new GlobalVariable(
            "MAX_UPLOAD_SIZE_MB",
            "50"
        ));
        
        return variables;
    }
    
    /**
     * Creates module variables for Customer Management module.
     */
    public static List<ModuleVariable> createCustomerModuleVariables() {
        List<ModuleVariable> variables = new ArrayList<>();
        
        variables.add(new ModuleVariable(
            "DEFAULT_CUSTOMER_STATUS",
            "Active"
        ));
        
        variables.add(new ModuleVariable(
            "CREDIT_LIMIT_THRESHOLD",
            "10000.00"
        ));
        
        variables.add(new ModuleVariable(
            "CUSTOMER_ID_PREFIX",
            "CUST-"
        ));
        
        variables.add(new ModuleVariable(
            "EMAIL_VALIDATION_PATTERN",
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        ));
        
        return variables;
    }
    
    /**
     * Creates module variables for Order Management module.
     */
    public static List<ModuleVariable> createOrderModuleVariables() {
        List<ModuleVariable> variables = new ArrayList<>();
        
        variables.add(new ModuleVariable(
            "ORDER_NUMBER_PREFIX",
            "ORD-"
        ));
        
        variables.add(new ModuleVariable(
            "SHIPPING_COST_RATE",
            "5.99"
        ));
        
        variables.add(new ModuleVariable(
            "FREE_SHIPPING_THRESHOLD",
            "100.00"
        ));
        
        variables.add(new ModuleVariable(
            "DEFAULT_PAYMENT_TERMS",
            "Net 30"
        ));
        
        variables.add(new ModuleVariable(
            "CANCELLATION_WINDOW_HOURS",
            "24"
        ));
        
        return variables;
    }
    
    /**
     * Creates module variables for Inventory Management module.
     */
    public static List<ModuleVariable> createInventoryModuleVariables() {
        List<ModuleVariable> variables = new ArrayList<>();
        
        variables.add(new ModuleVariable(
            "LOW_STOCK_THRESHOLD",
            "10"
        ));
        
        variables.add(new ModuleVariable(
            "REORDER_MULTIPLIER",
            "2.0"
        ));
        
        variables.add(new ModuleVariable(
            "BARCODE_FORMAT",
            "EAN-13"
        ));
        
        variables.add(new ModuleVariable(
            "INVENTORY_ADJUSTMENT_REQUIRES_APPROVAL",
            "true"
        ));
        
        variables.add(new ModuleVariable(
            "DEFAULT_WAREHOUSE_LOCATION",
            "WH-MAIN-01"
        ));
        
        return variables;
    }
    
    /**
     * Creates module variables for Reporting module.
     */
    public static List<ModuleVariable> createReportingModuleVariables() {
        List<ModuleVariable> variables = new ArrayList<>();
        
        variables.add(new ModuleVariable(
            "DASHBOARD_REFRESH_INTERVAL_SECONDS",
            "300"
        ));
        
        variables.add(new ModuleVariable(
            "REPORT_EXPORT_PATH",
            "C:\\Reports\\Exports"
        ));
        
        variables.add(new ModuleVariable(
            "MAX_REPORT_RECORDS",
            "100000"
        ));
        
        variables.add(new ModuleVariable(
            "CHART_COLOR_SCHEME",
            "Blue,Green,Orange,Red,Purple"
        ));
        
        return variables;
    }
    
    /**
     * Creates module variables for Administration module.
     */
    public static List<ModuleVariable> createAdminModuleVariables() {
        List<ModuleVariable> variables = new ArrayList<>();
        
        variables.add(new ModuleVariable(
            "PASSWORD_MIN_LENGTH",
            "8"
        ));
        
        variables.add(new ModuleVariable(
            "PASSWORD_REQUIRE_SPECIAL_CHAR",
            "true"
        ));
        
        variables.add(new ModuleVariable(
            "SESSION_TIMEOUT_MINUTES",
            "30"
        ));
        
        variables.add(new ModuleVariable(
            "MAX_LOGIN_ATTEMPTS",
            "5"
        ));
        
        variables.add(new ModuleVariable(
            "BACKUP_RETENTION_DAYS",
            "90"
        ));
        
        variables.add(new ModuleVariable(
            "AUDIT_LOG_RETENTION_DAYS",
            "365"
        ));
        
        return variables;
    }
}
