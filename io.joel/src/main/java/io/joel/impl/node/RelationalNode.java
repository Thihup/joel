package io.joel.impl.node;

public sealed interface RelationalNode extends BooleanExpression permits
        AndNode,
        EqualNode,
        GreaterEqualNode,
        GreaterThanNode,
        LessEqualNode,
        LessThanNode,
        NotEqualNode,
        OrNode {
    Node left();

    Node right();

}
