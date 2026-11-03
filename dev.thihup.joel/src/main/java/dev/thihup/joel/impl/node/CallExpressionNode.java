package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.LambdaExpression;
import jakarta.el.MethodNotFoundException;

import java.io.Serial;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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
    private final transient Map<String, MethodHandle> resolvedFunction = new HashMap<>();

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
            if (context.getFunctionMapper() != null
                    && context.getFunctionMapper().resolveFunction("", identifierNode.value()) != null) {
                return resolveQualifiedFunction(context, identifierNode);
            }
            if (identifierNode.getValue(context) instanceof LambdaExpression lambdaExpression) {
                return lambdaExpression.invoke(context, arguments.stream().map(x -> x.getValue(context)).toArray());
            }
            return resolveQualifiedFunction(context, identifierNode);
        }
        throw new UnsupportedOperationException();
    }

    private Object resolveQualifiedFunction(ELContext context, IdentifierNode identifierNode) {
        var functionMapper = context.getFunctionMapper();
        if (functionMapper == null) {
            throw new UnsupportedOperationException();
        }
        var methodHandle = resolvedFunction.computeIfAbsent(identifierNode.value(), key -> {
            String[] split = identifierNode.value().split(":", 2);
            var method = split.length == 1
                    ? functionMapper.resolveFunction("", split[0])
                    : functionMapper.resolveFunction(split[0], split[1]);
            if (method == null) {
                throw new UnsupportedOperationException();
            }

            if (method.getParameterCount() != arguments.size())
                throw new MethodNotFoundException("Method " + method.getName() + " requires " + method.getParameterCount() + "(" + Arrays.toString(method.getParameterTypes()) + ")," +
                        "but it was supplied " + Arrays.toString(arguments.stream().map(x -> x.getValue(context)).toArray()));

            Object[] objects = IntStream.range(0, arguments.size())
                    .mapToObj(x -> convertArgument(context, arguments.get(x).getValue(context), method.getParameterTypes()[x]))
                    .toArray();
            try {
                return MethodHandles.lookup().unreflect(method).asSpreader(0, Object[].class, 1).bindTo(objects);
            } catch (IllegalAccessException exception) {
                throw new ELException(exception);
            }
        });
        try {
            if (methodHandle != null)
                return methodHandle.invoke();
        } catch (Throwable exception) {
            sneakyThrow(exception);
        }
        throw new UnsupportedOperationException();
    }

    private static Object convertArgument(ELContext context, Object value, Class<?> targetType) {
        if (value instanceof LambdaExpression lambda && targetType.isInterface()) {
            Method abstractMethod = Arrays.stream(targetType.getMethods())
                    .filter(candidate -> !candidate.isDefault() && candidate.getDeclaringClass() != Object.class)
                    .findFirst()
                    .orElseThrow(() -> new MethodNotFoundException("No functional method in " + targetType));
            return Proxy.newProxyInstance(targetType.getClassLoader(), new Class<?>[]{targetType}, (proxy, method, args) -> {
                if (method.equals(abstractMethod)) {
                    Object result = lambda.invoke(context, args == null ? new Object[0] : args);
                    return method.getReturnType() == void.class ? null : context.convertToType(result, method.getReturnType());
                }
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(lambda, args);
                }
                return method.isDefault() ? method.invoke(proxy, args) : null;
            });
        }
        return context.convertToType(value, targetType);
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
