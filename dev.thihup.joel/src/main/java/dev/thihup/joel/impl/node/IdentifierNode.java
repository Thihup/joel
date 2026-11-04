package dev.thihup.joel.impl.node;

import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ImportHandler;
import jakarta.el.PropertyNotFoundException;

public record IdentifierNode(String value) implements Node {

    private static final ClassValue<ELClass> EL_CLASS_CACHE = new ClassValue<>() {
        @Override
        protected ELClass computeValue(Class<?> type) {
            return new ELClass(type);
        }
    };

    @Override
    public Class<?> getType(ELContext context) {
        var variableMapper = context.getVariableMapper();
        if (variableMapper != null) {
            var valueExpression = variableMapper.resolveVariable(value);
            if (valueExpression != null) {
                return valueExpression.getType(context);
            }
        }
        context.setPropertyResolved(false);
        Class<?> type = context.getELResolver().getType(context, null, value);
        if (context.isPropertyResolved()) {
            return type;
        }
        Class<?> aClass = context.getImportHandler().resolveClass(value);
        if (aClass != null)
            return aClass;
        throw new PropertyNotFoundException("Property %s not found".formatted(value));
    }

    @Override
    public Object getValue(ELContext context) {
        if (context.isLambdaArgument(value))
            return context.getLambdaArgument(value);
        var variableMapper = context.getVariableMapper();
        if (variableMapper != null) {
            var valueExpression = variableMapper.resolveVariable(value);
            if (valueExpression != null) {
                return valueExpression.getValue(context);
            }
        }
        context.setPropertyResolved(false);
        Object result = context.getELResolver().getValue(context, null, value);
        if (context.isPropertyResolved()) {
            return result;
        }
        ImportHandler importHandler = context.getImportHandler();
        if (importHandler != null) {
            Class<?> aClass = importHandler.resolveClass(value);
            if (aClass != null) {
                return EL_CLASS_CACHE.get(aClass);
            }
            aClass = importHandler.resolveStatic(value);
            if (aClass != null) {
                return context.getELResolver().getValue(context, EL_CLASS_CACHE.get(aClass), value);
            }
        }

        throw new PropertyNotFoundException("Property %s not found".formatted(value));
    }

    @Override
    public String prettyPrint() {
        return value;
    }
}
