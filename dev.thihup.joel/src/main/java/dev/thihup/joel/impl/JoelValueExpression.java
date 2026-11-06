package dev.thihup.joel.impl;

import dev.thihup.joel.impl.node.NodeEvaluator;
import dev.thihup.joel.impl.node.AssignNode;
import dev.thihup.joel.impl.node.IdentifierNode;
import dev.thihup.joel.impl.node.LambdaNode;
import dev.thihup.joel.impl.node.MemberNode;
import dev.thihup.joel.impl.node.Node;
import dev.thihup.joel.impl.node.ObjectNode;
import dev.thihup.joel.impl.node.StringNode;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import jakarta.el.VariableMapper;

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
    @SuppressWarnings("serial")
    private final FunctionMapper functionMapper;
    @SuppressWarnings("serial")
    private final VariableMapper variableMapper;

    private JoelValueExpression(String expression, Node node, Class<?> expectedType,
                                FunctionMapper functionMapper, VariableMapper variableMapper) {
        this.expression = expression;
        this.node = node;
        this.expectedType = expectedType;
        this.functionMapper = functionMapper;
        this.variableMapper = variableMapper;
    }

    public static JoelValueExpression newInstance(String expression, Node node, Class<?> expectedType,
                                                  FunctionMapper functionMapper, VariableMapper variableMapper) {
        return new JoelValueExpression(expression, node, expectedType, functionMapper, variableMapper);
    }

    public static JoelValueExpression newInstance(String expression, Node node, Class<?> expectedType) {
        return new JoelValueExpression(expression, node, expectedType, null, null);
    }

    private ELContext wrapContext(ELContext context) {
        if (context == null) {
            return null;
        }
        if ((functionMapper != null && context.getFunctionMapper() != functionMapper)
                || (variableMapper != null && context.getVariableMapper() != variableMapper)) {
            return new JoelEvaluationContext(context, functionMapper, variableMapper);
        }
        return context;
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
        ELContext evalContext = wrapContext(context);
        boolean isStandalone = node instanceof IdentifierNode;
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, isStandalone ? Boolean.TRUE : Boolean.FALSE);
        return node.getType(evalContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(ELContext context) {
        ELContext evalContext = wrapContext(context);
        boolean isStandalone = node instanceof IdentifierNode;
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, isStandalone ? Boolean.TRUE : Boolean.FALSE);
        try {
            evalContext.notifyBeforeEvaluation(expression);
            Object val;
            if (node instanceof LambdaNode lambdaNode && lambdaNode.parameters().isEmpty()) {
                val = NodeEvaluator.eval(lambdaNode.expression(), evalContext);
            } else {
                val = NodeEvaluator.eval(node, evalContext);
            }
            if (expectedType == Object.class || (val != null && expectedType.isInstance(val))) {
                return (T) val;
            }
            return (T) evalContext.convertToType(val, expectedType);
        } finally {
            evalContext.notifyAfterEvaluation(expression);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        ELContext evalContext = wrapContext(context);
        boolean isStandalone = node instanceof IdentifierNode;
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, isStandalone ? Boolean.TRUE : Boolean.FALSE);
        if (node instanceof MemberNode memberNode) {
            var valueReference = memberNode.valueReference(evalContext);
            boolean readOnly = evalContext.getELResolver().isReadOnly(evalContext, valueReference.getBase(), valueReference.getProperty());
            if (!evalContext.isPropertyResolved())
                throw new PropertyNotFoundException();
            return readOnly;
        }
        if (node instanceof IdentifierNode identifierNode) {
            boolean readOnly = evalContext.getELResolver().isReadOnly(evalContext, null, identifierNode.value());
            if (!evalContext.isPropertyResolved())
                throw new PropertyNotFoundException();
            return readOnly;
        }
        return true;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        ELContext evalContext = wrapContext(context);
        boolean isStandalone = node instanceof IdentifierNode;
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, isStandalone ? Boolean.TRUE : Boolean.FALSE);
        evalContext.setPropertyResolved(false);
        if (isReadOnly(evalContext))
            throw new PropertyNotWritableException();
        NodeEvaluator.eval(new AssignNode(node, new ObjectNode(value)), evalContext);
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        ELContext evalContext = wrapContext(context);
        boolean isStandalone = node instanceof IdentifierNode;
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, isStandalone ? Boolean.TRUE : Boolean.FALSE);
        if (node instanceof MemberNode memberNode) {
            return memberNode.valueReference(evalContext);
        }
        if (node instanceof IdentifierNode identifierNode) {
            return new ValueReference(null, identifierNode.value());
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

    private value record SerializedValueExpression(String expression, Node node,
                                             Class<?> expectedType) implements Serializable {
        @Serial
        public Object readResolve() {
            return JoelValueExpression.newInstance(expression, node, expectedType);
        }
    }

}
