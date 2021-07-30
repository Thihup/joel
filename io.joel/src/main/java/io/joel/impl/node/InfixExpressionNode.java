package io.joel.impl.node;

import jakarta.el.ELContext;

public sealed interface InfixExpressionNode extends ExpressionNode permits
        AddExpressionNode,
        AssignNode,
        ConcatNode,
        DivExpressionNode,
        ModExpressionNode,
        MulExpressionNode,
        SubExpressionNode {
    ExpressionNode left();

    ExpressionNode right();

    @Override
    default Class<?> getType(ELContext context) {
        return Number.class;
    }

}
