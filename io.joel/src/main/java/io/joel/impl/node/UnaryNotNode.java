package io.joel.impl.node;

import jakarta.el.ELContext;

public record UnaryNotNode(ExpressionNode node) implements ExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        return Boolean.class;
    }

    @Override
    public Object getValue(ELContext context) {
        return !(boolean) context.convertToType(node.getValue(context), boolean.class);
    }

    @Override
    public String prettyPrint() {
        return "! %s".formatted(node.prettyPrint());
    }
}
