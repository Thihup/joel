package io.joel.impl.node;

import jakarta.el.ELContext;

public record TernaryNode(ExpressionNode condition, ExpressionNode trueExpression,
                          ExpressionNode falseExpression) implements ExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        if ((boolean) context.convertToType(condition.getValue(context), boolean.class)) {
            return trueExpression.getType(context);
        }
        return falseExpression.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        if ((boolean) context.convertToType(condition.getValue(context), boolean.class)) {
            return trueExpression.getValue(context);
        }
        return falseExpression.getValue(context);
    }
}
