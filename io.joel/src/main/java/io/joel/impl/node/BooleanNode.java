package io.joel.impl.node;

import jakarta.el.ELContext;

public enum BooleanNode implements BooleanExpression {
    TRUE, FALSE;

    @Override
    public Object getValue(ELContext context) {
        return this == TRUE ? Boolean.TRUE : Boolean.FALSE;
    }

    public BooleanNode negate() {
        if (this == TRUE)
            return FALSE;
        return TRUE;
    }

    @Override
    public String toString() {
        return "BooleanNode[" + super.toString() + "]";
    }

    @Override
    public Class<?> getType(ELContext context) {
        return Boolean.class;
    }
}
