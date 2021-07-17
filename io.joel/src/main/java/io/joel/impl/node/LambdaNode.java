package io.joel.impl.node;

import io.joel.impl.JoelValueExpression;
import jakarta.el.ELContext;
import jakarta.el.LambdaExpression;

import java.util.List;

public record LambdaNode(List<String> parameters, ExpressionNode expression) implements ExpressionNode {
    @Override
    public Object getValue(ELContext context) {
        return new LambdaExpression(parameters, new JoelValueExpression(expression.prettyPrint(), expression, Object.class));
    }

    @Override
    public String prettyPrint() {
        return "(%s) -> %s".formatted(String.join(",", parameters), expression.prettyPrint());
    }
}
