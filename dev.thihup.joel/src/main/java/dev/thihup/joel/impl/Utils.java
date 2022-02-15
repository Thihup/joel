package dev.thihup.joel.impl;

public final class Utils {
    private Utils() {
    }

    public static boolean isFloatingPointNumber(String value) {
        return value.indexOf('.') >= 0 || value.indexOf('e') >= 0 || value.indexOf('E') >= 0;
    }
}
