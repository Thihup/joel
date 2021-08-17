package io.joel.impl.node;

import jakarta.el.ELContext;

public enum NullNode implements ExpressionNode {
    INSTANCE;

    @Override
    public Object getValue(ELContext context) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context) {
        return null;
    }

    @Override
    public String toString() {
        return "NullNode[" + super.toString() + "]";
    }
}
