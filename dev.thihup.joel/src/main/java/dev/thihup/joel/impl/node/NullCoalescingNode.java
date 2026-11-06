package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

public value record NullCoalescingNode(Node expression, Node defaultExpression) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        Object val = expression.getValue(context);
        if (val != null) {
            return expression.getType(context);
        }
        return defaultExpression.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        Object val = expression.getValue(context);
        if (val != null) {
            return val;
        }
        return defaultExpression.getValue(context);
    }

    @Override
    public String prettyPrint() {
        return "%s ?? %s".formatted(expression.prettyPrint(), defaultExpression.prettyPrint());
    }
}

