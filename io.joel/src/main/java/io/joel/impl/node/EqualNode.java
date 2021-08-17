package io.joel.impl.node;

import jakarta.el.ELContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiPredicate;

public record EqualNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        BiPredicate<Object, Object> equalsFunction = Object::equals;
        if (leftValue == rightValue)
            return true;

        if (leftValue == null || rightValue == null) {
            return false;
        }
        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            return equalsFunction.test(context.convertToType(leftValue, BigDecimal.class), context.convertToType(rightValue, BigDecimal.class));
        }
        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            return equalsFunction.test(context.convertToType(leftValue, BigInteger.class), context.convertToType(rightValue, BigInteger.class));
        }
        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
            return equalsFunction.test(context.convertToType(leftValue, Double.class), context.convertToType(rightValue, Double.class));
        }
        if (leftValue instanceof Number || rightValue instanceof Number) {
            return equalsFunction.test(context.convertToType(leftValue, Long.class), context.convertToType(rightValue, Long.class));
        }
        if (leftValue instanceof Boolean || rightValue instanceof Boolean) {
            return equalsFunction.test(context.convertToType(leftValue, Boolean.class), context.convertToType(rightValue, Boolean.class));
        }
        if (leftValue instanceof Enum<?>) {
            return equalsFunction.test(context.convertToType(leftValue, leftValue.getClass()), context.convertToType(rightValue, leftValue.getClass()));
        }
        if (rightValue instanceof Enum<?>) {
            return equalsFunction.test(context.convertToType(leftValue, rightValue.getClass()), context.convertToType(rightValue, rightValue.getClass()));
        }
        if (leftValue instanceof String || rightValue instanceof String) {
            return ((String) context.convertToType(leftValue, String.class)).compareTo((String) context.convertToType(rightValue, String.class)) == 0;
        }
        return leftValue.equals(rightValue);
    }
}
