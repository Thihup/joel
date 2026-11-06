package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public value record StringNode(String value) implements LiteralNode {
    @Override
    public Class<?> getType(ELContext context) {
        return String.class;
    }

    @Override
    public Object getValue(ELContext context) {
        return value;
    }

    @Override
    public String prettyPrint() {
        return "'%s'".formatted(value);
    }
}
