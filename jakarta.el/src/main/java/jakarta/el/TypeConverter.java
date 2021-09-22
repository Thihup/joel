package jakarta.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

/**
 * A convenient class for writing an ELResolver to do custom type conversions.
 *
 * <p>
 * For example, to convert a String to an instance of MyDate, one can write
 *
 * <pre>
 * <code>
 *     ELProcessor elp = new ELProcessor();
 *     elp.getELManager().addELResolver(new TypeConverter() {
 *         Object convertToType(ELContext context, Object obj, Class&lt;?&gt; type) {
 *             if ((obj instanceof String) &amp;&amp; type == MyDate.class) {
 *                 context.setPropertyResolved(obj, type);
 *                 return (obj == null)? null: new MyDate(obj.toString());
 *             }
 *             return null;
 *         }
 *      };
 * </code>
 * </pre>
 *
 * @since Jakarta Expression Language 3.0
 */
public abstract class TypeConverter extends ELResolver {

    public TypeConverter() {
    }

    /**
     * Converts an object to a specific type.
     *
     * <p>
     * An <code>ELException</code> is thrown if an error occurs during the conversion.
     * </p>
     *
     * @param context    The context of this evaluation.
     * @param object     The object to convert.
     * @param targetType The target type for the conversion.
     * @throws ELException thrown if errors occur.
     */
    @Override
    public abstract Object convertToType(ELContext context, Object object, Class<?> targetType);

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return false;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {

    }
}
