package dev.thihup.joel.impl.calculator;

import jakarta.el.ELContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public sealed interface Operation {
    Operation ADDITION = new Addition();
    Operation SUBTRACTION = new Subtraction();
    Operation MULTIPLICATION = new Multiplication();
    Operation DIVISION = new Division();
    Operation MODULO = new Modulo();

    Object calculate(Object leftValue, Object rightValue, ELContext context);

    record Addition() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL -> ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).add((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
                case BIG_INTEGER -> ((BigInteger) context.convertToType(leftValue, BigInteger.class)).add((BigInteger) context.convertToType(rightValue, BigInteger.class));
                case DOUBLE -> ((Double) context.convertToType(leftValue, Double.class)) + ((Double) context.convertToType(rightValue, Double.class));
                case LONG -> ((Long) context.convertToType(leftValue, Long.class)) + ((Long) context.convertToType(rightValue, Long.class));
            };
        }
    }

    record Subtraction() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL -> ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).subtract((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
                case BIG_INTEGER -> ((BigInteger) context.convertToType(leftValue, BigInteger.class)).subtract((BigInteger) context.convertToType(rightValue, BigInteger.class));
                case DOUBLE -> ((Double) context.convertToType(leftValue, Double.class)) - ((Double) context.convertToType(rightValue, Double.class));
                case LONG -> ((Long) context.convertToType(leftValue, Long.class)) - ((Long) context.convertToType(rightValue, Long.class));
            };
        }
    }

    record Multiplication() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL -> ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).multiply((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
                case BIG_INTEGER -> ((BigInteger) context.convertToType(leftValue, BigInteger.class)).multiply((BigInteger) context.convertToType(rightValue, BigInteger.class));
                case DOUBLE -> ((Double) context.convertToType(leftValue, Double.class)) * ((Double) context.convertToType(rightValue, Double.class));
                case LONG -> ((Long) context.convertToType(leftValue, Long.class)) * ((Long) context.convertToType(rightValue, Long.class));
            };
        }
    }

    record Division() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL, BIG_INTEGER -> ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).divide((BigDecimal) context.convertToType(rightValue, BigDecimal.class), RoundingMode.HALF_UP);
                case DOUBLE, LONG -> ((Double) context.convertToType(leftValue, Double.class)) / ((Double) context.convertToType(rightValue, Double.class));
            };
        }
    }

    record Modulo() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case DOUBLE, BIG_DECIMAL -> ((Double) context.convertToType(leftValue, Double.class)) % ((Double) context.convertToType(rightValue, Double.class));
                case BIG_INTEGER -> ((BigInteger) context.convertToType(leftValue, BigInteger.class)).remainder((BigInteger) context.convertToType(rightValue, BigInteger.class));
                case LONG -> ((Long) context.convertToType(leftValue, Long.class)) % ((Long) context.convertToType(rightValue, Long.class));
            };
        }
    }

}
