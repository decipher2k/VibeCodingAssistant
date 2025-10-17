/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.MainTaskData;
import com.vibecoding.wizard.TaskType;

/**
 * Builder for the Reporting & Analytics module.
 */
public class ReportingModuleBuilder {
    
    public static Module build() {
        Module module = new Module("ReportingAnalytics", TaskType.CREATE_MODULE);
        
        MainTaskData taskData = new MainTaskData();
        
        taskData.setProjectOverview(
            "Reporting & Analytics Module\n\n" +
            "This module provides comprehensive business intelligence and reporting capabilities. " +
            "It aggregates data from all other modules to deliver insights through interactive dashboards, " +
            "detailed reports, and data visualizations. The module supports both predefined and custom reports " +
            "with flexible filtering, export options, and scheduling capabilities.\n\n" +
            "Key Features:\n" +
            "- Interactive business dashboard with real-time KPIs\n" +
            "- Sales analytics with trend analysis\n" +
            "- Customer behavior insights and segmentation\n" +
            "- Financial reporting (P&L, Balance Sheet, Cash Flow)\n" +
            "- Custom report builder with drag-and-drop interface\n" +
            "- Multiple chart types (line, bar, pie, scatter)\n" +
            "- Date range filtering and comparison\n" +
            "- Export to PDF, Excel, and CSV\n" +
            "- Scheduled report generation and email delivery\n" +
            "- Drill-down capability for detailed analysis\n" +
            "- Cohort analysis and RFM segmentation"
        );
        
        taskData.setThemeDescription(
            "Data-focused design with emphasis on visualization clarity. Dashboard uses card-based layout " +
            "with white cards on light gray background. Charts use a consistent color palette: " +
            "Blue, Green, Orange, Purple, Red. KPI cards show large numbers with trend indicators " +
            "(up/down arrows in green/red). Use Chart.js or similar for responsive charts. " +
            "Tables in reports use zebra striping with hover highlighting. Export buttons use " +
            "file type icons (PDF, Excel, CSV). Filter panels collapsible to maximize report space. " +
            "Date pickers use calendar widgets with range selection."
        );
        
        // Set dialogs
        taskData.setDialogs(DialogFactory.createReportingDialogs());
        taskData.setMainWindowName("DashboardDialog");
        
        // Set workflows
        taskData.setWorkflowItems(WorkflowFactory.createReportingWorkflows());
        
        module.setTaskData(taskData);
        module.setModuleVariables(VariableFactory.createReportingModuleVariables());
        
        return module;
    }
}
