package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public value record NotEqualNode(Node left, Node right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        return !EqualNode.equals(left.getValue(context), right.getValue(context), context);
    }

    @Override
    public String prettyPrint() {
        return "%s != %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
