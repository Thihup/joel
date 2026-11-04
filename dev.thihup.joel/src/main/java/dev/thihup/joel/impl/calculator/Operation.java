package dev.thihup.joel.impl.calculator;

import jakarta.el.ELContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@SuppressWarnings("cast")
public sealed interface Operation {
    Operation ADDITION = new Addition();
    Operation SUBTRACTION = new Subtraction();
    Operation MULTIPLICATION = new Multiplication();
    Operation DIVISION = new Division();
    Operation MODULO = new Modulo();

    Object calculate(Object leftValue, Object rightValue, ELContext context);

    static Long toLong(Object value, ELContext context) {
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        if (value == null) return 0L;
        return (Long) context.convertToType(value, Long.class);
    }

    static Double toDouble(Object value, ELContext context) {
        if (value instanceof Double d) return d;
        if (value instanceof Number n) return n.doubleValue();
        if (value == null) return 0.0;
        return (Double) context.convertToType(value, Double.class);
    }

    static BigDecimal toBigDecimal(Object value, ELContext context) {
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof BigInteger bi) return new BigDecimal(bi);
        if (value instanceof Number n) return new BigDecimal(n.doubleValue());
        if (value == null) return BigDecimal.ZERO;
        return (BigDecimal) context.convertToType(value, BigDecimal.class);
    }

    static BigInteger toBigInteger(Object value, ELContext context) {
        if (value instanceof BigInteger bi) return bi;
        if (value instanceof Number n) return BigInteger.valueOf(n.longValue());
        if (value == null) return BigInteger.ZERO;
        return (BigInteger) context.convertToType(value, BigInteger.class);
    }

    record Addition() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL -> toBigDecimal(leftValue, context).add(toBigDecimal(rightValue, context));
                case BIG_INTEGER -> toBigInteger(leftValue, context).add(toBigInteger(rightValue, context));
                case DOUBLE -> toDouble(leftValue, context) + toDouble(rightValue, context);
                case LONG -> toLong(leftValue, context) + toLong(rightValue, context);
            };
        }
    }

    record Subtraction() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL -> toBigDecimal(leftValue, context).subtract(toBigDecimal(rightValue, context));
                case BIG_INTEGER -> toBigInteger(leftValue, context).subtract(toBigInteger(rightValue, context));
                case DOUBLE -> toDouble(leftValue, context) - toDouble(rightValue, context);
                case LONG -> toLong(leftValue, context) - toLong(rightValue, context);
            };
        }
    }

    record Multiplication() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL -> toBigDecimal(leftValue, context).multiply(toBigDecimal(rightValue, context));
                case BIG_INTEGER -> toBigInteger(leftValue, context).multiply(toBigInteger(rightValue, context));
                case DOUBLE -> toDouble(leftValue, context) * toDouble(rightValue, context);
                case LONG -> toLong(leftValue, context) * toLong(rightValue, context);
            };
        }
    }

    record Division() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case BIG_DECIMAL, BIG_INTEGER -> toBigDecimal(leftValue, context).divide(toBigDecimal(rightValue, context), RoundingMode.HALF_UP);
                case DOUBLE, LONG -> toDouble(leftValue, context) / toDouble(rightValue, context);
            };
        }
    }

    record Modulo() implements Operation {
        @Override
        public Object calculate(Object leftValue, Object rightValue, ELContext context) {
            return switch (ConversionType.of(leftValue, rightValue)) {
                case ZERO -> 0L;
                case DOUBLE, BIG_DECIMAL -> toDouble(leftValue, context) % toDouble(rightValue, context);
                case BIG_INTEGER -> toBigInteger(leftValue, context).remainder(toBigInteger(rightValue, context));
                case LONG -> toLong(leftValue, context) % toLong(rightValue, context);
            };
        }
    }
}
