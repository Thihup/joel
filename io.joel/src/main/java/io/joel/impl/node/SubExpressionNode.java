package io.joel.impl.node;

import io.joel.impl.calculator.Operation;
import jakarta.el.ELContext;

public record SubExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        return Operation.SUBTRACTION.calculate(leftValue, rightValue, context);
    }
}
