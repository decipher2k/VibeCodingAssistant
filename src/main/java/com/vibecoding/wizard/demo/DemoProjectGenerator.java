/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.demo;

import com.vibecoding.wizard.IDEProject;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.Module;
import com.vibecoding.wizard.ProjectSerializer;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.ProgramMode;
import com.vibecoding.wizard.ProjectMode;
import com.vibecoding.wizard.ProjectSettings;
import com.vibecoding.wizard.WorkflowItem;
import com.vibecoding.wizard.WorkflowStep;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Arrays;

/**
 * Main program to generate a comprehensive demo VCP project file.
 * This creates an Enterprise Resource Planning (ERP) demo application with 5 modules.
 */
public class DemoProjectGenerator {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("VCA Demo Project Generator");
        System.out.println("=================================================");
        System.out.println();
        
        try {
            // Create the demo project
            IDEProject project = createDemoProject();
            
            // Determine output path
            Path outputPath;
            if (args.length > 0) {
                outputPath = Paths.get(args[0]);
            } else {
                outputPath = Paths.get("demo-enterprise-app.vcp");
            }
            
            System.out.println("Saving project to: " + outputPath.toAbsolutePath());
            
            // Save the project
            boolean success = ProjectSerializer.save(project, outputPath);
            
            if (success) {
                System.out.println();
                System.out.println("✓ Demo project created successfully!");
                System.out.println();
                printProjectSummary(project);
                System.out.println();
                System.out.println("You can now open this file in VCA IDE mode.");
            } else {
                System.err.println("✗ Failed to save project file.");
                System.exit(1);
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error creating demo project: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Creates the complete demo project with all modules.
     */
    private static IDEProject createDemoProject() {
        System.out.println("Creating project configuration...");
        
        // Create initial configuration for C# Windows GUI application
        InitialConfig config = new InitialConfig(
            ProgrammingLanguage.CSHARP,
            ProjectStyle.GUI,
            EnumSet.of(TargetOs.WINDOWS),
            Paths.get("C:\\Projects\\EnterpriseApp"),
            ProgramMode.MAIN_WINDOW,  // Main window mode
            "Enterprise Management System",
            ProjectMode.IDE   // IDE mode for multi-module support
        );
        
        // Create the project
        IDEProject project = new IDEProject(config);
        
        // Configure project settings
        ProjectSettings settings = project.getProjectSettings();
        settings.setProjectName("Enterprise Management System");
        settings.setProjectPath("C:\\Projects\\EnterpriseApp");
        
        // Add global variables
        System.out.println("Adding global variables...");
        settings.setGlobalVariables(VariableFactory.createGlobalVariables());
        
        // Add project-level workflows
        System.out.println("Adding project-level workflows...");
        settings.setProjectWorkflows(createProjectWorkflows());
        
        // Create and add modules
        System.out.println("Creating Customer Management module...");
        Module customerModule = CustomerModuleBuilder.build();
        project.addRootModule(customerModule);
        
        System.out.println("Creating Order Management module...");
        Module orderModule = OrderModuleBuilder.build();
        project.addRootModule(orderModule);
        
        System.out.println("Creating Inventory Management module...");
        Module inventoryModule = InventoryModuleBuilder.build();
        project.addRootModule(inventoryModule);
        
        System.out.println("Creating Reporting & Analytics module...");
        Module reportingModule = ReportingModuleBuilder.build();
        project.addRootModule(reportingModule);
        
        System.out.println("Creating Administration module...");
        Module adminModule = AdministrationModuleBuilder.build();
        project.addRootModule(adminModule);
        
        // Set the main module (will launch with MDI interface)
        project.setMainModule(adminModule);
        
        System.out.println("Project assembly complete.");
        
        return project;
    }
    
    /**
     * Creates project-level workflows that span multiple modules.
     */
    private static java.util.List<WorkflowItem> createProjectWorkflows() {
        java.util.List<WorkflowItem> workflows = new java.util.ArrayList<>();
        
        workflows.add(new WorkflowItem(
            "CompleteOrderFulfillment",
            "Cross-Module",
            "End-to-end order processing workflow",
            Arrays.asList(
                new WorkflowStep("Customer Management: Verify customer exists or create new customer", "", false),
                new WorkflowStep("Order Management: Create new order and select products", "", false),
                new WorkflowStep("Inventory Management: Check product availability", "Products must be in stock", true),
                new WorkflowStep("Inventory Management: Reserve stock for order", "", false),
                new WorkflowStep("Order Management: Process payment", "Payment must be successful", true),
                new WorkflowStep("Inventory Management: Reduce stock quantities", "", false),
                new WorkflowStep("Order Management: Update order status to 'Confirmed'", "", false),
                new WorkflowStep("Order Management: Generate and send invoice to customer", "", false),
                new WorkflowStep("Administration: Log all transactions in audit trail", "", false),
                new WorkflowStep("Reporting: Update real-time dashboard metrics", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "ApplicationStartup",
            "System",
            "Application initialization sequence",
            Arrays.asList(
                new WorkflowStep("Display login screen", "", false),
                new WorkflowStep("Administration: Authenticate user credentials", "Valid username and password required", true),
                new WorkflowStep("Administration: Load user permissions", "", false),
                new WorkflowStep("Administration: Check session limits", "User must not exceed concurrent session limit", true),
                new WorkflowStep("Administration: Create new session record", "", false),
                new WorkflowStep("Load MDI main window with module menu", "", false),
                new WorkflowStep("Administration: Log user login event", "", false),
                new WorkflowStep("Reporting: Load dashboard if user has permission", "", false)
            )
        ));
        
        workflows.add(new WorkflowItem(
            "MonthEndProcessing",
            "Batch",
            "Automated month-end processing",
            Arrays.asList(
                new WorkflowStep("Reporting: Generate monthly sales report", "", false),
                new WorkflowStep("Reporting: Generate monthly financial report", "", false),
                new WorkflowStep("Customer Management: Update customer lifetime value calculations", "", false),
                new WorkflowStep("Inventory Management: Perform stock value calculation", "", false),
                new WorkflowStep("Inventory Management: Generate aging analysis report", "", false),
                new WorkflowStep("Order Management: Archive completed orders older than 90 days", "", false),
                new WorkflowStep("Administration: Archive old audit log entries", "", false),
                new WorkflowStep("Administration: Perform database backup", "", false),
                new WorkflowStep("Administration: Email reports to management", "", false)
            )
        ));
        
        return workflows;
    }
    
    /**
     * Prints a summary of the created project.
     */
    private static void printProjectSummary(IDEProject project) {
        System.out.println("=================================================");
        System.out.println("PROJECT SUMMARY");
        System.out.println("=================================================");
        System.out.println();
        System.out.println("Project Name: " + project.getProjectSettings().getProjectName());
        System.out.println("Language: C#");
        System.out.println("Style: Windows GUI Application");
        System.out.println("Mode: Main Window");
        System.out.println("Target OS: Windows");
        System.out.println();
        System.out.println("Global Variables: " + project.getProjectSettings().getGlobalVariables().size());
        System.out.println("Project Workflows: " + project.getProjectSettings().getProjectWorkflows().size());
        System.out.println();
        System.out.println("MODULES (" + project.getRootModules().size() + "):");
        System.out.println("-------------------------------------------------");
        
        int totalDialogs = 0;
        int totalWorkflows = 0;
        int totalVariables = 0;
        
        for (Module module : project.getRootModules()) {
            int dialogCount = module.getTaskData().getDialogs().size();
            int workflowCount = module.getTaskData().getWorkflowItems().size();
            int variableCount = module.getModuleVariables().size();
            
            totalDialogs += dialogCount;
            totalWorkflows += workflowCount;
            totalVariables += variableCount;
            
            System.out.println();
            System.out.println("• " + module.getName());
            System.out.println("  Main Window: " + module.getTaskData().getMainWindowName());
            System.out.println("  Dialogs: " + dialogCount);
            System.out.println("  Workflows: " + workflowCount);
            System.out.println("  Module Variables: " + variableCount);
        }
        
        System.out.println();
        System.out.println("=================================================");
        System.out.println("TOTALS:");
        System.out.println("  Total Dialogs: " + totalDialogs);
        System.out.println("  Total Workflows: " + totalWorkflows + " (module) + " + 
                          project.getProjectSettings().getProjectWorkflows().size() + " (project)");
        System.out.println("  Total Module Variables: " + totalVariables);
        System.out.println("  Global Variables: " + project.getProjectSettings().getGlobalVariables().size());
        System.out.println("=================================================");
    }
}
