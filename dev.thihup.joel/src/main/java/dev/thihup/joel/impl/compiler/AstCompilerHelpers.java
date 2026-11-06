package dev.thihup.joel.impl.compiler;

import dev.thihup.joel.impl.calculator.Operation;
import dev.thihup.joel.impl.Utils;
import jakarta.el.ELClass;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import dev.thihup.joel.impl.node.EqualNode;
import dev.thihup.joel.impl.node.Node;
import dev.thihup.joel.impl.node.RelationalNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ImportHandler;
import jakarta.el.PropertyNotFoundException;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.math.BigDecimal;

@SuppressWarnings({"cast", "exports"})
public final class AstCompilerHelpers {

    private AstCompilerHelpers() {
    }

    public static final ClassValue<ELClass> EL_CLASS_CACHE = new ClassValue<>() {
        @Override
        protected ELClass computeValue(Class<?> type) {
            return new ELClass(type);
        }
    };

    public static Object negateValue(Object value, ELContext context) {
        if (value == null)
            return 0L;
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.negate();
        }
        if (value instanceof java.math.BigInteger bigInteger) {
            return bigInteger.negate();
        }
        if (value instanceof String asString) {
            if (Utils.isFloatingPointNumber(asString)) {
                return -(Double) context.convertToType(asString, Double.class);
            }
            return -(Long) context.convertToType(asString, Long.class);
        }
        if (value instanceof Long asLong) {
            return -asLong;
        }
        if (value instanceof Integer asInt) {
            return -asInt;
        }
        if (value instanceof Double asDouble) {
            return -asDouble;
        }
        if (value instanceof Float asFloat) {
            return -asFloat;
        }
        if (value instanceof Short asShort) {
            return (short) -asShort;
        }
        if (value instanceof Byte asByte) {
            return (byte) -asByte;
        }
        throw new ELException("Could not perform unary minus on: " + value);
    }

    public static Object emptyValue(Object value) {
        if (value == null)
            return true;
        if (value instanceof String newValue && newValue.isEmpty())
            return true;
        if (value.getClass().isArray() && Array.getLength(value) == 0)
            return true;
        if (value instanceof Map<?, ?> newValue && newValue.isEmpty())
            return true;
        return value instanceof Collection<?> newValue && newValue.isEmpty();
    }

    private static String toConcatString(Object val, ELContext context) {
        if (val instanceof String s) return s;
        if (val == null) return "";
        return (String) context.convertToType(val, String.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object concatValues(Object leftVal, Object rightVal, ELContext context) {
        if (leftVal instanceof Map leftMap && rightVal instanceof Map rightMap) {
            try {
                leftMap.putAll(rightMap);
                return leftMap;
            } catch (Exception e) {
                throw new ELException(e);
            }
        }
        if (leftVal instanceof Set leftSet && rightVal instanceof Collection rightColl) {
            try {
                leftSet.addAll(rightColl);
                return leftSet;
            } catch (Exception e) {
                throw new ELException(e);
            }
        }
        if (leftVal instanceof List leftList && rightVal instanceof Collection rightColl) {
            try {
                leftList.addAll(rightColl);
                return leftList;
            } catch (Exception e) {
                throw new ELException(e);
            }
        }
        return toConcatString(leftVal, context) + toConcatString(rightVal, context);
    }

    public static Object resolveIdentifier(ELContext context, String name) {
        if (context.isLambdaArgument(name))
            return context.getLambdaArgument(name);
        var variableMapper = context.getVariableMapper();
        if (variableMapper != null) {
            var valueExpression = variableMapper.resolveVariable(name);
            if (valueExpression != null) {
                return valueExpression.getValue(context);
            }
        }
        context.setPropertyResolved(false);
        Object result = context.getELResolver().getValue(context, null, name);
        if (context.isPropertyResolved()) {
            return result;
        }
        ImportHandler importHandler = context.getImportHandler();
        if (importHandler != null) {
            Class<?> aClass = importHandler.resolveClass(name);
            if (aClass != null) {
                return EL_CLASS_CACHE.get(aClass);
            }
            aClass = importHandler.resolveStatic(name);
            if (aClass != null) {
                return context.getELResolver().getValue(context, EL_CLASS_CACHE.get(aClass), name);
            }
        }
        throw new PropertyNotFoundException("Property %s not found".formatted(name));
    }

    public static Object resolveMemberProperty(ELContext context, Object base, String propertyName) {
        if (base == null) return null;
        context.setPropertyResolved(false);
        try {
            return context.getELResolver().getValue(context, base, propertyName);
        } catch (ELException rootCause) {
            throw new PropertyNotFoundException(propertyName, rootCause);
        }
    }

    public static Object resolveMemberDynamic(ELContext context, Object base, Object property) {
        if (base == null) return null;
        context.setPropertyResolved(false);
        try {
            return context.getELResolver().getValue(context, base, property);
        } catch (ELException rootCause) {
            throw new PropertyNotFoundException(String.valueOf(property), rootCause);
        }
    }

    public static Object evalMemberProp(ELContext context, MethodHandle objMh, String prop) throws Throwable {
        Object base = objMh.invokeExact(context);
        return resolveMemberProperty(context, base, prop);
    }

    public static Object evalMemberDynamic(ELContext context, MethodHandle objMh, MethodHandle propMh) throws Throwable {
        Object base = objMh.invokeExact(context);
        Object prop = propMh.invokeExact(context);
        return resolveMemberDynamic(context, base, prop);
    }

    public static Object evalConcat(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        return concatValues(l, r, context);
    }

    public static Object evalAdd(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        return Operation.ADDITION.calculate(l, r, context);
    }

    public static Object evalSub(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        return Operation.SUBTRACTION.calculate(l, r, context);
    }

    public static Object evalMul(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        return Operation.MULTIPLICATION.calculate(l, r, context);
    }

    public static Object evalDiv(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        return Operation.DIVISION.calculate(l, r, context);
    }

    public static Object evalMod(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        return Operation.MODULO.calculate(l, r, context);
    }

    public static Object evalEqual(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        return EqualNode.equals(l, r, context);
    }

    public static Object evalNotEqual(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        return !EqualNode.equals(l, r, context);
    }

    public static Object evalLessThan(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        if (l == null || r == null) return false;
        return RelationalNode.compare(l, r, context) < 0;
    }

    public static Object evalLessEqual(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        if (l == null || r == null) return false;
        return RelationalNode.compare(l, r, context) <= 0;
    }

    public static Object evalGreaterThan(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        if (l == null || r == null) return false;
        return RelationalNode.compare(l, r, context) > 0;
    }

    public static Object evalGreaterEqual(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        Object r = right.invokeExact(context);
        if (l == null || r == null) return false;
        return RelationalNode.compare(l, r, context) >= 0;
    }

    public static Object evalAnd(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        if (!toBoolean(l, context)) return false;
        Object r = right.invokeExact(context);
        return toBoolean(r, context);
    }

    public static Object evalOr(ELContext context, MethodHandle left, MethodHandle right) throws Throwable {
        Object l = left.invokeExact(context);
        if (toBoolean(l, context)) return true;
        Object r = right.invokeExact(context);
        return toBoolean(r, context);
    }

    public static Object evalNot(ELContext context, MethodHandle node) throws Throwable {
        Object val = node.invokeExact(context);
        return !toBoolean(val, context);
    }

    public static Object evalMinus(ELContext context, MethodHandle node) throws Throwable {
        Object val = node.invokeExact(context);
        return negateValue(val, context);
    }

    public static Object evalEmpty(ELContext context, MethodHandle node) throws Throwable {
        Object val = node.invokeExact(context);
        return emptyValue(val);
    }

    public static Object evalTernary(ELContext context, MethodHandle cond, MethodHandle trueExpr, MethodHandle falseExpr) throws Throwable {
        Object c = cond.invokeExact(context);
        if (toBoolean(c, context)) {
            return trueExpr.invokeExact(context);
        }
        return falseExpr.invokeExact(context);
    }

    public static Object evalElvis(ELContext context, MethodHandle expr, MethodHandle defaultExpr) throws Throwable {
        Object val = expr.invokeExact(context);
        if (toBoolean(val, context)) return val;
        return defaultExpr.invokeExact(context);
    }

    public static Object evalNullCoalescing(ELContext context, MethodHandle expr, MethodHandle defaultExpr) throws Throwable {
        Object val = expr.invokeExact(context);
        if (val != null) return val;
        return defaultExpr.invokeExact(context);
    }

    public static Object evalFallback(ELContext context, Node node) {
        return node.getValue(context);
    }

    public static boolean toBoolean(Object val, ELContext context) {
        if (val instanceof Boolean b) return b;
        return (boolean) context.convertToType(val, boolean.class);
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static CallSite bootstrapMember(MethodHandles.Lookup callerLookup, String propName, MethodType type) {
        return new PropertyCallSite(callerLookup, propName, type);
    }

    public static boolean checkClass(Class<?> expected, Object obj) {
        return obj != null && obj.getClass() == expected;
    }

    public static final class PropertyCallSite extends MutableCallSite {
        private final String propertyName;
        private final MethodHandles.Lookup lookup;
        private int transitions = 0;

        public PropertyCallSite(MethodHandles.Lookup lookup, String propertyName, MethodType type) {
            super(type);
            this.lookup = lookup;
            this.propertyName = propertyName;
            try {
                MethodHandle fallbackMh = LOOKUP.findVirtual(PropertyCallSite.class, "fallback",
                        MethodType.methodType(Object.class, Object.class, ELContext.class)).bindTo(this);
                setTarget(fallbackMh.asType(type));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Object fallback(Object base, ELContext context) throws Throwable {
            if (base == null) return null;
            Class<?> clazz = base.getClass();
            if (transitions < 4) {
                MethodHandle directGetter = findGetter(clazz, propertyName);
                if (directGetter != null) {
                    transitions++;
                    MethodHandle test = LOOKUP.findStatic(AstCompilerHelpers.class, "checkClass",
                            MethodType.methodType(boolean.class, Class.class, Object.class)).bindTo(clazz);
                    MethodHandle direct = MethodHandles.dropArguments(directGetter.asType(MethodType.methodType(Object.class, Object.class)), 1, ELContext.class);
                    MethodHandle guarded = MethodHandles.guardWithTest(test, direct, getTarget());
                    setTarget(guarded);
                    return directGetter.invoke(base);
                }
            }
            return resolveMemberProperty(context, base, propertyName);
        }

        private MethodHandle findGetter(Class<?> clazz, String prop) {
            try {
                try {
                    var m = clazz.getMethod(prop);
                    if (m.getParameterCount() == 0 && m.getReturnType() != void.class) {
                        return lookup.unreflect(m);
                    }
                } catch (NoSuchMethodException ignored) {}

                String capitalized = Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
                try {
                    var m = clazz.getMethod("get" + capitalized);
                    if (m.getParameterCount() == 0 && m.getReturnType() != void.class) {
                        return lookup.unreflect(m);
                    }
                } catch (NoSuchMethodException ignored) {}

                try {
                    var m = clazz.getMethod("is" + capitalized);
                    if (m.getParameterCount() == 0 && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                        return lookup.unreflect(m);
                    }
                } catch (NoSuchMethodException ignored) {}

                try {
                    var f = clazz.getField(prop);
                    return lookup.unreflectGetter(f);
                } catch (NoSuchFieldException ignored) {}
            } catch (Exception ignored) {}
            return null;
        }
    }

    public static Object evalAddDirect(Object l, Object r, ELContext context) {
        if (l instanceof Long l1 && r instanceof Long l2) {
            return l1 + l2;
        }
        if (l instanceof Integer i1 && r instanceof Integer i2) {
            return (long) i1 + i2;
        }
        if (l instanceof Double d1 && r instanceof Double d2) {
            return d1 + d2;
        }
        if (l instanceof BigDecimal bd1 && r instanceof BigDecimal bd2) {
            return bd1.add(bd2);
        }
        return Operation.ADDITION.calculate(l, r, context);
    }

    public static Object evalSubDirect(Object l, Object r, ELContext context) {
        if (l instanceof Long l1 && r instanceof Long l2) {
            return l1 - l2;
        }
        if (l instanceof Integer i1 && r instanceof Integer i2) {
            return (long) i1 - i2;
        }
        if (l instanceof Double d1 && r instanceof Double d2) {
            return d1 - d2;
        }
        if (l instanceof BigDecimal bd1 && r instanceof BigDecimal bd2) {
            return bd1.subtract(bd2);
        }
        return Operation.SUBTRACTION.calculate(l, r, context);
    }

    public static Object evalMulDirect(Object l, Object r, ELContext context) {
        if (l instanceof Long l1 && r instanceof Long l2) {
            return l1 * l2;
        }
        if (l instanceof Integer i1 && r instanceof Integer i2) {
            return (long) i1 * i2;
        }
        if (l instanceof Double d1 && r instanceof Double d2) {
            return d1 * d2;
        }
        if (l instanceof BigDecimal bd1 && r instanceof BigDecimal bd2) {
            return bd1.multiply(bd2);
        }
        return Operation.MULTIPLICATION.calculate(l, r, context);
    }

    public static Object evalDivDirect(Object l, Object r, ELContext context) {
        return Operation.DIVISION.calculate(l, r, context);
    }

    public static Object evalModDirect(Object l, Object r, ELContext context) {
        return Operation.MODULO.calculate(l, r, context);
    }

    public static Object evalConcatDirect(Object l, Object r, ELContext context) {
        return concatValues(l, r, context);
    }

    public static Object evalEqualDirect(Object l, Object r, ELContext context) {
        return EqualNode.equals(l, r, context);
    }

    public static Object evalNotEqualDirect(Object l, Object r, ELContext context) {
        return !EqualNode.equals(l, r, context);
    }

    public static int evalCompareDirect(Object l, Object r, ELContext context) {
        return RelationalNode.compare(l, r, context);
    }
}
