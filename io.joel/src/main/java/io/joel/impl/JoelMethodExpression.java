package io.joel.impl;

import io.joel.impl.node.ExpressionNode;
import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;

import java.util.Objects;

public class JoelMethodExpression extends MethodExpression {
    private final ExpressionNode expressionNode;
    private final String expression;

    public JoelMethodExpression(String expression, ExpressionNode expressionNode) {
        this.expression = expression;
        this.expressionNode = expressionNode;
    }

    @Override
    public boolean isLiteralText() {
        return expressionNode instanceof ExpressionNode.StringNode;
    }

    @Override
    public String getExpressionString() {
        return expression;
    }

    @Override
    public MethodInfo getMethodInfo(ELContext context) {
        return null;
    }

    @Override
    public Object invoke(ELContext context, Object[] params) {
        try {
            context.notifyBeforeEvaluation(expression);
            return null;
        } finally {
            context.notifyAfterEvaluation(expression);
        }
    }

    @Override
    public boolean isParametersProvided() {
        return super.isParametersProvided();
    }

    @Override
    public boolean isParmetersProvided() {
        return isParametersProvided();
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, expressionNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoelMethodExpression that = (JoelMethodExpression) o;
        return Objects.equals(expressionNode, that.expressionNode) && Objects.equals(expression, that.expression);
    }
}
