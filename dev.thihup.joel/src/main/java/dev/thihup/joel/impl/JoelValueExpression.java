package dev.thihup.joel.impl;

import dev.thihup.joel.impl.compiler.AstBytecodeCompiler;
import dev.thihup.joel.impl.compiler.AstMethodHandleCompiler;
import dev.thihup.joel.impl.compiler.CompiledExpression;
import dev.thihup.joel.impl.node.AssignNode;
import dev.thihup.joel.impl.node.IdentifierNode;
import dev.thihup.joel.impl.node.LambdaNode;
import dev.thihup.joel.impl.node.MemberNode;
import dev.thihup.joel.impl.node.Node;
import dev.thihup.joel.impl.node.ObjectNode;
import dev.thihup.joel.impl.node.*;
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

    private static final boolean BYTECODE_ENABLED = Boolean.parseBoolean(System.getProperty("joel.bytecode.enabled", "true"));

    private final String expression;
    private final Node node;
    private final Class<?> expectedType;
    @SuppressWarnings("serial")
    private final FunctionMapper functionMapper;
    @SuppressWarnings("serial")
    private final VariableMapper variableMapper;
    private final Boolean standaloneMarker;

    private transient volatile CompiledExpression compiled;
    private transient volatile boolean compilationAttempted;
    private transient int invocationCount;

    private JoelValueExpression(String expression, Node node, Class<?> expectedType,
                                FunctionMapper functionMapper, VariableMapper variableMapper) {
        this.expression = expression;
        this.node = node;
        this.expectedType = expectedType;
        this.functionMapper = functionMapper;
        this.variableMapper = variableMapper;
        this.standaloneMarker = (node instanceof IdentifierNode) ? Boolean.TRUE : Boolean.FALSE;
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
        if (node instanceof ObjectNode obj) {
            return obj.value() != null ? obj.value().getClass() : expectedType;
        }
        ELContext evalContext = wrapContext(context);
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, standaloneMarker);
        return node.getType(evalContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(ELContext context) {
        if (node instanceof ObjectNode obj) {
            Object val = obj.value();
            if (expectedType == Object.class || (val != null && expectedType.isInstance(val))) {
                return (T) val;
            }
            ELContext evalContext = wrapContext(context);
            return (T) evalContext.convertToType(val, expectedType);
        }
        ELContext evalContext = wrapContext(context);
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, standaloneMarker);
        try {
            evalContext.notifyBeforeEvaluation(expression);
            Object val;
            CompiledExpression comp = this.compiled;
            if (comp != null) {
                try {
                    val = comp.evaluate(evalContext);
                } catch (Throwable t) {
                    val = evalAst(evalContext);
                }
            } else {
                if (!compilationAttempted && BYTECODE_ENABLED && !(node instanceof NumberNode || node instanceof StringNode || node instanceof BooleanNode || node instanceof NullNode || node instanceof ObjectNode)) {
                    if (++invocationCount >= 2) {
                        synchronized (this) {
                            if (!compilationAttempted) {
                                compilationAttempted = true;
                                try {
                                    try {
                                        this.compiled = AstBytecodeCompiler.compileToBytecode(node);
                                    } catch (Throwable t) {
                                        this.compiled = AstMethodHandleCompiler.compileToHiddenClass(node);
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                        comp = this.compiled;
                        if (comp != null) {
                            try {
                                val = comp.evaluate(evalContext);
                            } catch (Throwable t) {
                                val = evalAst(evalContext);
                            }
                        } else {
                            val = evalAst(evalContext);
                        }
                    } else {
                        val = evalAst(evalContext);
                    }
                } else {
                    val = evalAst(evalContext);
                }
            }
            if (expectedType == Object.class || (val != null && expectedType.isInstance(val))) {
                return (T) val;
            }
            return (T) evalContext.convertToType(val, expectedType);
        } finally {
            evalContext.notifyAfterEvaluation(expression);
        }
    }

    private Object evalAst(ELContext evalContext) {
        if (node instanceof LambdaNode lambdaNode && lambdaNode.parameters().isEmpty()) {
            return lambdaNode.expression().getValue(evalContext);
        }
        return node.getValue(evalContext);
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        if (node instanceof ObjectNode) {
            return true;
        }
        ELContext evalContext = wrapContext(context);
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, standaloneMarker);
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
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, standaloneMarker);
        evalContext.setPropertyResolved(false);
        if (isReadOnly(evalContext))
            throw new PropertyNotWritableException();
        new AssignNode(node, new ObjectNode(value)).getValue(evalContext);
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        if (node instanceof ObjectNode) {
            return null;
        }
        ELContext evalContext = wrapContext(context);
        evalContext.putContext(ELResolver.StandaloneIdentifierMarker.class, standaloneMarker);
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

    private record SerializedValueExpression(String expression, Node node,
                                             Class<?> expectedType) implements Serializable {
        @Serial
        public Object readResolve() {
            return JoelValueExpression.newInstance(expression, node, expectedType);
        }
    }

}
