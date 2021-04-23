package io.joel.impl;

import io.joel.impl.node.ExpressionNode;
import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;

import java.util.Objects;

public class JoelValueExpression extends ValueExpression {
    private final String expression;
    private final ExpressionNode expressionNode;
    private final Class<?> expectedType;

    public JoelValueExpression(String expression, ExpressionNode expressionNode, Class<?> expectedType) {
        this.expression = expression;
        this.expressionNode = expressionNode;
        this.expectedType = expectedType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, expectedType);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public boolean isLiteralText() {
        return false;
    }

    @Override
    public String getExpressionString() {
        return expression;
    }

    @Override
    public Class<?> getExpectedType() {
        return expectedType;
    }

    @Override
    public Class<?> getType(ELContext context) {
        return expressionNode.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        return context.convertToType(expressionNode.getValue(context), expectedType);
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        return false;
    }

    @Override
    public void setValue(ELContext context, Object value) {
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        return super.getValueReference(context);
    }

    @Override
    public String toString() {
        return expression;
    }
}
