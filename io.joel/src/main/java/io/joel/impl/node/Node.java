package io.joel.impl.node;

import jakarta.el.ELContext;

import java.io.Serializable;

public sealed interface Node extends Serializable
        permits CallExpressionNode,
        BooleanExpression,
        InfixExpressionNode,
        DeferredExpressionNode,
        DynamicExpressionNode,
        IdentifierNode,
        LambdaNode,
        ListNode,
        MemberNode,
        NullNode,
        NumberNode,
        SemicolonNode,
        SetNode,
        StringNode,
        TernaryNode,
        UnaryEmptyNode,
        UnaryMinusNode,
        UnaryNotNode,
        ObjectNode {

    default Object getValue(ELContext context) {
        throw new UnsupportedOperationException(this.toString());
    }

    default Class<?> getType(ELContext context) {
        throw new UnsupportedOperationException(this.toString());
    }

    default String prettyPrint() {
        throw new UnsupportedOperationException();
    }
}
