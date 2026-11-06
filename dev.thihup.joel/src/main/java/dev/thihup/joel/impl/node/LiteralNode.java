package dev.thihup.joel.impl.node;

public sealed interface LiteralNode extends Node permits
        BooleanNode,
        NullNode,
        NumberNode,
        ObjectNode,
        StringNode {
}

