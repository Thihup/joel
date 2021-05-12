package io.joel.impl;

import com.headius.invokebinder.Binder;
import jdk.dynalink.linker.ConversionComparator;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.GuardingTypeConverterFactory;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;

public class ELLinker implements GuardingDynamicLinker, GuardingTypeConverterFactory, ConversionComparator {

    public static Number convert(BigDecimal value, Class<?> klass) {
        if (klass == long.class || klass == Long.class)
            return value.longValue();
        if (klass == short.class || klass == Short.class)
            return value.shortValue();
        if (klass == int.class || klass == Integer.class)
            return value.intValue();
        if (klass == byte.class || klass == Byte.class)
            return value.byteValue();
        if (klass == float.class || klass == Float.class)
            return value.longValue();
        if (klass == double.class || klass == Double.class)
            return value.doubleValue();
        if (klass == BigInteger.class)
            return BigInteger.valueOf(value.longValue());
        return value;
    }

    @Override
    public GuardedInvocation convertToType(Class<?> sourceType, Class<?> targetType, Supplier<MethodHandles.Lookup> lookupSupplier) throws Exception {
//        return new GuardedInvocation(MethodHandles.lookup().findVirtual(ELContext.class, "convertToType", MethodType.methodType(Object.class, Object.class, Class.class)));
        if (sourceType == Double.class && targetType == BigDecimal.class) {
            return new GuardedInvocation(Binder.from(BigDecimal.class, Double.class)
                    .convert(BigDecimal.class, double.class)
                    .invokeConstructor(MethodHandles.lookup(), BigDecimal.class));
        }
        return null;
    }

    @Override
    public Comparison compareConversion(Class<?> sourceType, Class<?> targetType1, Class<?> targetType2) {
//        if (sourceType == BigDecimal.class) {
//            return targetType1 == sourceType ? Comparison.TYPE_1_BETTER : Comparison.TYPE_2_BETTER;
//        }
//        return Comparison.INDETERMINATE;
        return null;
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        return null;
    }
}
