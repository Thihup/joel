package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public record NotEqualNode(Node left, Node right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        return !(Boolean) new EqualNode(left, right).getValue(context);
    }

    @Override
    public String prettyPrint() {
        return "%s != %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
