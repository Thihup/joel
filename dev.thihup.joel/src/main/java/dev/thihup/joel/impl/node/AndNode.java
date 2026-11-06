package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

@SuppressWarnings("cast")
public value record AndNode(Node left, Node right) implements RelationalNode {
    @Override
    public Object getValue(ELContext context) {
        Object leftVal = left.getValue(context);
        if (!toBoolean(leftVal, context))
            return false;
        Object rightVal = right.getValue(context);
        return toBoolean(rightVal, context);
    }

    private static boolean toBoolean(Object val, ELContext context) {
        if (val instanceof Boolean b) return b;
        return (boolean) context.convertToType(val, boolean.class);
    }

    @Override
    public String prettyPrint() {
        return "%s && %s".formatted(left.prettyPrint(), right.prettyPrint());
    }
}
