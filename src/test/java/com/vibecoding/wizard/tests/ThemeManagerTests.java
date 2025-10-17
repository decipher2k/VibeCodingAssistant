/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.ThemeManager;

public final class ThemeManagerTests {
    private ThemeManagerTests() {
    }

    public static void run(TestContext ctx) {
        ThemeManager.apply();
        ctx.assertTrue("ThemeManager.apply completed", true);
    }
}
