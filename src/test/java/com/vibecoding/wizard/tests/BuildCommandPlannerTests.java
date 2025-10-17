/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.util.EnumSet;
import java.util.List;

import com.vibecoding.wizard.BuildCommandPlanner;
import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;

public final class BuildCommandPlannerTests {
    private BuildCommandPlannerTests() {
    }

    public static void run(TestContext ctx) {
        // Test with null config (backward compatibility)
        BuildCommandPlanner.BuildPlan csharp = BuildCommandPlanner.plan(ProgrammingLanguage.CSHARP, ProjectStyle.GUI, null);
        ctx.assertEquals("C# command without config", List.of("dotnet", "build"), csharp.getCommands().get(0));

        // Test C# with Linux target (no Wine needed)
        InitialConfig linuxConfig = new InitialConfig(ProgrammingLanguage.CSHARP, ProjectStyle.GUI, 
            EnumSet.of(TargetOs.LINUX), null);
        BuildCommandPlanner.BuildPlan csharpLinux = BuildCommandPlanner.plan(ProgrammingLanguage.CSHARP, ProjectStyle.GUI, linuxConfig);
        ctx.assertEquals("C# command for Linux target", List.of("dotnet", "build"), csharpLinux.getCommands().get(0));

        // Test C# with Windows target on non-Windows OS (Wine should be used if not on Windows)
        InitialConfig windowsConfig = new InitialConfig(ProgrammingLanguage.CSHARP, ProjectStyle.GUI, 
            EnumSet.of(TargetOs.WINDOWS), null);
        BuildCommandPlanner.BuildPlan csharpWindows = BuildCommandPlanner.plan(ProgrammingLanguage.CSHARP, ProjectStyle.GUI, windowsConfig);
        String currentOs = System.getProperty("os.name").toLowerCase();
        if (!currentOs.contains("win")) {
            ctx.assertEquals("C# command for Windows target on non-Windows", List.of("wine", "dotnet", "build"), csharpWindows.getCommands().get(0));
        } else {
            ctx.assertEquals("C# command for Windows target on Windows", List.of("dotnet", "build"), csharpWindows.getCommands().get(0));
        }

        BuildCommandPlanner.BuildPlan cpp = BuildCommandPlanner.plan(ProgrammingLanguage.CPP, ProjectStyle.GUI, null);
        ctx.assertEquals("C++ command", List.of("cmake", "--build", "build"), cpp.getCommands().get(0));

        BuildCommandPlanner.BuildPlan java = BuildCommandPlanner.plan(ProgrammingLanguage.JAVA, ProjectStyle.GUI, null);
        ctx.assertEquals("Java command", List.of("mvn", "package"), java.getCommands().get(0));

        BuildCommandPlanner.BuildPlan python = BuildCommandPlanner.plan(ProgrammingLanguage.PYTHON, ProjectStyle.SCRIPT, null);
        ctx.assertEquals("Python command", List.of("python", "-m", "compileall", "."), python.getCommands().get(0));

        BuildCommandPlanner.BuildPlan php = BuildCommandPlanner.plan(ProgrammingLanguage.PHP, ProjectStyle.WEB, null);
        ctx.assertEquals("PHP command", List.of("php", "-l"), php.getCommands().get(0));

        BuildCommandPlanner.BuildPlan rust = BuildCommandPlanner.plan(ProgrammingLanguage.RUST, ProjectStyle.GUI, null);
        ctx.assertEquals("Rust command", List.of("cargo", "build"), rust.getCommands().get(0));

        BuildCommandPlanner.BuildPlan go = BuildCommandPlanner.plan(ProgrammingLanguage.GO, ProjectStyle.GUI, null);
        ctx.assertEquals("Go command", List.of("go", "build"), go.getCommands().get(0));

        BuildCommandPlanner.BuildPlan javascript = BuildCommandPlanner.plan(ProgrammingLanguage.JAVASCRIPT, ProjectStyle.WEB, null);
        ctx.assertEquals("JavaScript command", List.of("npm", "run", "build"), javascript.getCommands().get(0));

        BuildCommandPlanner.BuildPlan ruby = BuildCommandPlanner.plan(ProgrammingLanguage.RUBY, ProjectStyle.WEB, null);
        ctx.assertEquals("Ruby command", List.of("bundle", "exec", "rake", "build"), ruby.getCommands().get(0));
    }
}
