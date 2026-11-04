package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

@SuppressWarnings("cast")
public record LessEqualNode(Node left, Node right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        if (leftValue == rightValue)
            return true;
        if (leftValue == null || rightValue == null) {
            return false;
        }
        if (leftValue.equals(rightValue))
            return true;
        return RelationalNode.compare(leftValue, rightValue, context) <= 0;
    }

    @Override
    public String prettyPrint() {
        return "%s <= %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
