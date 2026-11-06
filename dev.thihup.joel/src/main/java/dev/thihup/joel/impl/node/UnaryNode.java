package dev.thihup.joel.impl.node;

public sealed interface UnaryNode extends Node permits
        UnaryEmptyNode,
        UnaryMinusNode,
        UnaryNotNode {
    Node node();
}

