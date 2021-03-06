package dev.thihup.joel.impl.node;

import dev.thihup.joel.impl.JoelValueExpression;
import jakarta.el.ELContext;
import jakarta.el.LambdaExpression;

import java.util.List;

public record LambdaNode(List<String> parameters, Node expression) implements Node {
    @Override
    public Object getValue(ELContext context) {
        return new LambdaExpression(parameters, JoelValueExpression.newInstance(expression.prettyPrint(), expression, Object.class));
    }

    @Override
    public String prettyPrint() {
        return "(%s) -> %s".formatted(String.join(",", parameters), expression.prettyPrint());
    }
}
