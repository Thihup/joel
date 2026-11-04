package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings("cast")
public record EqualNode(Node left, Node right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        return equals(left.getValue(context), right.getValue(context), context);
    }

    public static boolean equals(Object leftValue, Object rightValue, ELContext context) {
        if (leftValue == rightValue)
            return true;

        if (leftValue == null || rightValue == null) {
            return false;
        }

        if (leftValue instanceof String s1 && rightValue instanceof String s2) {
            return s1.equals(s2);
        }

        if (leftValue instanceof Boolean b1 && rightValue instanceof Boolean b2) {
            return b1.equals(b2);
        }

        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            BigDecimal bd1 = leftValue instanceof BigDecimal bd ? bd : (BigDecimal) context.convertToType(leftValue, BigDecimal.class);
            BigDecimal bd2 = rightValue instanceof BigDecimal bd ? bd : (BigDecimal) context.convertToType(rightValue, BigDecimal.class);
            return bd1.compareTo(bd2) == 0;
        }

        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            BigInteger bi1 = leftValue instanceof BigInteger bi ? bi : (BigInteger) context.convertToType(leftValue, BigInteger.class);
            BigInteger bi2 = rightValue instanceof BigInteger bi ? bi : (BigInteger) context.convertToType(rightValue, BigInteger.class);
            return bi1.compareTo(bi2) == 0;
        }

        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
            double d1 = leftValue instanceof Number n ? n.doubleValue() : (Double) context.convertToType(leftValue, Double.class);
            double d2 = rightValue instanceof Number n ? n.doubleValue() : (Double) context.convertToType(rightValue, Double.class);
            return Double.compare(d1, d2) == 0;
        }

        if (leftValue instanceof Number || rightValue instanceof Number) {
            if (leftValue instanceof Number n1 && rightValue instanceof Number n2) {
                return n1.longValue() == n2.longValue();
            }
            Long l1 = (Long) context.convertToType(leftValue, Long.class);
            Long l2 = (Long) context.convertToType(rightValue, Long.class);
            return l1.equals(l2);
        }

        if (leftValue instanceof Boolean || rightValue instanceof Boolean) {
            Boolean b1 = (Boolean) context.convertToType(leftValue, Boolean.class);
            Boolean b2 = (Boolean) context.convertToType(rightValue, Boolean.class);
            return b1.equals(b2);
        }

        if (leftValue instanceof Enum<?> && rightValue instanceof Enum<?>) {
            return leftValue.equals(rightValue);
        }

        if (leftValue instanceof Enum<?>) {
            Object r = context.convertToType(rightValue, leftValue.getClass());
            return leftValue.equals(r);
        }

        if (rightValue instanceof Enum<?>) {
            Object l = context.convertToType(leftValue, rightValue.getClass());
            return rightValue.equals(l);
        }

        if (leftValue instanceof String || rightValue instanceof String) {
            String s1 = (String) context.convertToType(leftValue, String.class);
            String s2 = (String) context.convertToType(rightValue, String.class);
            return s1.equals(s2);
        }

        return leftValue.equals(rightValue);
    }

    @Override
    public String prettyPrint() {
        return "%s == %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
