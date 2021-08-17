package io.joel.impl.node;

import jakarta.el.ELContext;

public record SemicolonNode(ExpressionNode left, ExpressionNode right) implements ExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        return right.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        left.getValue(context);
        return right.getValue(context);
    }
}
