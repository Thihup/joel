package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

@SuppressWarnings("cast")
public value record GreaterThanNode(Node left, Node right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        return RelationalNode.compare(leftValue, rightValue, context) > 0;
    }

    @Override
    public String prettyPrint() {
        return "%s > %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
