package dev.thihup.joel.impl;

import dev.thihup.joel.impl.node.ObjectNode;
import dev.thihup.joel.impl.node.AssignNode;
import dev.thihup.joel.impl.node.Node;
import dev.thihup.joel.impl.node.IdentifierNode;
import dev.thihup.joel.impl.node.LambdaNode;
import dev.thihup.joel.impl.node.MemberNode;
import dev.thihup.joel.impl.node.StringNode;
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

    @Serial
    private static final long serialVersionUID = 2278002437607003601L;

    private final String expression;
    private final Node node;
    private final Class<?> expectedType;

    private JoelValueExpression(String expression, Node node, Class<?> expectedType) {
        this.expression = expression;
        this.node = node;
        this.expectedType = expectedType;
    }

    public static JoelValueExpression newInstance(String expression, Node node, Class<?> expectedType) {
        return new JoelValueExpression(expression, node, expectedType);
    }

    @Override
    public boolean isLiteralText() {
        return node instanceof StringNode;
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
        return node.getType(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(ELContext context) {
        try {
            context.notifyBeforeEvaluation(expression);
            if (node instanceof LambdaNode lambdaNode && lambdaNode.parameters().isEmpty()) {
                return (T) context.convertToType(lambdaNode.expression().getValue(context), expectedType);
            }
            return (T) context.convertToType(node.getValue(context), expectedType);
        } finally {
            context.notifyAfterEvaluation(expression);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        if (node instanceof MemberNode memberNode) {
            var valueReference = memberNode.valueReference(context);
            boolean readOnly = context.getELResolver().isReadOnly(context, valueReference.getBase(), valueReference.getProperty());
            if (!context.isPropertyResolved())
                throw new PropertyNotFoundException();
            return readOnly;
        }
        if (node instanceof IdentifierNode identifierNode) {
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
        new AssignNode(node, new ObjectNode(value)).getValue(context);
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        if (node instanceof MemberNode memberNode) {
            return memberNode.valueReference(context);
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, expectedType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoelValueExpression that = (JoelValueExpression) o;
        return Objects.equals(node, that.node) && Objects.equals(expectedType, that.expectedType);
    }

    @Override
    public String toString() {
        return expression;
    }

    @Serial
    private Object writeReplace() {
        return new SerializedValueExpression(expression, node, expectedType);
    }

    @Serial
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private record SerializedValueExpression(String expression, Node node,
                                             Class<?> expectedType) implements Serializable {
        @Serial
        public Object readResolve() {
            return JoelValueExpression.newInstance(expression, node, expectedType);
        }
    }

}
