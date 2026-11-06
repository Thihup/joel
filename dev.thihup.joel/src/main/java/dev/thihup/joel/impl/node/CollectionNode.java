package dev.thihup.joel.impl.node;

public sealed interface CollectionNode extends Node permits
        ListNode,
        MapNode,
        SetNode {
}

