package dev.thihup.joel.impl;

import dev.thihup.joel.impl.node.MemberNode;
import dev.thihup.joel.impl.node.Node;
import dev.thihup.joel.impl.node.StringNode;
import dev.thihup.joel.impl.node.NodeEvaluator;
import dev.thihup.joel.impl.node.CallExpressionNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.FunctionMapper;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.MethodNotFoundException;
import jakarta.el.MethodReference;
import jakarta.el.VariableMapper;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class JoelMethodExpression extends MethodExpression {

    @Serial
    private static final long serialVersionUID = 165770279277199402L;

    private final Node node;
    private final Class<?> expectedReturnType;
    private final Class<?>[] expectedParameterTypes;
    private final String expression;
    @SuppressWarnings("serial")
    private final FunctionMapper functionMapper;
    @SuppressWarnings("serial")
    private final VariableMapper variableMapper;
    private transient volatile MethodInfo resolvedMethodInfo;

    private JoelMethodExpression(String expression, Node node, Class<?> expectedReturnType,
                                 Class<?>[] expectedParameterTypes,
                                 FunctionMapper functionMapper, VariableMapper variableMapper) {
        this.expression = expression;
        this.node = node;
        this.expectedReturnType = expectedReturnType;
        this.expectedParameterTypes = expectedParameterTypes;
        this.functionMapper = functionMapper;
        this.variableMapper = variableMapper;
    }

    public static JoelMethodExpression newInstance(String expression, Node node, Class<?> expectedReturnType,
                                                   Class<?>[] expectedParameterTypes,
                                                   FunctionMapper functionMapper, VariableMapper variableMapper) {
        return new JoelMethodExpression(expression, node, expectedReturnType, expectedParameterTypes, functionMapper, variableMapper);
    }

    public static JoelMethodExpression newInstance(String expression, Node node, Class<?> expectedReturnType,
                                                   Class<?>[] expectedParameterTypes) {
        return new JoelMethodExpression(expression, node, expectedReturnType, expectedParameterTypes, null, null);
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
    public MethodInfo getMethodInfo(ELContext context) {
        ELContext evalContext = wrapContext(context);
        if (node instanceof StringNode stringLiteral)
            return new MethodInfo(stringLiteral.value(), expectedReturnType, expectedParameterTypes);
        if (node instanceof CallExpressionNode) {
            MethodTarget target = resolveMethodReference(evalContext);
            if (resolvedMethodInfo == null) {
                resolvedMethodInfo = new MethodInfo(target.method().getName(), target.method().getReturnType(),
                        target.method().getParameterTypes());
            }
            return resolvedMethodInfo;
        }
        if (node instanceof MemberNode memberNode) {
            var valueReference = memberNode.valueReference(evalContext);
            Object base = valueReference.getBase();
            var property = valueReference.getProperty().toString();
            try {
                var method = base.getClass().getMethod(property, expectedParameterTypes);
                if (resolvedMethodInfo == null) {
                    resolvedMethodInfo = new MethodInfo(property, method.getReturnType(), method.getParameterTypes());
                }
                return resolvedMethodInfo;
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new MethodNotFoundException(noSuchMethodException);
            }
        }
        throw new MethodNotFoundException(expression);
    }

    @Override
    public MethodReference getMethodReference(ELContext context) {
        Objects.requireNonNull(context);
        ELContext evalContext = wrapContext(context);
        if (node instanceof StringNode) {
            return super.getMethodReference(evalContext);
        }
        MethodTarget target = resolveMethodReference(evalContext);
        if (target == null) {
            return null;
        }
        Object[] parameters = target.evaluatedArguments();
        Method method = target.method();
        return new MethodReference(target.base(),
                getMethodInfo(evalContext),
                method.getAnnotations(), parameters);
    }

    @Override
    public Object invoke(ELContext context, Object[] params) {
        ELContext evalContext = wrapContext(context);
        try {
            evalContext.notifyBeforeEvaluation(expression);
            if (node instanceof StringNode stringLiteral) {
                return evalContext.convertToType(stringLiteral.value(), expectedReturnType);
            }
            if (node instanceof CallExpressionNode) {
                MethodTarget target = resolveMethodReference(evalContext);
                try {
                    return target.method().invoke(target.base(), target.arguments());
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new ELException(exception);
                }
            }
            if (node instanceof MemberNode memberNode) {
                var valueReference = memberNode.valueReference(evalContext);
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
            evalContext.notifyAfterEvaluation(expression);
        }
    }

    private MethodTarget resolveMethodReference(ELContext context) {
        if (!(node instanceof CallExpressionNode call)
                || !(call.callee() instanceof MemberNode memberNode)) {
            throw new MethodNotFoundException(expression);
        }

        var valueReference = memberNode.valueReference(context);
        Object base = valueReference.getBase();
        if (base == null) {
            throw new jakarta.el.PropertyNotFoundException("Base object is null");
        }
        String property = valueReference.getProperty().toString();
        Object[] originalArguments = call.arguments().stream()
                .map(argument -> NodeEvaluator.eval(argument, context)).toArray();
        MethodTarget best = null;
        for (Method method : base.getClass().getMethods()) {
            if (!method.getName().equals(property)) {
                continue;
            }
            MethodTarget candidate = match(base, method, originalArguments, context);
            if (candidate != null && (best == null || candidate.score() < best.score())) {
                best = candidate;
            } else if (candidate != null && candidate.score() == best.score()) {
                throw new MethodNotFoundException("Ambiguous method: " + property);
            }
        }
        if (best == null) {
            throw new MethodNotFoundException(property);
        }
        return best;
    }

    private MethodTarget match(Object base, Method method, Object[] arguments, ELContext context) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean varargs = method.isVarArgs();
        if ((!varargs && parameterTypes.length != arguments.length)
                || (varargs && arguments.length < parameterTypes.length - 1)) {
            return null;
        }
        Object[] converted = new Object[arguments.length];
        int score = varargs ? 100 : 0;
        for (int i = 0; i < arguments.length; i++) {
            Class<?> targetType = varargs && i >= parameterTypes.length - 1
                    ? parameterTypes[parameterTypes.length - 1].getComponentType()
                    : parameterTypes[i];
            Object argument = arguments[i];
            if (argument == null) {
                if (targetType.isPrimitive()) {
                    return null;
                }
                converted[i] = null;
                continue;
            }
            Class<?> argumentType = argument.getClass();
            if (wrap(targetType).equals(argumentType)) {
                converted[i] = argument;
            } else if (wrap(targetType).isAssignableFrom(argumentType)) {
                converted[i] = argument;
                score++;
            } else {
                try {
                    converted[i] = context.convertToType(argument, targetType);
                    score += 10;
                } catch (RuntimeException exception) {
                    return null;
                }
            }
        }
        if (varargs) {
            Object array = java.lang.reflect.Array.newInstance(
                    parameterTypes[parameterTypes.length - 1].getComponentType(),
                    arguments.length - parameterTypes.length + 1);
            for (int i = parameterTypes.length - 1; i < arguments.length; i++) {
                java.lang.reflect.Array.set(array, i - parameterTypes.length + 1, converted[i]);
            }
            Object[] invocationArguments = Arrays.copyOf(converted, parameterTypes.length);
            invocationArguments[parameterTypes.length - 1] = array;
            converted = invocationArguments;
        }
        return new MethodTarget(base, method, converted, score, arguments);
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private value record MethodTarget(Object base, Method method, Object[] arguments, int score, Object[] evaluatedArguments) {
    }

    @Override
    public boolean isParametersProvided() {
        return super.isParametersProvided();
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

    private value record SerializedMethodExpression(String expression, Node node,
                                              Class<?> expectedReturnType,
                                              List<Class<?>> expectedParameterTypes) implements Serializable {
        @Serial
        public Object readResolve() {
            return JoelMethodExpression.newInstance(expression, node, expectedReturnType, expectedParameterTypes.toArray(Class[]::new));
        }
    }

}
