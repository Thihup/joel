package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ELException;

import java.math.BigDecimal;
import java.math.BigInteger;

public sealed interface RelationalNode extends BooleanExpression permits
        AndNode,
        EqualNode,
        GreaterEqualNode,
        GreaterThanNode,
        LessEqualNode,
        LessThanNode,
        NotEqualNode,
        OrNode {
    Node left();

    Node right();

    @SuppressWarnings({"unchecked", "rawtypes"})
    static int compare(Object leftValue, Object rightValue, ELContext context) {
        if (leftValue instanceof String s1 && rightValue instanceof String s2) {
            return s1.compareTo(s2);
        }
        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            BigDecimal b1 = leftValue instanceof BigDecimal bd ? bd : context.convertToType(leftValue, BigDecimal.class);
            BigDecimal b2 = rightValue instanceof BigDecimal bd ? bd : context.convertToType(rightValue, BigDecimal.class);
            return b1.compareTo(b2);
        }
        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            BigInteger b1 = leftValue instanceof BigInteger bi ? bi : context.convertToType(leftValue, BigInteger.class);
            BigInteger b2 = rightValue instanceof BigInteger bi ? bi : context.convertToType(rightValue, BigInteger.class);
            return b1.compareTo(b2);
        }
        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
            double d1 = leftValue instanceof Number n ? n.doubleValue() : context.convertToType(leftValue, Double.class);
            double d2 = rightValue instanceof Number n ? n.doubleValue() : context.convertToType(rightValue, Double.class);
            return Double.compare(d1, d2);
        }
        if (leftValue instanceof Number || rightValue instanceof Number) {
            long l1 = leftValue instanceof Number n ? n.longValue() : context.convertToType(leftValue, Long.class);
            long l2 = rightValue instanceof Number n ? n.longValue() : context.convertToType(rightValue, Long.class);
            return Long.compare(l1, l2);
        }
        if (leftValue instanceof String || rightValue instanceof String) {
            String s1 = context.convertToType(leftValue, String.class);
            String s2 = context.convertToType(rightValue, String.class);
            return s1.compareTo(s2);
        }
        if (leftValue instanceof Comparable comparable) {
            return comparable.compareTo(rightValue);
        }
        if (rightValue instanceof Comparable comparable) {
            return -comparable.compareTo(leftValue);
        }
        throw new ELException("Cannot compare values: " + leftValue + " and " + rightValue);
    }
}
