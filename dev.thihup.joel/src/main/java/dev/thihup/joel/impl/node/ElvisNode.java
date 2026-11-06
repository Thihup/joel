package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

@SuppressWarnings("cast")
public value record ElvisNode(Node expression, Node defaultExpression) implements ConditionalNode {
    @Override
    public Class<?> getType(ELContext context) {
        Object val = NodeEvaluator.eval(expression, context);
        if ((boolean) context.convertToType(val, boolean.class)) {
            return expression.getType(context);
        }
        return defaultExpression.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        Object val = NodeEvaluator.eval(expression, context);
        if ((boolean) context.convertToType(val, boolean.class)) {
            return val;
        }
        return NodeEvaluator.eval(defaultExpression, context);
    }

    @Override
    public String prettyPrint() {
        return "%s ?: %s".formatted(expression.prettyPrint(), defaultExpression.prettyPrint());
    }
}

