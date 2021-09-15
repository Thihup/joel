package io.joel.impl;

import io.joel.impl.node.Node;
import io.joel.impl.node.MemberNode;
import io.joel.impl.node.StringNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.MethodNotFoundException;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class JoelMethodExpression extends MethodExpression {
    private final Node node;
    private final Class<?> expectedReturnType;
    private final Class<?>[] expectedParameterTypes;
    private final String expression;

    private JoelMethodExpression(String expression, Node node, Class<?> expectedReturnType, Class<?>[] expectedParameterTypes) {
        this.expression = expression;
        this.node = node;
        this.expectedReturnType = expectedReturnType;
        this.expectedParameterTypes = expectedParameterTypes;
    }

    public static JoelMethodExpression newInstance(String expression, Node node, Class<?> expectedReturnType, Class<?>[] expectedParameterTypes) {
        return new JoelMethodExpression(expression, node, expectedReturnType, expectedParameterTypes);
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
    public MethodInfo getMethodInfo(ELContext context) {
        if (node instanceof StringNode stringLiteral)
            return new MethodInfo(stringLiteral.value(), expectedReturnType, expectedParameterTypes);
        if (node instanceof MemberNode memberNode) {
            var valueReference = memberNode.valueReference(context);
            try {
                Object base = valueReference.getBase();
                var property = valueReference.getProperty().toString();
                var method = base.getClass().getMethod(property, expectedParameterTypes);
                return new MethodInfo(property, method.getReturnType(), method.getParameterTypes());
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new MethodNotFoundException(noSuchMethodException);
            }
        }
        return null;
    }

    @Override
    public Object invoke(ELContext context, Object[] params) {
        try {
            context.notifyBeforeEvaluation(expression);
            if (node instanceof StringNode stringLiteral) {
                return context.convertToType(stringLiteral.value(), expectedReturnType);
            }
            if (node instanceof MemberNode memberNode) {
                var valueReference = memberNode.valueReference(context);
                try {
                    Object base = valueReference.getBase();
                    var property = valueReference.getProperty().toString();
                    var method = base.getClass().getMethod(property, expectedParameterTypes);
                    return method.invoke(base, params);
                } catch (NoSuchMethodException noSuchMethodException) {
                    throw new MethodNotFoundException(noSuchMethodException);
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new ELException(exception);
                }
            }
            return null;
        } finally {
            context.notifyAfterEvaluation(expression);
        }
    }

    @Override
    public boolean isParametersProvided() {
        return super.isParametersProvided();
    }

    @Override
    public boolean isParmetersProvided() {
        return isParametersProvided();
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(node, expectedReturnType);
        result = 31 * result + Arrays.hashCode(expectedParameterTypes);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoelMethodExpression that = (JoelMethodExpression) o;
        return Objects.equals(node, that.node)
                && Arrays.equals(expectedParameterTypes, that.expectedParameterTypes)
                && Objects.equals(expectedReturnType, that.expectedReturnType);
    }

    @Serial
    private Object writeReplace() {
        return new SerializedMethodExpression(expression, node, expectedReturnType, List.of(expectedParameterTypes));
    }

    @Serial
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private record SerializedMethodExpression(String expression, Node node,
                                              Class<?> expectedReturnType,
                                              List<Class<?>> expectedParameterTypes) implements Serializable {
        @Serial
        public Object readResolve() {
            return JoelMethodExpression.newInstance(expression, node, expectedReturnType, expectedParameterTypes.toArray(Class[]::new));
        }
    }

}
