package io.joel.impl.node;

import jakarta.el.ELContext;

public record DynamicExpressionNode(ExpressionNode node) implements ExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        return node.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        return node.getValue(context);
    }
}
