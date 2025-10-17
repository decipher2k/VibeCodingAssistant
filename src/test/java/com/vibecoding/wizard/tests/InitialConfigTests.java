/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.util.EnumSet;

import com.vibecoding.wizard.InitialConfig;
import com.vibecoding.wizard.ProgrammingLanguage;
import com.vibecoding.wizard.ProjectStyle;
import com.vibecoding.wizard.TargetOs;

public final class InitialConfigTests {
    private InitialConfigTests() {
    }

    public static void run(TestContext ctx) {
        EnumSet<TargetOs> targets = EnumSet.of(TargetOs.LINUX, TargetOs.MACOS);
        InitialConfig config = new InitialConfig(ProgrammingLanguage.JAVA, ProjectStyle.GUI, targets, null);

        ctx.assertEquals("Programming language", ProgrammingLanguage.JAVA, config.getProgrammingLanguage());
        ctx.assertEquals("Project style", ProjectStyle.GUI, config.getProjectStyle());
        EnumSet<TargetOs> copy = config.getTargetOperatingSystems();
        ctx.assertTrue("Copy contains Linux", copy.contains(TargetOs.LINUX));
        ctx.assertTrue("Copy contains macOS", copy.contains(TargetOs.MACOS));

        targets.clear();
        ctx.assertTrue("Original set cleared", targets.isEmpty());
        ctx.assertFalse("Config retains targets after original cleared", config.getTargetOperatingSystems().isEmpty());
    }
}
