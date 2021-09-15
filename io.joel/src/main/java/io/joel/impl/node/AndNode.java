package io.joel.impl.node;

import jakarta.el.ELContext;

public record AndNode(Node left, Node right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        if (!(boolean) context.convertToType(left.getValue(context), boolean.class))
            return false;
        return context.convertToType(right.getValue(context), boolean.class);
    }

    @Override
    public String prettyPrint() {
        return "%s && %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
