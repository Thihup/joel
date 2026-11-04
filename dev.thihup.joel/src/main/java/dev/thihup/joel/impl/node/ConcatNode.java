package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

@SuppressWarnings("cast")
public record ConcatNode(Node left, Node right) implements InfixExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        throw new UnsupportedOperationException();
    }

    private static String toString(Object val, ELContext context) {
        if (val instanceof String s) return s;
        if (val == null) return "";
        return (String) context.convertToType(val, String.class);
    }

    @Override
    public Object getValue(ELContext context) {
        return toString(left.getValue(context), context) + toString(right.getValue(context), context);
    }

    @Override
    public String prettyPrint() {
        return "%s += %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
