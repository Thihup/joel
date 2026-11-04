package dev.thihup.joel.impl.node;

import dev.thihup.joel.impl.FunctionResolutionCache;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.LambdaExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record CallExpressionNode(Node callee, List<? extends Node> arguments) implements Node {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private Object[] evaluateArguments(ELContext context) {
        int size = arguments.size();
        if (size == 0) return EMPTY_ARGS;
        Object[] args = new Object[size];
        for (int i = 0; i < size; i++) {
            args[i] = arguments.get(i).getValue(context);
        }
        return args;
    }

    @Override
    public Object getValue(ELContext context) {
        if (callee instanceof CallExpressionNode callNode) {
            Object value = callNode.getValue(context);
            if (value instanceof Node node) {
                return node.getValue(context);
            }
            if (value instanceof LambdaExpression lambdaExpression) {
                return lambdaExpression.invoke(context, evaluateArguments(context));
            }
            return value;
        }
        if (callee instanceof LambdaNode lambdaNode) {
            return ((LambdaExpression) lambdaNode.getValue(context)).invoke(context, evaluateArguments(context));
        }
        if ((callee instanceof MemberNode memberNode)) {
            Object base = memberNode.getBase(context);
            Object prop = memberNode.getProperty(context);
            var objects = evaluateArguments(context);
            return context.getELResolver()
                    .invoke(context, base, prop, null, objects);
        }
        if (callee instanceof IdentifierNode identifierNode) {
            String idValue = identifierNode.value();
            int colon = idValue.indexOf(':');
            String prefix = colon < 0 ? "" : idValue.substring(0, colon);
            String localName = colon < 0 ? idValue : idValue.substring(colon + 1);

            if (colon >= 0) {
                return resolveQualifiedFunction(context, identifierNode, prefix, localName);
            }
            if (context.getFunctionMapper() != null
                    && context.getFunctionMapper().resolveFunction("", idValue) != null) {
                return resolveQualifiedFunction(context, identifierNode, "", idValue);
            }
            try {
                if (identifierNode.getValue(context) instanceof LambdaExpression lambdaExpression) {
                    return lambdaExpression.invoke(context, evaluateArguments(context));
                }
            } catch (PropertyNotFoundException ignored) {
            }
            return resolveQualifiedFunction(context, identifierNode, prefix, localName);
        }
        throw new UnsupportedOperationException();
    }

    private Object resolveQualifiedFunction(ELContext context, IdentifierNode identifierNode, String prefix, String localName) {
        var functionMapper = context.getFunctionMapper();
        if (functionMapper == null) {
            throw new MethodNotFoundException("Function '%s' not found: no FunctionMapper available".formatted(identifierNode.value()));
        }

        var resolved = FunctionResolutionCache.resolve(functionMapper, prefix, localName);
        if (resolved == null) {
            throw new MethodNotFoundException("Function '%s' not found".formatted(identifierNode.value()));
        }

        return invokeResolvedMethod(context, resolved.method(), resolved.parameterTypes());
    }

    private Object invokeResolvedMethod(ELContext context, Method method, Class<?>[] paramTypes) {
        if (paramTypes.length != arguments.size()) {
            throw new MethodNotFoundException("Method " + method.getName() + " requires " + paramTypes.length + " parameters, but " + arguments.size() + " were supplied");
        }

        int size = arguments.size();
        Object[] objects = new Object[size];
        for (int i = 0; i < size; i++) {
            Object val = arguments.get(i).getValue(context);
            Class<?> targetType = paramTypes[i];
            if (val != null && (targetType == Object.class || targetType.isInstance(val))) {
                objects[i] = val;
            } else {
                objects[i] = convertArgument(context, val, targetType);
            }
        }

        try {
            return method.invoke(null, objects);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof ELException elException) {
                throw elException;
            }
            throw new ELException(cause);
        } catch (IllegalAccessException exception) {
            throw new ELException(exception);
        }
    }

    @Override
    public Class<?> getType(ELContext context) {
        if (callee instanceof IdentifierNode identifierNode && context != null && context.getFunctionMapper() != null) {
            String funcName = identifierNode.value();
            int colon = funcName.indexOf(':');
            String prefix = colon < 0 ? "" : funcName.substring(0, colon);
            String localName = colon < 0 ? funcName : funcName.substring(colon + 1);
            var resolved = FunctionResolutionCache.resolve(context.getFunctionMapper(), prefix, localName);
            if (resolved != null) {
                return resolved.method().getReturnType();
            }
        }
        return null;
    }

    private static Object convertArgument(ELContext context, Object value, Class<?> targetType) {
        if (value != null && (targetType == Object.class || targetType.isInstance(value))) {
            return value;
        }
        if (value instanceof LambdaExpression lambda && targetType.isInterface()) {
            Method abstractMethod = Arrays.stream(targetType.getMethods())
                    .filter(candidate -> !candidate.isDefault() && candidate.getDeclaringClass() != Object.class)
                    .findFirst()
                    .orElseThrow(() -> new MethodNotFoundException("No functional method in " + targetType));
            return Proxy.newProxyInstance(targetType.getClassLoader(), new Class<?>[]{targetType}, (proxy, method, args) -> {
                if (method.equals(abstractMethod)) {
                    Object result = lambda.invoke(context, args == null ? EMPTY_ARGS : args);
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

    @Override
    public String prettyPrint() {
        return "%s(%s)".formatted(callee.prettyPrint(), arguments.stream().map(Node::prettyPrint).collect(Collectors.joining(",")));
    }
}
