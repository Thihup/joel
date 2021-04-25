package io.joel.impl;

import io.joel.impl.node.ExpressionNode;
import io.joel.impl.node.InfixExpressionNode;
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
        return Objects.hash(expression, expressionNode, expectedType);
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
    public <T> T getValue(ELContext context) {
        try {
            context.notifyBeforeEvaluation(expression);
            return (T) context.convertToType(expressionNode.getValue(context), expectedType);
        } finally {
            context.notifyAfterEvaluation(expression);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        return false;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        new InfixExpressionNode.AssignNode(expressionNode, new InfixExpressionNode.ObjectNode(value)).getValue(context);
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        if (expressionNode instanceof ExpressionNode.DynamicExpressionNode dynamicNode && dynamicNode.node() instanceof ExpressionNode.MemberNode memberNode) {
            return memberNode.valueReference(context);
        }
        if (expressionNode instanceof ExpressionNode.DeferredExpressionNode deferredNode && deferredNode.node() instanceof ExpressionNode.MemberNode memberNode) {
            return memberNode.valueReference(context);
        }
        return null;
    }

    @Override
    public String toString() {
        return expression;
    }
}
