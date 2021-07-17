package io.joel.impl.node;

import jakarta.el.ELContext;

public record NumberNode(Number value) implements ExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        return value.getClass();
    }

    @Override
    public Object getValue(ELContext context) {
        return value;
    }

    @Override
    public String prettyPrint() {
        return value.toString();
    }
}
