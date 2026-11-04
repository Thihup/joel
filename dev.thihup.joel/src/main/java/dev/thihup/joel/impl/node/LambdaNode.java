package dev.thihup.joel.impl.node;

import dev.thihup.joel.impl.JoelValueExpression;
import jakarta.el.ELContext;
import jakarta.el.LambdaExpression;
import jakarta.el.ValueExpression;

import java.util.List;

public record LambdaNode(List<String> parameters, Node expression, ValueExpression bodyExpr) implements Node {
    public LambdaNode(List<String> parameters, Node expression) {
        this(parameters, expression, JoelValueExpression.newInstance(expression.prettyPrint(), expression, Object.class));
    }

    @Override
    public Object getValue(ELContext context) {
        return new LambdaExpression(parameters, bodyExpr);
    }

    @Override
    public String prettyPrint() {
        return "(%s) -> %s".formatted(String.join(",", parameters), expression.prettyPrint());
    }
}
