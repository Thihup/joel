package io.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueReference;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public sealed interface InfixExpressionNode extends ExpressionNode {
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
                return ((BigInteger)context.convertToType(leftValue, BigInteger.class)).add((BigInteger) context.convertToType(rightValue, BigInteger.class));
            }

            return (Long) context.convertToType(leftValue, Long.class) + (Long) context.convertToType(rightValue, Long.class);
        }

        @Override
        public String prettyPrint() {
            return "%s + %s".formatted(left.prettyPrint(), right.prettyPrint());
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
                return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).subtract((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
            }

            if (leftValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (rightValue instanceof BigInteger asBigInteger) {
                        return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).subtract((BigDecimal) context.convertToType(asBigInteger, BigDecimal.class));
                    }
                    return (Double) context.convertToType(leftValue, Double.class) - (Double) (context.convertToType(rightValue, Double.class));
                }
            }
            if (rightValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (leftValue instanceof BigInteger asBigInteger) {
                        return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).subtract((BigDecimal) context.convertToType(asBigInteger, BigDecimal.class));
                    }
                    return (Double) context.convertToType(leftValue, Double.class) - (Double) (context.convertToType(rightValue, Double.class));
                }
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                    return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).subtract((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
                }
                return (Double) context.convertToType(leftValue, Double.class) - (Double) (context.convertToType(rightValue, Double.class));
            }

            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return ((BigInteger) context.convertToType(leftValue, BigInteger.class)).subtract((BigInteger) context.convertToType(rightValue, BigInteger.class));
            }

            return (Long) context.convertToType(leftValue, Long.class) - (Long) context.convertToType(rightValue, Long.class);
        }

        @Override
        public String prettyPrint() {
            return "%s - %s".formatted(left.prettyPrint(), right.prettyPrint());
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
                return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).multiply((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
            }

            if (leftValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (rightValue instanceof BigInteger asBigInteger) {
                        return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).multiply((BigDecimal) context.convertToType(asBigInteger, BigDecimal.class));
                    }
                }
                return (Double) context.convertToType(leftValue, Double.class) * (Double) (context.convertToType(rightValue, Double.class));
            }
            if (rightValue instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    if (leftValue instanceof BigInteger asBigInteger) {
                        return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).multiply((BigDecimal) context.convertToType(asBigInteger, BigDecimal.class));
                    }
                    return (Double) context.convertToType(leftValue, Double.class) * (Double) (context.convertToType(rightValue, Double.class));
                }
            }
            if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
                if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                    return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).multiply((BigDecimal) context.convertToType(rightValue, BigDecimal.class));
                }
                return ((Double) context.convertToType(leftValue, Double.class)) * (Double) (context.convertToType(rightValue, Double.class));
            }

            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return ((BigInteger) context.convertToType(leftValue, BigInteger.class)).multiply((BigInteger) context.convertToType(rightValue, BigInteger.class));
            }

            return (Long) context.convertToType(leftValue, Long.class) * (Long) context.convertToType(rightValue, Long.class);
        }

        @Override
        public String prettyPrint() {
            return "%s * %s".formatted(left.prettyPrint(), right.prettyPrint());
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
                return ((BigDecimal) context.convertToType(leftValue, BigDecimal.class)).divide((BigDecimal) context.convertToType(rightValue, BigDecimal.class), RoundingMode.HALF_UP);
            }
            return (Double) context.convertToType(leftValue, Double.class) / (Double) context.convertToType(rightValue, Double.class);
        }

        @Override
        public String prettyPrint() {
            return "%s / %s".formatted(left.prettyPrint(), right.prettyPrint());
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
                return (Double) context.convertToType(leftValue, Double.class) % (Double) (context.convertToType(rightValue, Double.class));
            }
            if (leftValue instanceof String asString && (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0)) {
                return (Double) context.convertToType(leftValue, Double.class) % (Double) (context.convertToType(rightValue, Double.class));
            }
            if (rightValue instanceof String asString && (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0)) {
                return (Double) context.convertToType(leftValue, Double.class) % (Double) (context.convertToType(rightValue, Double.class));
            }
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return ((BigInteger) context.convertToType(leftValue, BigInteger.class)).remainder((BigInteger) context.convertToType(rightValue, BigInteger.class));
            }

            return (Long) context.convertToType(leftValue, Long.class) % (Long) context.convertToType(rightValue, Long.class);
        }

        @Override
        public String prettyPrint() {
            return "%s %% %s".formatted(left.prettyPrint(), right.prettyPrint());
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
                Object value = getValue(context, right);
                context.getELResolver().setValue(context, null, identifierNode.value(), value);
                return value;
            }
            if (left instanceof MemberNode memberNode) {
                ValueReference valueReference = memberNode.valueReference(context);
                Object value = getValue(context, right);
                context.getELResolver().setValue(context, valueReference.getBase(), valueReference.getProperty(), value);
                return value;
            }
            throw new PropertyNotWritableException("Illegal syntax for assign operation");
        }

        private Object getValue(ELContext context, Object node) {
            if (node instanceof MemberNode memberNode)
                return getValue(context, memberNode.valueReference(context));
            if (node instanceof ValueReference reference)
                return context.getELResolver().getValue(context, reference.getBase(), reference.getProperty());
            if (node instanceof ExpressionNode expressionNode)
                return expressionNode.getValue(context);
            return null;
        }

        @Override
        public String prettyPrint() {
            return "%s = %s".formatted(left.prettyPrint(), right.prettyPrint());
        }
    }

    record ConcatNode(ExpressionNode left, ExpressionNode right) implements InfixExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue(ELContext context) {
            return (String) context.convertToType(left.getValue(context), String.class) + context.convertToType(right.getValue(context), String.class);
        }

        @Override
        public String prettyPrint() {
            return "%s += %s".formatted(left.prettyPrint(), right.prettyPrint());
        }
    }

}
