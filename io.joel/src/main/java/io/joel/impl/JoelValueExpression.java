package io.joel.impl;

import io.joel.impl.node.AssignNode;
import io.joel.impl.node.ExpressionNode;
import io.joel.impl.node.IdentifierNode;
import io.joel.impl.node.LambdaNode;
import io.joel.impl.node.MemberNode;
import io.joel.impl.node.ObjectNode;
import io.joel.impl.node.StringNode;
import jakarta.el.ELContext;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class JoelValueExpression extends ValueExpression {
    private final String expression;
    private final ExpressionNode expressionNode;
    private final Class<?> expectedType;

    private JoelValueExpression(String expression, ExpressionNode expressionNode, Class<?> expectedType) {
        this.expression = expression;
        this.expressionNode = expressionNode;
        this.expectedType = expectedType;
    }

    public static JoelValueExpression newInstance(String expression, ExpressionNode expressionNode, Class<?> expectedType) {
        return new JoelValueExpression(expression, expressionNode, expectedType);
    }

    @Override
    public boolean isLiteralText() {
        return expressionNode instanceof StringNode;
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
            if (expressionNode instanceof LambdaNode lambdaNode && lambdaNode.parameters().isEmpty()) {
                return context.convertToType(lambdaNode.expression().getValue(context), expectedType);
            }
            return context.convertToType(expressionNode.getValue(context), expectedType);
        } finally {
            context.notifyAfterEvaluation(expression);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        if (expressionNode instanceof MemberNode memberNode) {
            var valueReference = memberNode.valueReference(context);
            boolean readOnly = context.getELResolver().isReadOnly(context, valueReference.getBase(), valueReference.getProperty());
            if (!context.isPropertyResolved())
                throw new PropertyNotFoundException();
            return readOnly;
        }
        if (expressionNode instanceof IdentifierNode identifierNode) {
            boolean readOnly = context.getELResolver().isReadOnly(context, null, identifierNode.value());
            if (!context.isPropertyResolved())
                throw new PropertyNotFoundException();
            return readOnly;
        }
        return true;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        context.setPropertyResolved(false);
        if (isReadOnly(context))
            throw new PropertyNotWritableException();
        new AssignNode(expressionNode, new ObjectNode(value)).getValue(context);
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        if (expressionNode instanceof MemberNode memberNode) {
            return memberNode.valueReference(context);
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressionNode, expectedType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoelValueExpression that = (JoelValueExpression) o;
        return Objects.equals(expressionNode, that.expressionNode) && Objects.equals(expectedType, that.expectedType);
    }

    @Override
    public String toString() {
        return expression;
    }

    @Serial
    private Object writeReplace() {
        return new SerializedValueExpression(expression, expressionNode, expectedType);
    }

    @Serial
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private record SerializedValueExpression(String expression, ExpressionNode expressionNode,
                                             Class<?> expectedType) implements Serializable {
        @Serial
        public Object readResolve() {
            return JoelValueExpression.newInstance(expression, expressionNode, expectedType);
        }
    }

}
