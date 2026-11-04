package dev.thihup.joel.impl.node;

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

    @Override
    default void bind(ELContext context) {
        if (left() != null) left().bind(context);
        if (right() != null) right().bind(context);
    }

}
