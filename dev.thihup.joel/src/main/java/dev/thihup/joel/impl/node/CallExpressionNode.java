package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.FunctionMapper;
import jakarta.el.LambdaExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CallExpressionNode implements Node {
    @Serial
    private static final long serialVersionUID = 0L;
    private static final Object[] EMPTY_ARGS = new Object[0];

    @SuppressWarnings("serial")
    private final Node callee;
    @SuppressWarnings("serial")
    private final List<? extends Node> arguments;

    private transient volatile Method cachedMethod;
    private transient volatile Class<?>[] cachedParamTypes;
    private transient volatile FunctionMapper cachedFunctionMapper;

    public CallExpressionNode(Node callee, List<? extends Node> arguments) {
        this.callee = callee;
        this.arguments = arguments;
    }

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

        Method method = this.cachedMethod;
        Class<?>[] paramTypes = this.cachedParamTypes;
        if (method == null || this.cachedFunctionMapper != functionMapper) {
            method = functionMapper.resolveFunction(prefix, localName);
            if (method == null) {
                throw new MethodNotFoundException("Function '%s' not found".formatted(identifierNode.value()));
            }
            try {
                method.trySetAccessible();
            } catch (Exception ignored) {
            }
            paramTypes = method.getParameterTypes();
            this.cachedMethod = method;
            this.cachedParamTypes = paramTypes;
            this.cachedFunctionMapper = functionMapper;
        }

        return invokeResolvedMethod(context, method, paramTypes);
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
            Method method = context.getFunctionMapper().resolveFunction(prefix, localName);
            if (method != null) {
                return method.getReturnType();
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
