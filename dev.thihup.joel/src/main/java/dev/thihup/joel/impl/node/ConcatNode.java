package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ELException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"cast", "rawtypes", "unchecked"})
public record ConcatNode(Node left, Node right) implements InfixExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        throw new UnsupportedOperationException();
    }

    private static String toString(Object val, ELContext context) {
        if (val instanceof String s) return s;
        if (val == null) return "";
        return (String) context.convertToType(val, String.class);
    }

    @Override
    public Object getValue(ELContext context) {
        Object leftVal = left.getValue(context);
        Object rightVal = right.getValue(context);
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
        return toString(leftVal, context) + toString(rightVal, context);
    }

    @Override
    public String prettyPrint() {
        return "%s += %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
