/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

@FunctionalInterface
public interface TestCase {
    void run(TestContext context) throws Exception;
}
