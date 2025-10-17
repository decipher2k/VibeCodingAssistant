/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.util;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ensures JavaFX toolkit is initialised for UI-related tests.
 */
public abstract class FxTestSupport {

    private static final AtomicBoolean INITIALISED = new AtomicBoolean(false);

    @BeforeAll
    static void setupJavaFx() throws Exception {
        if (INITIALISED.get()) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
        INITIALISED.set(true);
    }
}
