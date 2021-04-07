package jakarta.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

public abstract class ELResolver {
    public static final String TYPE = "type";
    public static final String RESOLVABLE_AT_DESIGN_TIME = "resolvableAtDesignTime";

    public Object convertToType(ELContext context, Object object, Class<?> targetType) {
        return null;
    }

    public Object invoke(ELContext context, Object base, Object method, Class<?>[] parameterTypes, Object[] params) {
        return null;
    }

    public abstract Class<?> getCommonPropertyType(ELContext context, Object base);

    public abstract Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base);

    public abstract Class<?> getType(ELContext context, Object base, Object property);

    public abstract Object getValue(ELContext context, Object base, Object property);

    public abstract boolean isReadOnly(ELContext context, Object base, Object property);

    public abstract void setValue(ELContext context, Object base, Object property, Object value);

}
