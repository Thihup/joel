package io.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ValueReference;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public interface InfixExpressionNode extends ExpressionNode {
    ExpressionNode left();

    ExpressionNode right();

    @Override
    default Class<?> getType(ELContext context) {
        return Number.class;
    }

    record AddExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == null && rightValue == null)
                return 0L;
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
                return context.convertToType(leftValue, BigDecimal.class).add(context.convertToType(rightValue, BigDecimal.class));
            }

            if (leftValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (rightValue instanceof BigInteger asBigInteger) {
                        return context.convertToType(leftValue, BigDecimal.class).add(context.convertToType(asBigInteger, BigDecimal.class));
                    }
                    return context.convertToType(leftValue, Double.class) + (context.convertToType(rightValue, Double.class));
                }
            }
            if (rightValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (leftValue instanceof BigInteger asBigInteger) {
                        return context.convertToType(leftValue, BigDecimal.class).add(context.convertToType(asBigInteger, BigDecimal.class));
                    }
                    return context.convertToType(leftValue, Double.class) + (context.convertToType(rightValue, Double.class));
                }
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                if (rightValue instanceof BigInteger asBigInteger) {
                    return context.convertToType(leftValue, BigDecimal.class).add(context.convertToType(asBigInteger, BigDecimal.class));
                }
                return context.convertToType(leftValue, Double.class) + (context.convertToType(rightValue, Double.class));
            }

            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigInteger.class).add(context.convertToType(rightValue, BigInteger.class));
            }

            return context.convertToType(leftValue, Long.class) + context.convertToType(rightValue, Long.class);
        }
    }

    record SubExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == null && rightValue == null)
                return 0L;
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
                return context.convertToType(leftValue, BigDecimal.class).subtract(context.convertToType(rightValue, BigDecimal.class));
            }

            if (leftValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (rightValue instanceof BigInteger asBigInteger) {
                        return context.convertToType(leftValue, BigDecimal.class).subtract(context.convertToType(asBigInteger, BigDecimal.class));
                    }
                    return context.convertToType(leftValue, Double.class) - (context.convertToType(rightValue, Double.class));
                }
            }
            if (rightValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (leftValue instanceof BigInteger asBigInteger) {
                        return context.convertToType(leftValue, BigDecimal.class).subtract(context.convertToType(asBigInteger, BigDecimal.class));
                    }
                    return context.convertToType(leftValue, Double.class) - (context.convertToType(rightValue, Double.class));
                }
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                if (rightValue instanceof BigInteger asBigInteger) {
                    return context.convertToType(leftValue, BigDecimal.class).subtract(context.convertToType(asBigInteger, BigDecimal.class));
                }
                return context.convertToType(leftValue, Double.class) - (context.convertToType(rightValue, Double.class));
            }

            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigInteger.class).subtract(context.convertToType(rightValue, BigInteger.class));
            }

            return context.convertToType(leftValue, Long.class) - context.convertToType(rightValue, Long.class);
        }
    }

    record MulExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == null && rightValue == null)
                return 0L;
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
                return context.convertToType(leftValue, BigDecimal.class).multiply(context.convertToType(rightValue, BigDecimal.class));
            }

            if (leftValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (rightValue instanceof BigInteger asBigInteger) {
                        return context.convertToType(leftValue, BigDecimal.class).multiply(context.convertToType(asBigInteger, BigDecimal.class));
                    }
                }
                return context.convertToType(leftValue, Double.class) * (context.convertToType(rightValue, Double.class));
            }
            if (rightValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (leftValue instanceof BigInteger asBigInteger) {
                        return context.convertToType(leftValue, BigDecimal.class).multiply(context.convertToType(asBigInteger, BigDecimal.class));
                    }
                    return context.convertToType(leftValue, Double.class) * (context.convertToType(rightValue, Double.class));
                }
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                if (rightValue instanceof BigInteger asBigInteger) {
                    return context.convertToType(leftValue, BigDecimal.class).multiply(context.convertToType(asBigInteger, BigDecimal.class));
                }
                return context.convertToType(leftValue, Double.class) * (context.convertToType(rightValue, Double.class));
            }

            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigInteger.class).multiply(context.convertToType(rightValue, BigInteger.class));
            }

            return context.convertToType(leftValue, Long.class) * context.convertToType(rightValue, Long.class);
        }
    }

    record DivExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == null && rightValue == null)
                return 0L;
            if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal || leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigDecimal.class).divide(context.convertToType(rightValue, BigDecimal.class), RoundingMode.HALF_UP);
            }
            return context.convertToType(leftValue, Double.class) / context.convertToType(rightValue, Double.class);
        }
    }

    record ModExpressionNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
        @Override
        public Object getValue(ELContext context) {
            var leftValue = left.getValue(context);
            var rightValue = right.getValue(context);
            if (leftValue == null && rightValue == null)
                return 0L;
            if (leftValue instanceof Float || leftValue instanceof Double || leftValue instanceof BigDecimal ||
                    rightValue instanceof Float || rightValue instanceof Double || rightValue instanceof BigDecimal) {
                return context.convertToType(leftValue, Double.class) % (context.convertToType(rightValue, Double.class));
            }
            if (leftValue instanceof String asString && (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0)) {
                return context.convertToType(leftValue, Double.class) % (context.convertToType(rightValue, Double.class));
            }
            if (rightValue instanceof String asString && (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0)) {
                return context.convertToType(leftValue, Double.class) % (context.convertToType(rightValue, Double.class));
            }
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return context.convertToType(leftValue, BigInteger.class).remainder(context.convertToType(rightValue, BigInteger.class));
            }

            return context.convertToType(leftValue, Long.class) % context.convertToType(rightValue, Long.class);
        }
    }

    record AssignNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue(ELContext context) {
            if (left instanceof IdentifierNode identifierNode) {
                Object value = right.getValue(context);
                context.getELResolver().setValue(context, null, identifierNode.value(), value instanceof ValueReference reference? context.getELResolver().getValue(context, reference.getBase(), reference.getProperty()) : value);
                return value;
            }
            if (left instanceof MemberNode memberNode) {
                Object leftValue = memberNode.getValue(context);
                if (leftValue instanceof ValueReference valueReference) {
                    Object value = right.getValue(context);
                    context.getELResolver().setValue(context, valueReference.getBase(), valueReference.getProperty(), value instanceof ValueReference reference? context.getELResolver().getValue(context, reference.getBase(), reference.getProperty()) : value);
                    return value;
                }
            }
            return null;
        }
    }

    record ConcatNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue(ELContext context) {
            return context.convertToType(left.getValue(context), String.class) + context.convertToType(right.getValue(context), String.class);
        }
    }
}
