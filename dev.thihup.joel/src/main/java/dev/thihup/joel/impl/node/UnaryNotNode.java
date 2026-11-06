package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

@SuppressWarnings("cast")
public value record UnaryNotNode(Node node) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        return Boolean.class;
    }

    @Override
    public Object getValue(ELContext context) {
        Object val = node.getValue(context);
        if (val instanceof Boolean b) {
            return !b;
        }
        return !(boolean) context.convertToType(val, boolean.class);
    }

    @Override
    public String prettyPrint() {
        return "! %s".formatted(node.prettyPrint());
    }
}
