package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public enum NullNode implements Node {
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
    public String prettyPrint() {
        return "null";
    }

    @Override
    public String toString() {
        return "NullNode[" + super.toString() + "]";
    }
}
