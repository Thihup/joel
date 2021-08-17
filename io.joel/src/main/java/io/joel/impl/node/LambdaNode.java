package io.joel.impl.node;

import io.joel.impl.JoelValueExpression;
import io.joel.impl.Utils;
import jakarta.el.ELContext;
import jakarta.el.LambdaExpression;

import java.util.List;

public record LambdaNode(List<String> parameters, ExpressionNode expression) implements ExpressionNode {
    @Override
    public Object getValue(ELContext context) {
        return new LambdaExpression(parameters, JoelValueExpression.newInstance(Utils.prettyPrint(expression), expression, Object.class));
    }
}
