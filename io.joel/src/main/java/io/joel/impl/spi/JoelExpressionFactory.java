package io.joel.impl.spi;

import io.joel.impl.JoelMethodExpression;
import io.joel.impl.JoelValueExpression;
import io.joel.impl.StreamELResolver;
import io.joel.impl.antlr.JoelExpressionParser;
import io.joel.impl.node.IdentifierNode;
import io.joel.impl.node.MemberNode;
import io.joel.impl.node.StringNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

import java.util.Objects;

public final class JoelExpressionFactory extends ExpressionFactory {

    @Override
    public Object coerceToType(Object object, Class<?> targetType) {
        try {
            return io.joel.impl.TypeConverter.coerce(object, targetType);
        } catch (Exception rootCause) {
            throw new ELException(rootCause);
        }
    }

    @Override
    public MethodExpression createMethodExpression(ELContext context, String expression, Class<?> expectedReturnType, Class<?>[] expectedParamTypes) {
        Objects.requireNonNull(expectedParamTypes);
        var parse = JoelExpressionParser.parse(expression);
        if (!(parse instanceof StringNode) && !(parse instanceof MemberNode) && !(parse instanceof IdentifierNode))
            throw new ELException("Invalid method expression: " + expression);
        return new JoelMethodExpression(expression, parse, expectedReturnType, expectedParamTypes);
    }

    @Override
    public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType) {
        Objects.requireNonNull(expectedType);
        var parseResult = JoelExpressionParser.parse(expression);
        return new JoelValueExpression(expression, parseResult, expectedType);
    }

    @Override
    public ValueExpression createValueExpression(Object instance, Class<?> expectedType) {
        Objects.requireNonNull(expectedType);
        return null;
    }

    @Override
    public ELResolver getStreamELResolver() {
        return new StreamELResolver();
    }
}
