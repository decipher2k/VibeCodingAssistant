/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

public final class TestRunner {
    private TestRunner() {
    }

    public static TestSuite createSuite() {
        return new TestSuite()
            // Enum tests
            .add("EnumTests", EnumTests::run)
            // Data model tests
            .add("DialogDefinitionTests", DialogDefinitionTests::run)
            .add("InitialConfigTests", InitialConfigTests::run)
            .add("MainTaskDataTests", MainTaskDataTests::run)
            .add("ModuleTests", ModuleTests::run)
            .add("WorkflowItemTests", WorkflowItemTests::run)
            .add("WorkflowStepTests", WorkflowStepTests::run)
            // Process tests
            .add("ProcessTests", ProcessTests::run)
            .add("ProcessAndResultTests", ProcessAndResultTests::run)
            // Business logic tests
            .add("PromptBuilderTests", PromptBuilderTests::run)
            .add("DatabaseSupportTests", DatabaseSupportTests::run)
            .add("BuildCommandPlannerTests", BuildCommandPlannerTests::run)
            .add("CopilotCliServiceTests", CopilotCliServiceTests::run)
            .add("PackageManagerDetectorTests", PackageManagerDetectorTests::run)
            .add("MultiPhaseBuildExecutorTests", MultiPhaseBuildExecutorTests::run)
            // Project management tests
            .add("ProjectTemplateTests", ProjectTemplateTests::run)
            .add("IDEProjectTests", IDEProjectTests::run)
            .add("ProjectSerializerTests", ProjectSerializerTests::run)
            .add("TemplateManagerTests", TemplateManagerTests::run)
            // Controller tests
            .add("WizardControllerTests", WizardControllerTests::run)
            .add("IDEControllerTests", IDEControllerTests::run)
            // UI component tests
            .add("UiComponentTests", UiComponentTests::run)
            .add("DatabaseFileSectionTests", DatabaseFileSectionTests::run)
            .add("ThemeManagerTests", ThemeManagerTests::run)
            .add("FormLayoutBuilderTests", FormLayoutBuilderTests::run)
            .add("DialogListSectionTests", DialogListSectionTests::run)
            // UI panel tests
            .add("InitialPanelTests", InitialPanelTests::run)
            .add("TaskSelectionPanelTests", TaskSelectionPanelTests::run)
            .add("MainDialogPanelTests", MainDialogPanelTests::run)
            .add("ModuleEditorPanelTests", ModuleEditorPanelTests::run)
            .add("ModuleTreePanelTests", ModuleTreePanelTests::run)
            // UI dialog tests
            .add("DialogDefinitionEditorDialogTests", DialogDefinitionEditorDialogTests::run)
            .add("TaskExecutionDialogTests", TaskExecutionDialogTests::run)
            .add("CompilationErrorDialogTests", CompilationErrorDialogTests::run)
            .add("AlgorithmResultDialogTests", AlgorithmResultDialogTests::run)
            // Frame tests
            .add("WizardFrameTests", WizardFrameTests::run)
            .add("IDEMainFrameTests", IDEMainFrameTests::run)
            // Integration tests
            .add("FormEditorLauncherTests", FormEditorLauncherTests::run)
            .add("StartupCheckerTests", StartupCheckerTests::run)
            .add("WizardAppHeadlessTests", WizardAppHeadlessTests::run);
    }

    public static void main(String[] args) {
        int exitCode = createSuite().runAll();
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
