package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ELException;

import java.math.BigDecimal;
import java.math.BigInteger;

public record GreaterThanNode(Node left, Node right) implements RelationalNode {
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            return context.convertToType(leftValue, BigDecimal.class).compareTo(context.convertToType(rightValue, BigDecimal.class)) > 0;
        }
        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            return context.convertToType(leftValue, BigInteger.class).compareTo(context.convertToType(rightValue, BigInteger.class)) > 0;
        }
        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
            return context.convertToType(leftValue, Double.class).compareTo(context.convertToType(rightValue, Double.class)) > 0;
        }
        if (leftValue instanceof Number || rightValue instanceof Number) {
            return context.convertToType(leftValue, Long.class) > context.convertToType(rightValue, Long.class);
        }
        if (leftValue instanceof String || rightValue instanceof String) {
            return context.convertToType(leftValue, String.class).compareTo(context.convertToType(rightValue, String.class)) > 0;
        }
        if (leftValue instanceof Comparable comparable) {
            return comparable.compareTo(rightValue) > 0;
        }
        if (rightValue instanceof Comparable comparable) {
            return comparable.compareTo(leftValue) < 0;
        }
        throw new ELException("Cannot compare values: %s > %s".formatted(leftValue, rightValue));
    }

    @Override
    public String prettyPrint() {
        return "%s > %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
