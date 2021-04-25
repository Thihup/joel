package io.joel.impl.node;

import io.joel.impl.JoelValueExpression;
import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ImportHandler;
import jakarta.el.LambdaExpression;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueReference;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface ExpressionNode extends Serializable {

    default Object getValue(ELContext context) {
        throw new UnsupportedOperationException(this.toString());
    }

    default Class<?> getType(ELContext context) {
        throw new UnsupportedOperationException(this.toString());
    }

    default String prettyPrint() {
        throw new UnsupportedOperationException();
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
        public String prettyPrint() {
            return "null";
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

        @Override
        public String prettyPrint() {
            return super.toString();
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

        @Override
        public String prettyPrint() {
            return "${%s}".formatted(node.prettyPrint());
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

        @Override
        public String prettyPrint() {
            return "#{%s}".formatted(node.prettyPrint());
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
                return (short) -asShort;
            }
            if (value instanceof Byte asByte) {
                return (byte) -asByte;
            }
            throw new ELException("Cannot apply the '-' operator to %s".formatted(value));
        }

        @Override
        public String prettyPrint() {
            return "-%s".formatted(node.prettyPrint());
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

        @Override
        public String prettyPrint() {
            return value.toString();
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

        @Override
        public String prettyPrint() {
            return "'%s'".formatted(value);
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
            if (context.isLambdaArgument(value))
                return context.getLambdaArgument(value);
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
            Class<?> aClass = context.getImportHandler().resolveStatic(value);
            if (aClass != null) {
                context.getELResolver().getValue(context, new ELClass(aClass), value);
            }

            throw new ELException("Property %s not found".formatted(value));
        }

        @Override
        public String prettyPrint() {
            return value;
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

        @Override
        public String prettyPrint() {
            return "! %s".formatted(node.prettyPrint());
        }
    }

    record UnaryEmptyNode(ExpressionNode node) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return Boolean.class;
        }

        @Override
        public Object getValue(ELContext context) {
            Object value = node.getValue(context);
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

        @Override
        public String prettyPrint() {
            return "empty %s".formatted(node.prettyPrint());
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
            try {
                return context.getELResolver().getValue(context, object.getValue(context), property instanceof IdentifierNode node ? node.value : property.getValue(context));
            } catch (ELException rootCause) {
                if (object instanceof IdentifierNode node) {
                    ImportHandler importHandler = context.getImportHandler();
                    if (importHandler != null) {
                        Class<?> aClass = importHandler.resolveClass(node.value);
                        if (aClass != null)
                            return context.getELResolver().getValue(context, new ELClass(aClass), property instanceof IdentifierNode identifier ? identifier.value : property.getValue(context));
                    }
                }
                throw new PropertyNotFoundException(prettyPrint(), rootCause);
            }
        }

        public ValueReference valueReference(ELContext context) {
            return new ValueReference(object.getValue(context), property instanceof IdentifierNode node ? node.value : property.getValue(context));
        }

        @Override
        public String prettyPrint() {
            return "%s.%s".formatted(object.prettyPrint(), property.prettyPrint());
        }
    }

    record SemicolonNode(ExpressionNode left, ExpressionNode right) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return right.getType(context);
        }

        @Override
        public Object getValue(ELContext context) {
            left.getValue(context);
            return right.getValue(context);
        }

        @Override
        public String prettyPrint() {
            return "%s; %s".formatted(left.prettyPrint(), right.prettyPrint());
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
                return trueExpression.getValue(context);
            }
            return falseExpression.getValue(context);
        }

        @Override
        public String prettyPrint() {
            return "%s ? %s : %s".formatted(condition.prettyPrint(), trueExpression.prettyPrint(), falseExpression.prettyPrint());
        }
    }

    record SetNode(List<ExpressionNode> values) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return Set.class;
        }

        @Override
        public Object getValue(ELContext context) {
            return values.stream()
                    .map(x -> x.getValue(context))
                    .collect(Collectors.toSet());
        }

        @Override
        public String prettyPrint() {
            return values.stream().map(ExpressionNode::prettyPrint).collect(Collectors.joining(",", "{", "}"));
        }
    }

    record ListNode(List<ExpressionNode> values) implements ExpressionNode {
        @Override
        public Class<?> getType(ELContext context) {
            return List.class;
        }

        @Override
        public Object getValue(ELContext context) {
            return values.stream()
                    .map(x -> x.getValue(context))
                    .collect(Collectors.toList());
        }
        @Override
        public String prettyPrint() {
            return values.stream().map(ExpressionNode::prettyPrint).collect(Collectors.joining(",", "[", "]"));
        }
    }

    record CallExpressionNode(ExpressionNode callee, List<ExpressionNode> arguments) implements ExpressionNode {
        @Override
        public Object getValue(ELContext context) {
            if (!(callee instanceof MemberNode memberNode))
                throw new UnsupportedOperationException();
            var valueReference = memberNode.valueReference(context);
            var objects = arguments.stream().map(x -> x.getValue(context)).toArray();
            return context.getELResolver()
                    .invoke(context, valueReference.getBase(), valueReference.getProperty(), null, objects);
        }

        @Override
        public String prettyPrint() {
            return "%s(%s)".formatted(callee.prettyPrint(), arguments.stream().map(ExpressionNode::prettyPrint).collect(Collectors.joining(",")));
        }
    }

    record LambdaNode(List<String> parameters, ExpressionNode expression) implements ExpressionNode {
        @Override
        public Object getValue(ELContext context) {
            return new LambdaExpression(parameters, new JoelValueExpression(expression.prettyPrint(), expression, Object.class));
        }

        @Override
        public String prettyPrint() {
            return "(%s) -> %s".formatted(String.join(",", parameters), expression.prettyPrint());
        }
    }

}
