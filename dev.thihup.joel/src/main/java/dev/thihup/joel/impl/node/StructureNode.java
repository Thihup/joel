package dev.thihup.joel.impl.node;

public sealed interface StructureNode extends Node permits
        CallExpressionNode,
        IdentifierNode,
        MemberNode {
}

