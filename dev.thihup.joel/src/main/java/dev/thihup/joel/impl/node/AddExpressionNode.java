package dev.thihup.joel.impl.node;

import dev.thihup.joel.impl.calculator.Operation;
import jakarta.el.ELContext;

public record AddExpressionNode(Node left, Node right) implements InfixExpressionNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        return Operation.ADDITION.calculate(leftValue, rightValue, context);
    }

    @Override
    public String prettyPrint() {
        return "%s + %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
