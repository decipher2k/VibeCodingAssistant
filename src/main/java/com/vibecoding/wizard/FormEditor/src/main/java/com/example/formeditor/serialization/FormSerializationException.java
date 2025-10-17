/* Copyright 2025 Dennis Michael Heine */
package com.example.formeditor.serialization;

/**
 * Runtime exception thrown when form serialization or deserialization fails.
 */
public class FormSerializationException extends RuntimeException {
    public FormSerializationException(String message) {
        super(message);
    }

    public FormSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
