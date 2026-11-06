package dev.thihup.joel.impl.compiler;

import dev.thihup.joel.impl.calculator.Operation;
import dev.thihup.joel.impl.node.EqualNode;
import dev.thihup.joel.impl.node.Node;
import dev.thihup.joel.impl.node.NodeEvaluator;
import dev.thihup.joel.impl.node.RelationalNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ImportHandler;
import jakarta.el.PropertyNotFoundException;

import java.lang.invoke.MethodHandle;

@SuppressWarnings({"cast", "exports"})
public final class AstCompilerHelpers {

    private AstCompilerHelpers() {
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
                return NodeEvaluator.EL_CLASS_CACHE.get(aClass);
            }
            aClass = importHandler.resolveStatic(name);
            if (aClass != null) {
                return context.getELResolver().getValue(context, NodeEvaluator.EL_CLASS_CACHE.get(aClass), name);
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
        return NodeEvaluator.eval(node, context);
    }

    public static boolean toBoolean(Object val, ELContext context) {
        if (val instanceof Boolean b) return b;
        return (boolean) context.convertToType(val, boolean.class);
    }
}
