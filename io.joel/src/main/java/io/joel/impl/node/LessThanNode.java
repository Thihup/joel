package io.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ELException;

import java.math.BigDecimal;
import java.math.BigInteger;

public record LessThanNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).compareTo((BigDecimal) context.convertToType(rightValue, BigDecimal.class)) < 0;
        }
        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            return ((BigInteger) context.convertToType(leftValue, BigInteger.class)).compareTo((BigInteger) context.convertToType(rightValue, BigInteger.class)) < 0;
        }
        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
            return ((Double) context.convertToType(leftValue, Double.class)).compareTo((Double) context.convertToType(rightValue, Double.class)) < 0;
        }
        if (leftValue instanceof Number || rightValue instanceof Number) {
            return (Long) context.convertToType(leftValue, Long.class) < (Long) context.convertToType(rightValue, Long.class);
        }
        if (leftValue instanceof String || rightValue instanceof String) {
            return ((String) context.convertToType(leftValue, String.class)).compareTo((String) context.convertToType(rightValue, String.class)) < 0;
        }
        if (leftValue instanceof Comparable comparable) {
            return comparable.compareTo(rightValue) < 0;
        }
        if (rightValue instanceof Comparable comparable) {
            return comparable.compareTo(leftValue) > 0;
        }
        throw new ELException("Cannot compare values: %s < %s".formatted(leftValue, rightValue));
    }
}
