package dev.thihup.joel.impl.node;

public sealed interface ConditionalNode extends Node permits
        ElvisNode,
        NullCoalescingNode,
        TernaryNode {
}

