package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public record DynamicExpressionNode(Node node) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        return node.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        return node.getValue(context);
    }

    @Override
    public String prettyPrint() {
        return "${%s}".formatted(node.prettyPrint());
    }
}
