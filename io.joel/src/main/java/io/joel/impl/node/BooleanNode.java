package io.joel.impl.node;

import jakarta.el.ELContext;

public enum BooleanNode implements BooleanExpression {
    TRUE, FALSE;

    @Override
    public Object getValue(ELContext context) {
        return this == TRUE ? Boolean.TRUE : Boolean.FALSE;
    }

    public io.joel.impl.node.BooleanNode negate() {
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

    @Override
    public String prettyPrint() {
        return switch (this) {
            case TRUE -> "true";
            case FALSE -> "false";
        };
    }
}
