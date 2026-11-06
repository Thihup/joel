package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

import java.io.Serializable;

public sealed interface Node extends Serializable
        permits LiteralNode,
        InfixExpressionNode,
        RelationalNode,
        UnaryNode,
        ConditionalNode,
        StructureNode,
        CollectionNode,
        WrapperNode {

    default Object getValue(ELContext context) {
        return NodeEvaluator.eval(this, context);
    }

    default Class<?> getType(ELContext context) {
        throw new UnsupportedOperationException(this.toString());
    }

    default String prettyPrint() {
        throw new UnsupportedOperationException();
    }
}
