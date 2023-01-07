package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public record OrNode(Node left, Node right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        if (context.convertToType(left.getValue(context), boolean.class))
            return true;
        return context.convertToType(right.getValue(context), boolean.class);
    }

    @Override
    public String prettyPrint() {
        return "%s || %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
