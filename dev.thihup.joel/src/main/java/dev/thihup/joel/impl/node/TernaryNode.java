package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public record TernaryNode(Node condition, Node trueExpression,
                          Node falseExpression) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        if (context.convertToType(condition.getValue(context), boolean.class)) {
            return trueExpression.getType(context);
        }
        return falseExpression.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        if (context.convertToType(condition.getValue(context), boolean.class)) {
            return trueExpression.getValue(context);
        }
        return falseExpression.getValue(context);
    }

    @Override
    public String prettyPrint() {
        return "%s ? %s : %s".formatted(condition.prettyPrint(), trueExpression.prettyPrint(), falseExpression.prettyPrint());
    }
}
