package io.joel.impl.spi;

import io.joel.impl.JoelMethodExpression;
import io.joel.impl.JoelValueExpression;
import io.joel.impl.StreamELResolver;
import io.joel.impl.antlr.JoelExpressionParser;
import io.joel.impl.node.ExpressionNode;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

import java.util.Objects;

import static java.lang.System.Logger.Level.INFO;

public class JoelExpressionFactory extends ExpressionFactory {
    private static final System.Logger LOGGER = System.getLogger(JoelExpressionFactory.class.getName());

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
        if (!(parse instanceof ExpressionNode.StringNode) && !(parse instanceof ExpressionNode.MemberNode) && !(parse instanceof ExpressionNode.IdentifierNode))
            throw new ELException("Invalid method expression: " + expression);
        return new JoelMethodExpression(expression, parse, expectedReturnType, expectedParamTypes);
    }

    @Override
    public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType) {
        Objects.requireNonNull(expectedType);
        LOGGER.log(INFO, "[JOEL] Evaluating expression: " + expression);
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
