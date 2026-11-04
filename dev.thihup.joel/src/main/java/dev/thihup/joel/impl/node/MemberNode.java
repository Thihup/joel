package dev.thihup.joel.impl.node;

import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ImportHandler;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueReference;

public record MemberNode(Node object, Node property) implements Node {

    private static final ClassValue<ELClass> EL_CLASS_CACHE = new ClassValue<>() {
        @Override
        protected ELClass computeValue(Class<?> type) {
            return new ELClass(type);
        }
    };

    @Override
    public Class<?> getType(ELContext context) {
        if (property instanceof IdentifierNode node)
            return resolveType(context, node.value());

        return resolveType(context, property.getValue(context));
    }

    private Class<?> resolveType(ELContext context, Object property) {
        Object base = null;
        try {
            base = object.getValue(context);
        } catch (Exception ignored) {
        }
        if (base == null) {
            try {
                Class<?> objectType = object.getType(context);
                if (objectType != null) {
                    base = EL_CLASS_CACHE.get(objectType);
                }
            } catch (Exception ignored) {
            }
        }
        context.setPropertyResolved(false);
        Class<?> type = context.getELResolver().getType(context, base, property);
        if (type == null && object instanceof IdentifierNode identifier && property instanceof String name) {
            try {
                Class<?> baseClass = context.getImportHandler().resolveClass(identifier.value());
                return baseClass.getDeclaredField(name).getType();
            } catch (NoSuchFieldException ignored) {
                return null;
            }
        }
        return type;
    }

    @Override
    public Object getValue(ELContext context) {
        Object base = object.getValue(context);
        if (base == null) {
            return null;
        }
        Object prop = property instanceof IdentifierNode node ? node.value() : property.getValue(context);
        context.setPropertyResolved(false);
        try {
            return context.getELResolver().getValue(context, base, prop);
        } catch (ELException rootCause) {
            if (object instanceof IdentifierNode node) {
                ImportHandler importHandler = context.getImportHandler();
                if (importHandler != null) {
                    Class<?> aClass = importHandler.resolveClass(node.value());
                    if (aClass != null)
                        return context.getELResolver().getValue(context, EL_CLASS_CACHE.get(aClass), prop);
                }
            }
            throw new PropertyNotFoundException(prettyPrint(), rootCause);
        }
    }

    public Object getBase(ELContext context) {
        return object.getValue(context);
    }

    public Object getProperty(ELContext context) {
        return property instanceof IdentifierNode node ? node.value() : property.getValue(context);
    }

    public ValueReference valueReference(ELContext context) {
        return new ValueReference(getBase(context), getProperty(context));
    }

    @Override
    public String prettyPrint() {
        return "%s.%s".formatted(object.prettyPrint(), property.prettyPrint());
    }
}
