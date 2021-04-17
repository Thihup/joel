package io.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ELException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiPredicate;

public interface RelationalNode extends ExpressionNode.BooleanExpression {
    ExpressionNode left();

    ExpressionNode right();

    record AndNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
        @Override
        public Object getValue(ELContext context) {
            if (!context.convertToType(left.getValue(context), boolean.class))
                return false;
            return context.convertToType(right.getValue(context), boolean.class);
        }
    }

    record OrNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
        @Override
        public Object getValue(ELContext context) {
            if (context.convertToType(left.getValue(context), boolean.class))
                return true;
            return context.convertToType(right.getValue(context), boolean.class);
        }
    }

    record EqualNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            BiPredicate<Object, Object> equalsFunction = Object::equals;
            if (leftValue == rightValue)
                return true;

            if (leftValue == null || rightValue == null) {
                return false;
            }
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
                return equalsFunction.test(context.convertToType(leftValue, BigDecimal.class), context.convertToType(rightValue, BigDecimal.class));
            }
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return equalsFunction.test(context.convertToType(leftValue, BigInteger.class), context.convertToType(rightValue, BigInteger.class));
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                return equalsFunction.test(context.convertToType(leftValue, Double.class), context.convertToType(rightValue, Double.class));
            }
            if (leftValue instanceof Number || rightValue instanceof Number) {
                return equalsFunction.test(context.convertToType(leftValue, Long.class), context.convertToType(rightValue, Long.class));
            }
            if (leftValue instanceof Boolean || rightValue instanceof Boolean) {
                return equalsFunction.test(context.convertToType(leftValue, Boolean.class), context.convertToType(rightValue, Boolean.class));
            }
            if (leftValue instanceof Enum<?> || rightValue instanceof Enum<?>) {
                return equalsFunction.test(context.convertToType(leftValue, Enum.class), context.convertToType(rightValue, Enum.class));
            }
            return leftValue.equals(rightValue);
        }
    }

    record NotEqualNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
        @Override
        public Object getValue(ELContext context) {
            return !(Boolean) new EqualNode(left, right).getValue(context);
        }
    }

    record GreaterThanNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == null || rightValue == null) {
                return false;
            }
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
                return context.convertToType(leftValue, BigDecimal.class).compareTo(context.convertToType(rightValue, BigDecimal.class)) > 0;
            }
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigInteger.class).compareTo(context.convertToType(rightValue, BigInteger.class)) > 0;
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                return context.convertToType(leftValue, Double.class).compareTo(context.convertToType(rightValue, Double.class)) > 0;
            }
            if (leftValue instanceof Number || rightValue instanceof Number) {
                return context.convertToType(leftValue, Long.class) > context.convertToType(rightValue, Long.class);
            }
            if (leftValue instanceof Comparable comparable) {
                return comparable.compareTo(rightValue) > 0;
            }
            if (rightValue instanceof Comparable comparable) {
                return comparable.compareTo(leftValue) < 0;
            }
            throw new ELException("Cannot compare values: %s > %s".formatted(leftValue, rightValue));
        }
    }

    record GreaterEqualNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == rightValue)
                return true;
            if (leftValue == null || rightValue == null) {
                return false;
            }
            if (leftValue.equals(rightValue))
                return true;
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
                return context.convertToType(leftValue, BigDecimal.class).compareTo(context.convertToType(rightValue, BigDecimal.class)) >= 0;
            }
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigInteger.class).compareTo(context.convertToType(rightValue, BigInteger.class)) >= 0;
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                return context.convertToType(leftValue, Double.class).compareTo(context.convertToType(rightValue, Double.class)) >= 0;
            }
            if (leftValue instanceof Number || rightValue instanceof Number) {
                return context.convertToType(leftValue, Long.class) >= context.convertToType(rightValue, Long.class);
            }
            if (leftValue instanceof Comparable comparable) {
                return comparable.compareTo(rightValue) >= 0;
            }
            if (rightValue instanceof Comparable comparable) {
                return comparable.compareTo(leftValue) <= 0;
            }
            throw new ELException("Cannot compare values: %s >= %s".formatted(leftValue, rightValue));
        }
    }

    record LessThanNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == null || rightValue == null) {
                return false;
            }
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
                return context.convertToType(leftValue, BigDecimal.class).compareTo(context.convertToType(rightValue, BigDecimal.class)) < 0;
            }
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigInteger.class).compareTo(context.convertToType(rightValue, BigInteger.class)) < 0;
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                return context.convertToType(leftValue, Double.class).compareTo(context.convertToType(rightValue, Double.class)) < 0;
            }
            if (leftValue instanceof Number || rightValue instanceof Number) {
                return context.convertToType(leftValue, Long.class) < context.convertToType(rightValue, Long.class);
            }
            if (leftValue instanceof Comparable comparable) {
                return comparable.compareTo(rightValue) < 0;
            }
            if (rightValue instanceof Comparable comparable) {
                return comparable.compareTo(leftValue) > 0;
            }
            throw new ELException("Cannot compare values: %s < %s".formatted(leftValue, rightValue));
        }
    }

    record LessEqualNode(ExpressionNode left, ExpressionNode right) implements RelationalNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == rightValue)
                return true;
            if (leftValue == null || rightValue == null) {
                return false;
            }
            if (leftValue.equals(rightValue))
                return true;
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
                return context.convertToType(leftValue, BigDecimal.class).compareTo(context.convertToType(rightValue, BigDecimal.class)) <= 0;
            }
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigInteger.class).compareTo(context.convertToType(rightValue, BigInteger.class)) <= 0;
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                return context.convertToType(leftValue, Double.class).compareTo(context.convertToType(rightValue, Double.class)) <= 0;
            }
            if (leftValue instanceof Number || rightValue instanceof Number) {
                return context.convertToType(leftValue, Long.class) <= context.convertToType(rightValue, Long.class);
            }
            if (leftValue instanceof Comparable comparable) {
                return comparable.compareTo(rightValue) <= 0;
            }
            if (rightValue instanceof Comparable comparable) {
                return comparable.compareTo(leftValue) >= 0;
            }
            throw new ELException("Cannot compare values: %s <= %s".formatted(leftValue, rightValue));
        }
    }
}
