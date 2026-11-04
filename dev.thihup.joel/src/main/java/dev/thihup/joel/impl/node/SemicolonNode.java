package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public record SemicolonNode(Node left, Node right) implements Node {
    @Override
    public void bind(ELContext context) {
        if (left != null) left.bind(context);
        if (right != null) right.bind(context);
    }

    @Override
    public Class<?> getType(ELContext context) {
        return right.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        left.getValue(context);
        return right.getValue(context);
    }

    @Override
    public String prettyPrint() {
        return "%s; %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
