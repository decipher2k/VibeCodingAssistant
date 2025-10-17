/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;
import com.vibecoding.wizard.TaskType;

public final class EnumTests {
    private EnumTests() {
    }

    public static void run(TestContext ctx) {
        ctx.assertEquals("ProgrammingLanguage C#", "C# (DotNET 9.0)", ProgrammingLanguage.CSHARP.toString());
        ctx.assertEquals("ProgrammingLanguage C++", "C++", ProgrammingLanguage.CPP.toString());
        ctx.assertEquals("ProgrammingLanguage Java", "Java", ProgrammingLanguage.JAVA.toString());
        ctx.assertEquals("ProgrammingLanguage Python", "Python", ProgrammingLanguage.PYTHON.toString());
        ctx.assertEquals("ProgrammingLanguage PHP", "PHP", ProgrammingLanguage.PHP.toString());
        ctx.assertEquals("ProgrammingLanguage Rust", "Rust", ProgrammingLanguage.RUST.toString());
        ctx.assertEquals("ProgrammingLanguage Go", "Go", ProgrammingLanguage.GO.toString());
        ctx.assertEquals("ProgrammingLanguage JavaScript", "JavaScript", ProgrammingLanguage.JAVASCRIPT.toString());
        ctx.assertEquals("ProgrammingLanguage Ruby", "Ruby", ProgrammingLanguage.RUBY.toString());

        ctx.assertEquals("ProjectStyle WEB", "Web", ProjectStyle.WEB.toString());
        ctx.assertEquals("ProjectStyle GUI", "GUI", ProjectStyle.GUI.toString());
        ctx.assertEquals("ProjectStyle SCRIPT", "Script", ProjectStyle.SCRIPT.toString());

        ctx.assertEquals("TargetOs Windows", "Windows", TargetOs.WINDOWS.toString());
        ctx.assertEquals("TargetOs Linux", "Linux", TargetOs.LINUX.toString());
        ctx.assertEquals("TargetOs macOS", "macOS", TargetOs.MACOS.toString());

        ctx.assertEquals("TaskType Generate", "Generate or modify existing VCA app", TaskType.GENERATE_APP_OR_SCRIPT.toString());
        ctx.assertEquals("TaskType Fix", "Fix coding errors", TaskType.FIX_CODING_ERRORS.toString());
        ctx.assertEquals("TaskType Module", "Create module", TaskType.CREATE_MODULE.toString());
        ctx.assertEquals("TaskType Algo", "Create algorithm", TaskType.CREATE_ALGORITHM.toString());
        ctx.assertEquals("TaskType Modify", "Modify existing, unknown software", TaskType.MODIFY_EXISTING_SOFTWARE.toString());
    }
}
