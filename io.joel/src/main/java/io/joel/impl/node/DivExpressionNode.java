package io.joel.impl.node;

import jakarta.el.ELContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public record DivExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        if (leftValue == null && rightValue == null)
            return 0L;
        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal || leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).divide((BigDecimal) context.convertToType(rightValue, BigDecimal.class), RoundingMode.HALF_UP);
        }
        return (Double) context.convertToType(leftValue, Double.class) / (Double) context.convertToType(rightValue, Double.class);
    }

    @Override
    public String prettyPrint() {
        return "%s / %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
