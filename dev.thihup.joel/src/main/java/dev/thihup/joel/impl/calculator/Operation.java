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
                case BIG_DECIMAL -> context.convertToType(leftValue, BigDecimal.class).add(context.convertToType(rightValue, BigDecimal.class));
                case BIG_INTEGER -> context.convertToType(leftValue, BigInteger.class).add(context.convertToType(rightValue, BigInteger.class));
                case DOUBLE -> context.convertToType(leftValue, Double.class) + context.convertToType(rightValue, Double.class);
                case LONG -> context.convertToType(leftValue, Long.class) + context.convertToType(rightValue, Long.class);
            };
        }
    }

    record Subtraction() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL -> context.convertToType(leftValue, BigDecimal.class).subtract(context.convertToType(rightValue, BigDecimal.class));
                case BIG_INTEGER -> context.convertToType(leftValue, BigInteger.class).subtract(context.convertToType(rightValue, BigInteger.class));
                case DOUBLE -> context.convertToType(leftValue, Double.class) - context.convertToType(rightValue, Double.class);
                case LONG -> context.convertToType(leftValue, Long.class) - context.convertToType(rightValue, Long.class);
            };
        }
    }

    record Multiplication() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL -> context.convertToType(leftValue, BigDecimal.class).multiply(context.convertToType(rightValue, BigDecimal.class));
                case BIG_INTEGER -> context.convertToType(leftValue, BigInteger.class).multiply(context.convertToType(rightValue, BigInteger.class));
                case DOUBLE -> context.convertToType(leftValue, Double.class) * context.convertToType(rightValue, Double.class);
                case LONG -> context.convertToType(leftValue, Long.class) * context.convertToType(rightValue, Long.class);
            };
        }
    }

    record Division() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL, BIG_INTEGER -> context.convertToType(leftValue, BigDecimal.class).divide(context.convertToType(rightValue, BigDecimal.class), RoundingMode.HALF_UP);
                case DOUBLE, LONG -> context.convertToType(leftValue, Double.class) / context.convertToType(rightValue, Double.class);
            };
        }
    }

    record Modulo() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case DOUBLE, BIG_DECIMAL -> context.convertToType(leftValue, Double.class) % context.convertToType(rightValue, Double.class);
                case BIG_INTEGER -> context.convertToType(leftValue, BigInteger.class).remainder(context.convertToType(rightValue, BigInteger.class));
                case LONG -> context.convertToType(leftValue, Long.class) % context.convertToType(rightValue, Long.class);
            };
        }
    }

}
