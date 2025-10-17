/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import com.vibecoding.wizard.PackageManagerDetector;

public final class PackageManagerDetectorTests {
    private PackageManagerDetectorTests() {
    }

    public static void run(TestContext ctx) throws Exception {
        testDetectorExists(ctx);
        testDetect(ctx);
        testPackageManagerClass(ctx);
    }

    private static void testDetectorExists(TestContext ctx) {
        try {
            Class<?> clazz = Class.forName("com.vibecoding.wizard.PackageManagerDetector");
            ctx.assertNotNull("PackageManagerDetector class exists", clazz);
        } catch (ClassNotFoundException e) {
            ctx.fail("PackageManagerDetector class not found: " + e.getMessage());
        }
    }

    private static void testDetect(TestContext ctx) {
        try {
            // Try to detect package manager (may be null on some systems)
            PackageManagerDetector.PackageManager pm = PackageManagerDetector.detect();
            
            if (pm != null) {
                ctx.assertNotNull("Package manager detected", pm);
                ctx.assertNotNull("Package manager has name", pm.getName());
                ctx.assertNotNull("Package manager has install command", pm.getInstallCommand());
                ctx.assertTrue("Install command not empty", !pm.getInstallCommand().isEmpty());
            } else {
                // No package manager detected is OK (e.g., on Windows)
                ctx.assertTrue("No package manager detected (acceptable)", true);
            }
        } catch (Exception e) {
            ctx.fail("Detect test failed: " + e.getMessage());
        }
    }

    private static void testPackageManagerClass(TestContext ctx) {
        try {
            // Test creating a PackageManager instance
            PackageManagerDetector.PackageManager pm = 
                new PackageManagerDetector.PackageManager("apt", "apt install", true);
            
            ctx.assertEquals("Name is apt", "apt", pm.getName());
            ctx.assertEquals("Install command", "apt install", pm.getInstallCommand());
            ctx.assertTrue("Requires sudo", pm.requiresSudo());
            
            // Test with non-sudo manager
            PackageManagerDetector.PackageManager pm2 = 
                new PackageManagerDetector.PackageManager("brew", "brew install", false);
            ctx.assertFalse("Does not require sudo", pm2.requiresSudo());
            
        } catch (Exception e) {
            ctx.fail("PackageManager class test failed: " + e.getMessage());
        }
    }
}
