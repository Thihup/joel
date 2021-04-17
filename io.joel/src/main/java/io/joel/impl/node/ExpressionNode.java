package io.joel.impl.node;

import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ELException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface ExpressionNode {

    default Object getValue(ELContext context) {
        throw new UnsupportedOperationException(this.toString());
    }

    default Class<?> getType(ELContext context) {
        throw new UnsupportedOperationException(this.toString());
    }

    enum NullNode implements ExpressionNode {
        INSTANCE;

        @Override
        public Object getValue(ELContext context) {
            return null;
        }

        @Override
        public Class<?> getType(ELContext context) {
            return null;
        }

        @Override
        public String toString() {
            return "NullNode[" + super.toString() + "]";
        }

    }

    enum BooleanNode implements BooleanExpression {
        TRUE, FALSE;

        @Override
        public Object getValue(ELContext context) {
            return this == TRUE ? Boolean.TRUE : Boolean.FALSE;
        }

        public BooleanNode negate() {
            if (this == TRUE)
                return FALSE;
            return TRUE;
        }

        @Override
        public String toString() {
            return "BooleanNode[" + super.toString() + "]";
        }

        @Override
        public Class<?> getType(ELContext context) {
            return Boolean.class;
        }
    }

    interface BooleanExpression extends ExpressionNode {
        @Override
        default Class<?> getType(ELContext context) {
            return Boolean.class;
        }
    }

    record DynamicExpressionNode(ExpressionNode node) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return node.getType(context);
        }

        @Override
        public Object getValue(ELContext context) {
            return node.getValue(context);
        }
    }

    record DeferredExpressionNode(ExpressionNode node) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return node.getType(context);
        }

        @Override
        public Object getValue(ELContext context) {
            return node.getValue(context);
        }
    }

    record UnaryMinusNode(ExpressionNode node) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return node.getType(context);
        }

        @Override
        public Object getValue(ELContext context) {
            Object value = node.getValue(context);
            if (value == null)
                return 0L;
            if (value instanceof BigDecimal bigDecimal) {
                return bigDecimal.negate();
            }
            if (value instanceof BigInteger bigInteger) {
                return bigInteger.negate();
            }
            if (value instanceof String asString) {
                if (asString.indexOf('.') >= 0 || asString.indexOf('e') >= 0 || asString.indexOf('E') >= 0) {
                    return -context.convertToType(asString, Double.class);
                }
                return -context.convertToType(asString, Long.class);
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
                return -asShort;
            }
            if (value instanceof Byte asByte) {
                return -asByte;
            }
            throw new ELException("Cannot apply the '-' operator to %s".formatted(value));
        }
    }


    record NumberNode(Number value) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return value.getClass();
        }

        @Override
        public Object getValue(ELContext context) {
            return value;
        }
    }

    record StringNode(String value) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return String.class;
        }

        @Override
        public Object getValue(ELContext context) {
            return value;
        }
    }

    record IdentifierNode(String value) implements ExpressionNode {
        public IdentifierNode {
            if (!Character.isJavaIdentifierStart(value.charAt(0)) || !value.chars().skip(1).allMatch(Character::isJavaIdentifierPart))
                throw new IllegalArgumentException("Invalid identifier: " + value);
        }

        @Override
        public Class<?> getType(ELContext context) {
            var variableMapper = context.getVariableMapper();
            if (variableMapper != null) {
                var valueExpression = variableMapper.resolveVariable(value);
                if (valueExpression != null) {
                    return valueExpression.getType(context);
                }
            }
            context.setPropertyResolved(false);
            Class<?> type = context.getELResolver().getType(context, null, value);
            if (context.isPropertyResolved()) {
                return type;
            }
            Class<?> aClass = context.getImportHandler().resolveClass(value);
            if (aClass != null)
                return aClass;
            throw new ELException("Property %s not found".formatted(value));
        }

        @Override
        public Object getValue(ELContext context) {
            var variableMapper = context.getVariableMapper();
            if (variableMapper != null) {
                var valueExpression = variableMapper.resolveVariable(value);
                if (valueExpression != null) {
                    return valueExpression.getValue(context);
                }
            }
            context.setPropertyResolved(false);
            Object result = context.getELResolver().getValue(context, null, value);
            if (context.isPropertyResolved()) {
                return result;
            }
            throw new ELException("Property %s not found".formatted(value));
        }
    }

    record UnaryNotNode(ExpressionNode node) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return Boolean.class;
        }

        @Override
        public Object getValue(ELContext context) {
            return !context.convertToType(node.getValue(context), boolean.class);
        }
    }

    record MemberNode(ExpressionNode object, ExpressionNode property) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            if (property instanceof IdentifierNode node)
                return context.getELResolver().getType(context, new ELClass(object.getType(context)), node.value);

            return context.getELResolver().getType(context, new ELClass(object.getType(context)), property.getValue(context));
        }

        @Override
        public Object getValue(ELContext context) {
            if (property instanceof IdentifierNode node)
                return context.getELResolver().getValue(context, new ELClass(object.getType(context)), node.value);

            return context.getELResolver().getValue(context, new ELClass(object.getType(context)), property.getValue(context));
        }
    }

    record TernaryNode(ExpressionNode condition, ExpressionNode trueExpression,
                       ExpressionNode falseExpression) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            if (context.convertToType(condition.getValue(context), boolean.class)) {
                return trueExpression.getType(context);
            }
            return falseExpression.getType(context);
        }

        @Override
        public Object getValue(ELContext context) {
            if (context.convertToType(condition.getValue(context), boolean.class)) {
                return trueExpression.getType(context);
            }
            return falseExpression.getType(context);
        }
    }

    record CallExpressionNode(ExpressionNode callee, List<IdentifierNode> arguments) implements ExpressionNode {
    }

}
