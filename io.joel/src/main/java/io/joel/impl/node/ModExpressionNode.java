package io.joel.impl.node;

import io.joel.impl.Utils;
import jakarta.el.ELContext;

import java.math.BigDecimal;
import java.math.BigInteger;

public record ModExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        if (leftValue == null && rightValue == null)
            return 0L;
        if (leftValue instanceof Float || leftValue instanceof Double || leftValue instanceof BigDecimal ||
                rightValue instanceof Float || rightValue instanceof Double || rightValue instanceof BigDecimal) {
            return (Double) context.convertToType(leftValue, Double.class) % (Double) (context.convertToType(rightValue, Double.class));
        }
        if (leftValue instanceof String asString && (Utils.isFloatingPointNumber(asString))) {
            return (Double) context.convertToType(leftValue, Double.class) % (Double) (context.convertToType(rightValue, Double.class));
        }
        if (rightValue instanceof String asString && (Utils.isFloatingPointNumber(asString))) {
            return (Double) context.convertToType(leftValue, Double.class) % (Double) (context.convertToType(rightValue, Double.class));
        }
        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            return ((BigInteger) context.convertToType(leftValue, BigInteger.class)).remainder((BigInteger) context.convertToType(rightValue, BigInteger.class));
        }

        return (Long) context.convertToType(leftValue, Long.class) % (Long) context.convertToType(rightValue, Long.class);
    }

    @Override
    public String prettyPrint() {
        return "%s %% %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
