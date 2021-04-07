package jakarta.el;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

public abstract class ExpressionFactory {

    public static ExpressionFactory newInstance() {
        return ServiceLoader.load(ExpressionFactory.class).iterator().next();
    }

    public static ExpressionFactory newInstance(Properties properties) {
        return newInstance();
    }

    public ExpressionFactory() {}

    public Map<String, Method> getInitFunctionMap() {
        return Collections.emptyMap();
    }

    public ELResolver getStreamELResolver() {
        return null;
    }

    public abstract Object coerceToType(Object object, Class<?> targetType);

    public abstract MethodExpression createMethodExpression(ELContext context, String expression, Class<?> expectedReturnType, Class<?>[] expectedParamTypes);

    public abstract ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType);

    public abstract ValueExpression createValueExpression(Object instance, Class<?> expectedType);

}
