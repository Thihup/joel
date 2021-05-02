package io.joel.impl;

import io.joel.impl.node.ExpressionNode;
import io.joel.impl.node.InfixExpressionNode;
import jakarta.el.ELContext;
import jakarta.el.PropertyNotFoundException;
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
    public boolean isLiteralText() {
        return expressionNode instanceof ExpressionNode.StringNode;
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
        try {
            context.notifyBeforeEvaluation(expression);
            return context.convertToType(expressionNode.getValue(context), expectedType);
        } finally {
            context.notifyAfterEvaluation(expression);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        if (expressionNode instanceof ExpressionNode.MemberNode memberNode) {
            var valueReference = memberNode.valueReference(context);
            boolean readOnly = context.getELResolver().isReadOnly(context, valueReference.getBase(), valueReference.getProperty());
            if (!context.isPropertyResolved())
                throw new PropertyNotFoundException();
            return readOnly;
        }
        if (expressionNode instanceof ExpressionNode.IdentifierNode identifierNode) {
            boolean readOnly = context.getELResolver().isReadOnly(context, null, identifierNode.value());
            if (!context.isPropertyResolved())
                throw new PropertyNotFoundException();
            return readOnly;
        }
        return true;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        new InfixExpressionNode.AssignNode(expressionNode, new InfixExpressionNode.ObjectNode(value)).getValue(context);
        if (!context.isPropertyResolved())
            throw new PropertyNotFoundException();
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        if (expressionNode instanceof ExpressionNode.MemberNode memberNode) {
            return memberNode.valueReference(context);
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, expressionNode, expectedType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoelValueExpression that = (JoelValueExpression) o;
        return Objects.equals(expression, that.expression) && Objects.equals(expressionNode, that.expressionNode) && Objects.equals(expectedType, that.expectedType);
    }

    @Override
    public String toString() {
        return expression;
    }
}
