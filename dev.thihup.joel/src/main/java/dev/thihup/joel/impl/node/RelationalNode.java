package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

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

    @Override
    default void bind(ELContext context) {
        if (left() != null) left().bind(context);
        if (right() != null) right().bind(context);
    }

}
