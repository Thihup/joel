package io.joel.impl.node;

import com.headius.invokebinder.Binder;
import io.joel.impl.Arithmetic;
import io.joel.impl.ELLinker;
import jakarta.el.ELContext;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.beans.StaticClass;
import jdk.dynalink.support.ChainedCallSite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.BigInteger;

public record AddExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final DynamicLinker LINKER;
    private static final MethodHandle ADD_HANDLE;

    static {
        var dynamicLinkerFactory = new DynamicLinkerFactory();
        dynamicLinkerFactory.setPrioritizedLinker(new ELLinker());
        LINKER = dynamicLinkerFactory.createLinker();
    }

    static {
        var getMethodType = MethodType.methodType(Object.class, StaticClass.class, String.class);
        var getDescriptor = new CallSiteDescriptor(LOOKUP, StandardOperation.GET.withNamespace(StandardNamespace.METHOD), getMethodType);
        var callMethodType = MethodType.methodType(Number.class, Object.class, Object.class, Number.class, Number.class);
        var callDescriptor = new CallSiteDescriptor(LOOKUP, StandardOperation.CALL, callMethodType);
        MethodHandle tmp;
        try {
            var addMethod = LINKER.link(new ChainedCallSite(getDescriptor)).dynamicInvoker().invokeExact(StaticClass.forClass(Arithmetic.class), "add");
            ADD_HANDLE = LINKER.link(new ChainedCallSite(callDescriptor)).dynamicInvoker().bindTo(addMethod);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public Object getValue(ELContext context) {
        var leftValue = left.getValue(context);
        var rightValue = right.getValue(context);
        if (leftValue == null && rightValue == null)
            return 0L;
        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).add((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
        }

        if (leftValue instanceof String asString) {
            if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                if (rightValue instanceof BigInteger asBigInteger) {
                    return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).add((BigDecimal) context.convertToType(asBigInteger, BigDecimal.class));
                }
                return ((Double) context.convertToType(leftValue, Double.class)) + (Double) (context.convertToType(rightValue, Double.class));
            }
        }
        if (rightValue instanceof String asString) {
            if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                if (leftValue instanceof BigInteger asBigInteger) {
                    return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).add((BigDecimal) context.convertToType(asBigInteger, BigDecimal.class));
                }
                return (Double) context.convertToType(leftValue, Double.class) + (Double) (context.convertToType(rightValue, Double.class));
            }
        }
        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).add((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
            }
            return (Double) context.convertToType(leftValue, Double.class) + (Double) (context.convertToType(rightValue, Double.class));
        }

        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            return ((BigInteger) context.convertToType(leftValue, BigInteger.class)).add((BigInteger) context.convertToType(rightValue, BigInteger.class));
        }

        return (Long) context.convertToType(leftValue, Long.class) + (Long) context.convertToType(rightValue, Long.class);
    }

    @Override
    public MethodHandle compile(MethodHandle context) throws Throwable {
        return Binder.from(Object.class, ELContext.class)
                .permute(0, 0)
                .filterForward(0, left.compile(context))
                .filterForward(1, right.compile(context))
                .cast(MethodType.methodType(Number.class, Number.class, Number.class))
                .prepend(ELContext.class, context)
                .invoke(ADD_HANDLE);
    }

    @Override
    public String prettyPrint() {
        return "%s + %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
