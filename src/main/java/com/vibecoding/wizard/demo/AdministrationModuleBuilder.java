/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.TaskType;

/**
 * Builder for the Administration module.
 */
public class AdministrationModuleBuilder {
    
    public static Module build() {
        Module module = new Module("Administration", TaskType.CREATE_MODULE);
        
        MainTaskData taskData = new MainTaskData();
        
        taskData.setProjectOverview(
            "Administration Module\n\n" +
            "This module provides system administration and configuration capabilities. " +
            "It handles user management, role-based access control, system settings, audit logging, " +
            "and backup/restore operations. The module ensures security through permission management " +
            "and provides tools for system maintenance and monitoring.\n\n" +
            "Key Features:\n" +
            "- User account management (create, edit, deactivate)\n" +
            "- Role-based permission system with granular access control\n" +
            "- System configuration (email, payment, security settings)\n" +
            "- Comprehensive audit log with filtering and search\n" +
            "- Database backup and restore functionality\n" +
            "- Password policy enforcement\n" +
            "- Two-factor authentication support\n" +
            "- Session management and timeout control\n" +
            "- IP whitelisting for enhanced security\n" +
            "- System health monitoring\n" +
            "- Email template management\n" +
            "- Automated backup scheduling"
        );
        
        taskData.setThemeDescription(
            "Professional administrative interface with emphasis on security and clarity. " +
            "Use shield icons for security-related features. Permission checkboxes organized in " +
            "matrix layout with module columns and action rows. User status badges: Green for Active, " +
            "Gray for Inactive. Audit log uses monospace font for technical data. " +
            "System settings organized in accordion or tab panels. Warning dialogs for destructive " +
            "actions (delete, deactivate) use yellow/orange accent. Backup section uses green for " +
            "successful backups, amber for warnings. Password strength meter shows visual feedback."
        );
        
        // Set dialogs
        taskData.setDialogs(DialogFactory.createAdminDialogs());
        taskData.setMainWindowName("UserManagementDialog");
        
        // Set workflows
        taskData.setWorkflowItems(WorkflowFactory.createAdminWorkflows());
        
        module.setTaskData(taskData);
        module.setModuleVariables(VariableFactory.createAdminModuleVariables());
        
        return module;
    }
}
