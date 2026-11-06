package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

@SuppressWarnings("cast")
public value record TernaryNode(Node condition, Node trueExpression,
                          Node falseExpression) implements ConditionalNode {
    @Override
    public Class<?> getType(ELContext context) {
        if ((boolean) context.convertToType(NodeEvaluator.eval(condition, context), boolean.class)) {
            return trueExpression.getType(context);
        }
        return falseExpression.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        if ((boolean) context.convertToType(NodeEvaluator.eval(condition, context), boolean.class)) {
            return NodeEvaluator.eval(trueExpression, context);
        }
        return NodeEvaluator.eval(falseExpression, context);
    }

    @Override
    public String prettyPrint() {
        return "%s ? %s : %s".formatted(condition.prettyPrint(), trueExpression.prettyPrint(), falseExpression.prettyPrint());
    }
}
