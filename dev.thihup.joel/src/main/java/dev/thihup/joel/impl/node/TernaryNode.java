package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

@SuppressWarnings("cast")
public record TernaryNode(Node condition, Node trueExpression,
                          Node falseExpression) implements Node {
    @Override
    public void bind(ELContext context) {
        if (condition != null) condition.bind(context);
        if (trueExpression != null) trueExpression.bind(context);
        if (falseExpression != null) falseExpression.bind(context);
    }

    @Override
    public Class<?> getType(ELContext context) {
        if ((boolean) context.convertToType(condition.getValue(context), boolean.class)) {
            return trueExpression.getType(context);
        }
        return falseExpression.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        if ((boolean) context.convertToType(condition.getValue(context), boolean.class)) {
            return trueExpression.getValue(context);
        }
        return falseExpression.getValue(context);
    }

    @Override
    public String prettyPrint() {
        return "%s ? %s : %s".formatted(condition.prettyPrint(), trueExpression.prettyPrint(), falseExpression.prettyPrint());
    }
}
