package io.joel.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class Arithmetic {

    //region Constructor
    private Arithmetic() {
    }
    //endregion

    //region Addition
    public static byte add(byte a, byte b) {
        return (byte) (a + b);
    }

    public static short add(short a, short b) {
        return (short) (a + b);
    }

    public static char add(char a, char b) {
        return (char) (a + b);
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static long add(long a, long b) {
        return a + b;
    }

    public static float add(float a, float b) {
        return a + b;
    }

    public static double add(double a, double b) {
        return a + b;
    }

    public static BigInteger add(BigInteger a, BigInteger b) {
        return a.add(b);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }
    //endregion

    //region Subtraction
    public static byte sub(byte a, byte b) {
        return (byte) (a - b);
    }

    public static short sub(short a, short b) {
        return (short) (a - b);
    }

    public static char sub(char a, char b) {
        return (char) (a - b);
    }

    public static int sub(int a, int b) {
        return a - b;
    }

    public static long sub(long a, long b) {
        return a - b;
    }

    public static float sub(float a, float b) {
        return a - b;
    }

    public static double sub(double a, double b) {
        return a - b;
    }

    public static BigInteger sub(BigInteger a, BigInteger b) {
        return a.subtract(b);
    }

    public static BigDecimal sub(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }
    //endregion

    //region Multiplication
    public static byte mul(byte a, byte b) {
        return (byte) (a * b);
    }

    public static short mul(short a, short b) {
        return (short) (a * b);
    }

    public static char mul(char a, char b) {
        return (char) (a * b);
    }

    public static int mul(int a, int b) {
        return a * b;
    }

    public static long mul(long a, long b) {
        return a * b;
    }

    public static float mul(float a, float b) {
        return a * b;
    }

    public static double mul(double a, double b) {
        return a * b;
    }

    public static BigInteger mul(BigInteger a, BigInteger b) {
        return a.multiply(b);
    }

    public static BigDecimal mul(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }
    //endregion

    //region Division
    public static byte div(byte a, byte b) {
        return (byte) (a / b);
    }

    public static short div(short a, short b) {
        return (short) (a / b);
    }

    public static char div(char a, char b) {
        return (char) (a / b);
    }

    public static int div(int a, int b) {
        return a / b;
    }

    public static long div(long a, long b) {
        return a / b;
    }

    public static float div(float a, float b) {
        return a / b;
    }

    public static double div(double a, double b) {
        return a / b;
    }

    public static BigInteger div(BigInteger a, BigInteger b) {
        return a.divide(b);
    }

    public static BigDecimal div(BigDecimal a, BigDecimal b) {
        return a.divide(b, RoundingMode.HALF_UP);
    }
    //endregion

    //region Modulo
    public static byte mod(byte a, byte b) {
        return (byte) (a % b);
    }

    public static short mod(short a, short b) {
        return (short) (a % b);
    }

    public static char mod(char a, char b) {
        return (char) (a % b);
    }

    public static int mod(int a, int b) {
        return a % b;
    }

    public static long mod(long a, long b) {
        return a % b;
    }

    public static float mod(float a, float b) {
        return a % b;
    }

    public static double mod(double a, double b) {
        return a % b;
    }

    public static BigInteger mod(BigInteger a, BigInteger b) {
        return a.remainder(b);
    }

    public static BigDecimal mod(BigDecimal a, BigDecimal b) {
        return a.remainder(b);
    }
    //endregion

}
