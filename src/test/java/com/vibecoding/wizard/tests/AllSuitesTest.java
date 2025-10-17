/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AllSuitesTest {
    @Test
    void runCustomSuite() {
        int exitCode = TestRunner.createSuite().runAll();
        assertEquals(0, exitCode, "Custom test suite failures");
    }
}
