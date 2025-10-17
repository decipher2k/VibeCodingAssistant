/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.util.ArrayList;
import java.util.List;

public final class TestSuite {
    private final List<Entry> tests = new ArrayList<>();

    public TestSuite add(String name, TestCase testCase) {
        tests.add(new Entry(name, testCase));
        return this;
    }

    public int runAll() {
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        List<String> failures = new ArrayList<>();
        List<String> skips = new ArrayList<>();

        for (Entry entry : tests) {
            try {
                entry.testCase.run(new TestContext(entry.name));
                passed++;
                System.out.println("[PASS] " + entry.name);
            } catch (SkippedTestException skippedEx) {
                skipped++;
                skips.add(skippedEx.getMessage());
                System.out.println("[SKIP] " + entry.name + " - " + skippedEx.getMessage());
            } catch (AssertionError | Exception error) {
                failed++;
                failures.add(entry.name + ": " + error.getMessage());
                System.out.println("[FAIL] " + entry.name + " - " + error.getMessage());
            }
        }

        System.out.println();
        System.out.println("Summary: " + passed + " passed, " + failed + " failed, " + skipped + " skipped");
        if (!failures.isEmpty()) {
            System.out.println("Failures:");
            failures.forEach(message -> System.out.println("  - " + message));
        }
        if (!skips.isEmpty()) {
            System.out.println("Skipped:");
            skips.forEach(message -> System.out.println("  - " + message));
        }

        return failed == 0 ? 0 : 1;
    }

    private record Entry(String name, TestCase testCase) {
    }
}
