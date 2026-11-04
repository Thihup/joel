package dev.thihup.joel.impl;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.LambdaExpression;
import jakarta.el.MethodNotFoundException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("cast")
public final class StreamELResolver extends ELResolver {
    private static final MethodHandle LAMBDA_IMPL_WITH_RETURN;
    private static final MethodHandle LAMBDA_IMPL_WITHOUT_RETURN;
    private static final Method[] STREAM_METHODS = Stream.class.getMethods();

    static {
        try {
            var lookup = MethodHandles.lookup();
            LAMBDA_IMPL_WITH_RETURN = lookup.findStatic(StreamELResolver.class, "proxyWithReturnType", MethodType.methodType(Object.class, Class.class, ELContext.class, LambdaExpression.class, Object[].class));
            LAMBDA_IMPL_WITHOUT_RETURN = lookup.findStatic(StreamELResolver.class, "proxyWithoutReturnType", MethodType.methodType(void.class, ELContext.class, LambdaExpression.class, Object[].class));
        } catch (Throwable throwable) {
            throw new IllegalStateException(throwable);
        }
    }

    private static Object proxyWithReturnType(Class<?> returnType, ELContext context, LambdaExpression x, Object... args) {
        return context.convertToType(x.invoke(context, args), returnType);
    }

    private static void proxyWithoutReturnType(ELContext context, LambdaExpression x, Object... args) {
        x.invoke(context, args);
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return String.class;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
    }

    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] parameterTypes, Object[] params) {
        Objects.requireNonNull(context);
        if (base instanceof Optional<?> && method != null && method.toString().equals("orElseGet") && params != null && params.length > 0 && params[0] instanceof LambdaExpression lambda) {
            return executeOptionalOrElseGet(context, base, method, lambda);
        }
        if (method == null)
            return null;
        if (!(base instanceof Stream<?> stream))
            return null;
        try {
            String methodName = (String) method;

            switch (methodName) {
                case "substream" -> {
                    context.setPropertyResolved(base, method);
                    return executeSubstream(stream, params);
                }
                case "average" -> {
                    context.setPropertyResolved(base, method);
                    return executeAverage(context, stream);
                }
                case "sum" -> {
                    context.setPropertyResolved(base, method);
                    return executeSum(context, stream);
                }
                case "sorted", "min", "max" -> {
                    if (params == null || params.length == 0) {
                        params = new Object[]{Comparator.naturalOrder()};
                    }
                }
                default -> {
                }
            }

            Object[] currentParams = params;
            var method1 = findMethod(parameterTypes, methodName, currentParams);

            // toList() / toArray()
            if (method1.getParameterCount() == 0) {
                context.setPropertyResolved(base, method);
                return method1.invoke(base);
            }

            // sorted() / min() / max()
            if (params != null && params.length != 0) {
                boolean hasLambda = false;
                for (Object param : params) {
                    if (param instanceof LambdaExpression) {
                        hasLambda = true;
                        break;
                    }
                }
                if (!hasLambda) {
                    context.setPropertyResolved(base, method);
                    return method1.invoke(base, params);
                }
            }

            // parameters of stream method
            Class<?>[] parameterTypes1 = method1.getParameterTypes();

            Object[] objects = getArguments(context, currentParams, parameterTypes1);
            context.setPropertyResolved(base, method);
            return method1.invoke(base, objects);
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new MethodNotFoundException(noSuchMethodException);
        } catch (Throwable throwable) {
            throw new ELException(throwable);
        }
    }

    @SuppressWarnings("unchecked")
    private Object executeOptionalOrElseGet(ELContext context, Object base, Object method, LambdaExpression lambda) {
        context.setPropertyResolved(base, method);
        return ((Optional<Object>) base).orElseGet(() -> lambda.invoke(context));
    }

    private Object[] getArguments(ELContext context, Object[] currentParams, Class<?>[] parameterTypes1) {
        Object[] array = new Object[parameterTypes1.length];
        for (int x = 0; x < parameterTypes1.length; x++) {
            try {
                array[x] = parameterTypes1[x].isInterface() && currentParams[x] instanceof LambdaExpression lambda ?
                        createLambdaFromLambdaExpression(context, lambda, parameterTypes1[x], findMethodFromClass(parameterTypes1[x]))
                        : currentParams[x];
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new ELException(noSuchMethodException);
            }
        }
        return array;
    }

    private Method findMethod(Class<?>[] parameterTypes, String methodName, Object[] currentParams) throws NoSuchMethodException {
        int paramCount = currentParams == null ? 0 : currentParams.length;
        for (Method m : STREAM_METHODS) {
            if (!Modifier.isStatic(m.getModifiers())
                    && m.getName().equals(methodName)
                    && m.getParameterCount() == paramCount) {
                if (parameterTypes == null || Arrays.equals(parameterTypes, m.getParameterTypes())) {
                    return m;
                }
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object createLambdaFromLambdaExpression(ELContext context, LambdaExpression lambdaExpression, Class<?> aClass, Method method) {
        if (aClass == java.util.function.Predicate.class) {
            return (java.util.function.Predicate<Object>) t -> {
                Object res = lambdaExpression.invoke(context, t);
                if (res instanceof Boolean b) return b;
                return context.convertToType(res, Boolean.class);
            };
        }
        if (aClass == java.util.function.Function.class) {
            return (java.util.function.Function<Object, Object>) t -> lambdaExpression.invoke(context, t);
        }
        if (aClass == java.util.function.Consumer.class) {
            return (java.util.function.Consumer<Object>) t -> lambdaExpression.invoke(context, t);
        }
        if (aClass == java.util.function.Supplier.class) {
            return (java.util.function.Supplier<Object>) () -> lambdaExpression.invoke(context);
        }
        if (aClass == java.util.function.BinaryOperator.class || aClass == java.util.function.BiFunction.class) {
            return (java.util.function.BinaryOperator<Object>) (t, u) -> lambdaExpression.invoke(context, t, u);
        }
        if (aClass == java.util.Comparator.class) {
            return (java.util.Comparator<Object>) (t, u) -> {
                Object res = lambdaExpression.invoke(context, t, u);
                if (res instanceof Number n) return n.intValue();
                return context.convertToType(res, Integer.class);
            };
        }
        if (method.getReturnType() == void.class)
            return MethodHandleProxies.asInterfaceInstance(aClass, LAMBDA_IMPL_WITHOUT_RETURN.bindTo(context).bindTo(lambdaExpression).asVarargsCollector(Object[].class));
        return MethodHandleProxies.asInterfaceInstance(aClass, LAMBDA_IMPL_WITH_RETURN.bindTo(method.getReturnType()).bindTo(context).bindTo(lambdaExpression).asVarargsCollector(Object[].class));
    }

    private Method findMethodFromClass(Class<?> klass) throws NoSuchMethodException {
        for (Method m : klass.getMethods()) {
            if (!m.isDefault() && !Modifier.isStatic(m.getModifiers())) {
                String name = m.getName();
                if (!name.equals("equals") && !name.equals("hashCode") && !name.equals("toString")) {
                    return m;
                }
            }
        }
        throw new NoSuchMethodException(klass.getName());
    }

    private Object executeSum(ELContext context, Stream<?> base) {
        return base.mapToLong(x -> {
            if (x instanceof Long l) return l;
            if (x instanceof Number n) return n.longValue();
            return (Long) context.convertToType(x, Long.class);
        }).sum();
    }

    private Object executeAverage(ELContext context, Stream<?> base) {
        // We need to return Optional instead of OptionalDouble because
        // the spec requires the method "get", but the OptionalDouble has
        // "getAsDouble"
        long[] avg = base.mapToLong(x -> {
            if (x instanceof Long l) return l;
            if (x instanceof Number n) return n.longValue();
            return (Long) context.convertToType(x, Long.class);
        }).collect(() -> new long[2],
                (ll, i) -> {
                    ll[0]++;
                    ll[1] += i;
                },
                (ll, rr) -> {
                    ll[0] += rr[0];
                    ll[1] += rr[1];
                });
        return avg[0] > 0
                ? Optional.of((double) avg[1] / avg[0])
                : Optional.empty();
    }

    private Object executeSubstream(Stream<?> base, Object[] params) {
        Stream<?> skippedStream = base.skip(((long) params[0]));
        if (params.length == 2) {
            return skippedStream.limit((long) params[1] - (long) params[0]);
        }
        return skippedStream;
    }
}
