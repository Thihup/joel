package dev.thihup.joel.impl.node;

public sealed interface WrapperNode extends Node permits
        DeferredExpressionNode,
        DynamicExpressionNode,
        LambdaNode,
        SemicolonNode {
}

