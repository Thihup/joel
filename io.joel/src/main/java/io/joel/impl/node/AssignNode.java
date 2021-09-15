package io.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueReference;

public record AssignNode(Node left, Node right) implements InfixExpressionNode {
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
        if (node instanceof Node expressionNode)
            return expressionNode.getValue(context);
        return null;
    }

    @Override
    public String prettyPrint() {
        return "%s = %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
