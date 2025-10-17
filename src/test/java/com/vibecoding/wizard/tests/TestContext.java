/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.util.Objects;

public final class TestContext {
    private final String testName;

    TestContext(String testName) {
        this.testName = testName;
    }

    public void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(testName + ": " + message + " expected <true> but was <false>");
        }
    }

    public void assertFalse(String message, boolean condition) {
        if (condition) {
            throw new AssertionError(testName + ": " + message + " expected <false> but was <true>");
        }
    }

    public void assertEquals(String message, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(testName + ": " + message + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public void assertNotNull(String message, Object value) {
        if (value == null) {
            throw new AssertionError(testName + ": " + message + " expected non-null value");
        }
    }

    public void fail(String message) {
        throw new AssertionError(testName + ": " + message);
    }

    public void skip(String reason) {
        throw new SkippedTestException(testName + " skipped: " + reason);
    }
}
