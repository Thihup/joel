package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ELException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public sealed interface RelationalNode extends Node permits
        AndNode,
        EqualNode,
        GreaterEqualNode,
        GreaterThanNode,
        LessEqualNode,
        LessThanNode,
        NotEqualNode,
        OrNode {
    Node left();

    Node right();

    @Override
    default Class<?> getType(ELContext context) {
        return Boolean.class;
    }

    static boolean isDateTime(Object obj) {
        return obj instanceof java.time.temporal.TemporalAccessor
                || obj instanceof java.time.Clock
                || obj instanceof java.util.Date;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "cast"})
    static int compare(Object leftValue, Object rightValue, ELContext context) {
        if (leftValue instanceof String s1 && rightValue instanceof String s2) {
            return s1.compareTo(s2);
        }
        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            BigDecimal b1 = leftValue instanceof BigDecimal bd ? bd : context.convertToType(leftValue, BigDecimal.class);
            BigDecimal b2 = rightValue instanceof BigDecimal bd ? bd : context.convertToType(rightValue, BigDecimal.class);
            return b1.compareTo(b2);
        }
        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            BigInteger b1 = leftValue instanceof BigInteger bi ? bi : context.convertToType(leftValue, BigInteger.class);
            BigInteger b2 = rightValue instanceof BigInteger bi ? bi : context.convertToType(rightValue, BigInteger.class);
            return b1.compareTo(b2);
        }
        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
            double d1 = leftValue instanceof Number n ? n.doubleValue() : context.convertToType(leftValue, Double.class);
            double d2 = rightValue instanceof Number n ? n.doubleValue() : context.convertToType(rightValue, Double.class);
            return Double.compare(d1, d2);
        }
        if (leftValue instanceof Number || rightValue instanceof Number) {
            long l1 = leftValue instanceof Number n ? n.longValue() : context.convertToType(leftValue, Long.class);
            long l2 = rightValue instanceof Number n ? n.longValue() : context.convertToType(rightValue, Long.class);
            return Long.compare(l1, l2);
        }
        if (leftValue instanceof Character || rightValue instanceof Character) {
            short s1 = leftValue instanceof Character c ? (short) c.charValue() : (short) ((Number) context.convertToType(leftValue, Short.class)).shortValue();
            short s2 = rightValue instanceof Character c ? (short) c.charValue() : (short) ((Number) context.convertToType(rightValue, Short.class)).shortValue();
            return Short.compare(s1, s2);
        }
        if (leftValue instanceof Boolean || rightValue instanceof Boolean) {
            boolean b1 = leftValue instanceof Boolean b ? b : context.convertToType(leftValue, Boolean.class);
            boolean b2 = rightValue instanceof Boolean b ? b : context.convertToType(rightValue, Boolean.class);
            return Boolean.compare(b1, b2);
        }
        if (isDateTime(leftValue) || isDateTime(rightValue)) {
            Instant i1 = leftValue instanceof Instant i ? i : context.convertToType(leftValue, Instant.class);
            Instant i2 = rightValue instanceof Instant i ? i : context.convertToType(rightValue, Instant.class);
            return i1.compareTo(i2);
        }
        if (leftValue instanceof Comparable c1 && rightValue instanceof Comparable c2) {
            return c1.compareTo(c2);
        }

        throw new ELException("Could not compare %s with %s".formatted(leftValue, rightValue));
    }
}
