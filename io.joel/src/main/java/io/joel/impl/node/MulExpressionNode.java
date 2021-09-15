package io.joel.impl.node;

import io.joel.impl.calculator.Operation;
import jakarta.el.ELContext;

public record MulExpressionNode(Node left, Node right) implements InfixExpressionNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        return Operation.MULTIPLICATION.calculate(leftValue, rightValue, context);
    }

    @Override
    public String prettyPrint() {
        return "%s * %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
