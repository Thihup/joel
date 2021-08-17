package io.joel.impl.node;

import jakarta.el.ELContext;

public record NotEqualNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        return !(Boolean) new EqualNode(left, right).getValue(context);
    }
}
