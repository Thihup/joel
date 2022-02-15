package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public record UnaryEmptyNode(Node node) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        return Boolean.class;
    }

    @Override
    public Object getValue(ELContext context) {
        Object value = node.getValue(context);
        if (value == null)
            return true;
        if (value instanceof String newValue && newValue.isEmpty())
            return true;
        if (value.getClass().isArray() && Array.getLength(value) == 0)
            return true;
        if (value instanceof Map<?, ?> newValue && newValue.isEmpty())
            return true;
        return value instanceof Collection<?> newValue && newValue.isEmpty();
    }

    @Override
    public String prettyPrint() {
        return "empty %s".formatted(node.prettyPrint());
    }
}
