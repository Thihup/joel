package io.joel.impl.node;

import jakarta.el.ELContext;

public record ConcatNode(Node left, Node right) implements InfixExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue(ELContext context) {
        return (String) context.convertToType(left.getValue(context), String.class) + context.convertToType(right.getValue(context), String.class);
    }

    @Override
    public String prettyPrint() {
        return "%s += %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
