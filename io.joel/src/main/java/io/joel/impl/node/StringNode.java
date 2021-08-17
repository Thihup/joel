package io.joel.impl.node;

import jakarta.el.ELContext;

public record StringNode(String value) implements ExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        return String.class;
    }

    @Override
    public Object getValue(ELContext context) {
        return value;
    }
}
