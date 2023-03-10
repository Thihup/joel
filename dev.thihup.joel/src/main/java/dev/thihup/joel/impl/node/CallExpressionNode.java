package dev.thihup.joel.impl.node;

import jakarta.el.*;

import java.io.Serial;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CallExpressionNode implements Node {
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("serial")
    private final Node callee;
    @SuppressWarnings("serial")
    private final List<? extends Node> arguments;
    private final transient Map<String, Method> resolvedFunction = new HashMap<>();

    public CallExpressionNode(Node callee, List<? extends Node> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }

    @Override
    public Object getValue(ELContext context) {
        if (callee instanceof CallExpressionNode callNode) {
            Object value = callNode.getValue(context);
            if (value instanceof Node node) {
                return node.getValue(context);
            }
            if (value instanceof LambdaExpression lambdaExpression) {
                return lambdaExpression.invoke(context, arguments.stream().map(x -> x.getValue(context)).toArray());
            }
            return value;
        }
        if (callee instanceof LambdaNode lambdaNode) {
            return ((LambdaExpression) lambdaNode.getValue(context)).invoke(context, arguments.stream().map(x -> x.getValue(context)).toArray());
        }
        if ((callee instanceof MemberNode memberNode)) {
            var valueReference = memberNode.valueReference(context);
            var objects = arguments.stream().map(x -> x.getValue(context)).toArray();
            return context.getELResolver()
                    .invoke(context, valueReference.getBase(), valueReference.getProperty(), null, objects);
        }
        if (callee instanceof IdentifierNode identifierNode) {
            Method method = resolvedFunction.computeIfAbsent(identifierNode.value(), functionName -> getMethod(context, functionName));
            if (method != null) {
                return invokeFunction(context, method);
            }
            // TODO where is the best place to reinvoke the lambda
            if (identifierNode.getValue(context) instanceof LambdaExpression lambdaExpression) {
                return lambdaExpression.invoke(context, arguments.stream().map(x -> x.getValue(context)).toArray());
            }

        }
        throw new UnsupportedOperationException();
    }

    private Object invokeFunction(ELContext context, Method method) {
        if (method.getParameterCount() != arguments.size()) {
            throw new MethodNotFoundException("Method " + method.getName() + " requires " + method.getParameterCount() + "(" + Arrays.toString(method.getParameterTypes()) + ")," +
                    "but it was supplied " + Arrays.toString(arguments.stream().map(x -> x.getValue(context)).toArray()));
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] objects = IntStream.range(0, arguments.size())
                .mapToObj(x -> convertArgument(context, parameterTypes[x], arguments.get(x).getValue(context)))
                .toArray();
        try {
            return method.invoke(null, objects);
        } catch (Throwable exception) {
            throw new ELException(exception);
        }
    }

    private static Method getMethod(ELContext context, String functionName) {
        var functionMapper = context.getFunctionMapper();
        if (functionMapper == null ) {
            throw new ELException("Function mapper not defined");
        }
        String[] split = functionName.split(":");
        String namespace = split.length == 1 ? "" : split[0] ;
        String function = split.length == 1 ? split[0] : split[1];
        return functionMapper.resolveFunction(namespace, function);
    }

    private Object convertArgument(ELContext context, Class<?> parameterType, Object value) {
        if (value == null)
            return null;
        if (value instanceof LambdaExpression lambdaExpression && parameterType.getAnnotation(FunctionalInterface.class) != null) {
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{parameterType},
                    (proxy, method, args) -> lambdaExpression.invoke(context, args));
        }
        if (!parameterType.isArray()) {
            return context.convertToType(value, parameterType);
        }
        Class<?> componentType = parameterType.componentType();
        try {
            int length = Array.getLength(value);
            Object newArray = Array.newInstance(componentType, length);
            IntStream.range(0, length).forEach(i -> {
                Object element = Array.get(value, i);
                Array.set(newArray, i, context.convertToType(element, componentType));
            });
            return newArray;
        } catch (IllegalArgumentException e) {
            throw new ELException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    @Override
    public String prettyPrint() {
        return "%s(%s)".formatted(callee.prettyPrint(), arguments.stream().map(Node::prettyPrint).collect(Collectors.joining(",")));
    }

    public Node callee() {
        return callee;
    }

    public List<? extends Node> arguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CallExpressionNode) obj;
        return Objects.equals(this.callee, that.callee) &&
                Objects.equals(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callee, arguments);
    }

    @Override
    public String toString() {
        return "CallExpressionNode[" +
                "callee=" + callee + ", " +
                "arguments=" + arguments + ']';
    }

}
