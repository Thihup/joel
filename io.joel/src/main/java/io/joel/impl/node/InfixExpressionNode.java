package io.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueReference;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public sealed interface InfixExpressionNode extends ExpressionNode permits
        AddExpressionNode,
        AssignNode,
        ConcatNode,
        DivExpressionNode,
        ModExpressionNode,
        MulExpressionNode,
        SubExpressionNode {
    ExpressionNode left();

    ExpressionNode right();

    @Override
    default Class<?> getType(ELContext context) {
        return Number.class;
    }

}
