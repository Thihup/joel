package io.joel.impl.node;

import jakarta.el.ELContext;

public sealed interface InfixExpressionNode extends Node permits
        AddExpressionNode,
        AssignNode,
        ConcatNode,
        DivExpressionNode,
        ModExpressionNode,
        MulExpressionNode,
        SubExpressionNode {
    Node left();

    Node right();

    @Override
    default Class<?> getType(ELContext context) {
        return Number.class;
    }

}
