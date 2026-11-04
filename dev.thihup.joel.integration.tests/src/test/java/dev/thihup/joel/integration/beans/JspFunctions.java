package dev.thihup.joel.integration.beans;

public class JspFunctions {
    public static String toUpperCase(String input) {
        return input != null ? input.toUpperCase() : null;
    }

    public static String join(String a, String b) {
        return a + "-" + b;
    }

    public static int add(int a, int b) {
        return a + b;
    }
}

