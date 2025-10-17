/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard.tests;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static Object getField(Object target, String name) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Object target, String name, Class<T> type) {
        return (T) getField(target, name);
    }

    public static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
