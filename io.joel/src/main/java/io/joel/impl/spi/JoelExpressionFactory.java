package io.joel.impl.spi;

import io.joel.impl.JoelValueExpression;
import io.joel.impl.TypeConverter;
import io.joel.impl.antlr.JoelExpressionParser;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;

import java.util.Map;

import static java.lang.System.Logger.Level.*;

public class JoelExpressionFactory extends ExpressionFactory {
    private static final System.Logger LOGGER = System.getLogger(JoelExpressionFactory.class.getName());
    @Override
    @SuppressWarnings("unchecked")
    public <T> T coerceToType(Object object, Class<T> targetType) {
        return (T) TypeConverter.coerce(object, targetType);
    }

    @Override
    public MethodExpression createMethodExpression(ELContext context, String expression, Class<?> expectedReturnType, Class<?>[] expectedParamTypes) {
        return null;
    }

    @Override
    public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType) {
        LOGGER.log(INFO, "[JOEL] Evaluating expression: " + expression);
        var parseResult = JoelExpressionParser.parse(expression);
        return new JoelValueExpression(expression, parseResult, expectedType);
    }

    @Override
    public ValueExpression createValueExpression(Object instance, Class<?> expectedType) {
        return null;
    }
}
