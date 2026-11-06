package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public value record NumberNode(Number value) implements LiteralNode {
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
