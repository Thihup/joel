package dev.thihup.joel.impl.spi;

import dev.thihup.joel.impl.JoelValueExpression;
import dev.thihup.joel.impl.TypeConverter;
import dev.thihup.joel.impl.node.ObjectNode;
import dev.thihup.joel.impl.JoelMethodExpression;
import dev.thihup.joel.impl.StreamELResolver;
import dev.thihup.joel.impl.antlr.JoelExpressionParser;
import dev.thihup.joel.impl.node.CallExpressionNode;
import dev.thihup.joel.impl.node.IdentifierNode;
import dev.thihup.joel.impl.node.MemberNode;
import dev.thihup.joel.impl.node.StringNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

import java.util.Objects;

public final class JoelExpressionFactory extends ExpressionFactory {

    public JoelExpressionFactory() {
    }

    @Override
    public Object coerceToType(Object object, Class<?> targetType) {
        try {
            return TypeConverter.coerce(object, targetType);
        } catch (Exception rootCause) {
            throw new ELException(rootCause);
        }
    }

    @Override
    public MethodExpression createMethodExpression(ELContext context, String expression, Class<?> expectedReturnType, Class<?>[] expectedParamTypes) {
        Objects.requireNonNull(expectedParamTypes);
        var parse = JoelExpressionParser.parse(expression);
        if (!(parse instanceof StringNode) && !(parse instanceof MemberNode) && !(parse instanceof IdentifierNode) && !(parse instanceof CallExpressionNode))
            throw new ELException("Invalid method expression: " + expression);
        return JoelMethodExpression.newInstance(expression, parse, expectedReturnType, expectedParamTypes);
    }

    @Override
    public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType) {
        Objects.requireNonNull(expectedType);
        var parseResult = JoelExpressionParser.parse(expression);
        return JoelValueExpression.newInstance(expression, parseResult, expectedType);
    }

    @Override
    public ValueExpression createValueExpression(Object instance, Class<?> expectedType) {
        Objects.requireNonNull(expectedType);
        return JoelValueExpression.newInstance("", new ObjectNode(instance), expectedType);
    }

    @Override
    public ELResolver getStreamELResolver() {
        return new StreamELResolver();
    }
}
