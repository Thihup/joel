package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public record ObjectNode(Object value) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        return value.getClass();
    }

    @Override
    public Object getValue(ELContext context) {
        return value;
    }
}
