package dev.thihup.joel.impl;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.LambdaExpression;
import jakarta.el.MethodNotFoundException;

import java.beans.FeatureDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class StreamELResolver extends ELResolver {
    private static final MethodHandle LAMBDA_IMPL_WITH_RETURN;
    private static final MethodHandle LAMBDA_IMPL_WITHOUT_RETURN;
    private static final List<String> IGNORED_METHODS = List.of("equals", "hashCode", "toString");
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
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
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
            if (params != null && params.length != 0 && Arrays.stream(params).noneMatch(LambdaExpression.class::isInstance)) {
                context.setPropertyResolved(base, method);
                return method1.invoke(base, params);
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

    private Object executeOptionalOrElseGet(ELContext context, Object base, Object method, LambdaExpression lambda) {
        try {
            var orElseGet = Optional.class.getMethod("orElseGet", Supplier.class);
            Supplier<?> lambdaFromLambdaExpression = (Supplier<?>) createLambdaFromLambdaExpression(context, lambda, Supplier.class, Supplier.class.getMethod("get"));
            context.setPropertyResolved(base, method);
            return orElseGet.invoke(base, lambdaFromLambdaExpression);
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new MethodNotFoundException(noSuchMethodException);
        } catch (Exception exception) {
            throw new ELException(exception);
        }
    }

    private Object[] getArguments(ELContext context, Object[] currentParams, Class<?>[] parameterTypes1) {
        Object[] array = new Object[parameterTypes1.length];
        for (int x = 0; x < parameterTypes1.length; x++) {
            try {
                array[x] = parameterTypes1[x].isInterface() ?
                        createLambdaFromLambdaExpression(context, (LambdaExpression) currentParams[x], parameterTypes1[x], findMethodFromClass(parameterTypes1[x]))
                        : currentParams[x];
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new ELException(noSuchMethodException);
            }
        }
        return array;
    }

    private Method findMethod(Class<?>[] parameterTypes, String methodName, Object[] currentParams) throws NoSuchMethodException {
        return Arrays.stream(STREAM_METHODS)
                .filter(x -> !Modifier.isStatic(x.getModifiers()))
                .filter(x -> x.getName().equals(methodName))
                .filter(x -> parameterTypes == null || Arrays.equals(parameterTypes, x.getParameterTypes()))
                .filter(x -> currentParams == null || currentParams.length == x.getParameterCount())
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
    }

    private Object createLambdaFromLambdaExpression(ELContext context, LambdaExpression lambdaExpression, Class<?> aClass, Method method) {
        if (method.getReturnType() == void.class)
            return MethodHandleProxies.asInterfaceInstance(aClass, LAMBDA_IMPL_WITHOUT_RETURN.bindTo(context).bindTo(lambdaExpression).asVarargsCollector(Object[].class));
        return MethodHandleProxies.asInterfaceInstance(aClass, LAMBDA_IMPL_WITH_RETURN.bindTo(method.getReturnType()).bindTo(context).bindTo(lambdaExpression).asVarargsCollector(Object[].class));
    }

    private Method findMethodFromClass(Class<?> klass) throws NoSuchMethodException {
        return Arrays.stream(klass.getMethods())
                .filter(x -> !x.isDefault())
                .filter(x -> !Modifier.isStatic(x.getModifiers()))
                .filter(x -> IGNORED_METHODS.stream().noneMatch(x.getName()::equals))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
    }

    private Object executeSum(ELContext context, Stream<?> base) {
        return base.mapToLong(x -> (Long) context.convertToType(x, Long.class)).sum();
    }

    private Object executeAverage(ELContext context, Stream<?> base) {
        // We need to return Optional instead of OptionalDouble because
        // the spec requires the method "get", but the OptionalDouble has
        // "getAsDouble"
        long[] avg = base.mapToLong(x -> (Long) context.convertToType(x, Long.class))
                .collect(() -> new long[2],
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
