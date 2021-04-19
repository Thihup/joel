package jakarta.el;

import java.beans.FeatureDescriptor;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Objects;

/**
 * Defines property resolution behavior on arrays.
 *
 * <p>
 * This resolver handles base objects that are Java language arrays. It accepts any object as a property and coerces
 * that object into an integer index into the array. The resulting value is the value in the array at that index.
 * </p>
 *
 * <p>
 * This resolver can be constructed in read-only mode, which means that {@link #isReadOnly} will always return
 * <code>true</code> and {@link #setValue} will always throw <code>PropertyNotWritableException</code>.
 * </p>
 *
 * <p>
 * <code>ELResolver</code>s are combined together using {@link CompositeELResolver}s, to define rich semantics for
 * evaluating an expression. See the javadocs for {@link ELResolver} for details.
 * </p>
 *
 * @see CompositeELResolver
 * @see ELResolver
 * @since Jakarta Server Pages 2.1
 */
public class ArrayELResolver extends ELResolver {
    private final boolean readyOnly;

    /**
     * Creates a new read/write <code>ArrayELResolver</code>.
     */
    public ArrayELResolver() {
        this(false);
    }

    /**
     * Creates a new <code>ArrayELResolver</code> whose read-only status is determined by the given parameter.
     *
     * @param isReadOnly <code>true</code> if this resolver cannot modify arrays; <code>false</code> otherwise.
     */
    public ArrayELResolver(boolean isReadOnly) {
        readyOnly = isReadOnly;
    }

    /**
     * If the base object is a Java language array, returns the most general type that this resolver accepts for the
     * <code>property</code> argument. Otherwise, returns <code>null</code>.
     *
     * <p>
     * Assuming the base is an array, this method will always return <code>Integer.class</code>. This is because arrays
     * accept integers for their index.
     * </p>
     *
     * @param context The context of this evaluation.
     * @param base    The array to analyze. Only bases that are a Java language array are handled by this resolver.
     * @return <code>null</code> if base is not a Java language array; otherwise <code>Integer.class</code>.
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base == null)
            return null;
        if (!base.getClass().isArray())
            return null;
        return Integer.class;
    }

    /**
     * Always returns <code>null</code>, since there is no reason to iterate through set set of all integers.
     *
     * <p>
     * The {@link #getCommonPropertyType} method returns sufficient information about what properties this resolver accepts.
     * </p>
     *
     * @param context The context of this evaluation.
     * @param base    The array to analyze. Only bases that are a Java language array are handled by this resolver.
     * @return <code>null</code>.
     */
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    /**
     * If the base object is an array, returns the most general acceptable type for a value in this array.
     *
     * <p>
     * If the base is a <code>array</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * Assuming the base is an <code>array</code>, this method will always return
     * <code>base.getClass().getComponentType()</code>, which is the most general type of component that can be stored at
     * any given index in the array.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The array to analyze. Only bases that are Java language arrays are handled by this resolver.
     * @param property The index of the element in the array to return the acceptable type for. Will be coerced into an
     *                 integer, but otherwise ignored by this resolver.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * the most general acceptable type; otherwise undefined.
     * @throws PropertyNotFoundException if the given index is out of bounds for this array.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        if (base == null)
            return null;
        if (!base.getClass().isArray())
            return null;
        context.setPropertyResolved(base, property);
        checkBounds(Array.getLength(base), toInt(property));
        return base.getClass().getComponentType();
    }

    /**
     * If the base object is a Java language array, returns the value at the given index. The index is specified by the
     * <code>property</code> argument, and coerced into an integer. If the coercion could not be performed, an
     * <code>IllegalArgumentException</code> is thrown. If the index is out of bounds, <code>null</code> is returned.
     *
     * <p>
     * If the base is a Java language array, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The array to analyze. Only bases that are Java language arrays are handled by this resolver.
     * @param property The index of the value to be returned. Will be coerced into an integer.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * the value at the given index or <code>null</code> if the index was out of bounds. Otherwise, undefined.
     * @throws IllegalArgumentException if the property could not be coerced into an integer.
     * @throws NullPointerException     if context is <code>null</code>.
     * @throws ELException              if an exception was thrown while performing the property or variable resolution. The thrown
     *                                  exception must be included as the cause property of this exception, if available.
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        if (base == null)
            return null;
        if (!base.getClass().isArray())
            return null;
        int index = toInt(property);
        context.setPropertyResolved(base, property);
        if (index < 0 || index >= Array.getLength(base))
            return null;
        return Array.get(base, index);
    }

    /**
     * If the base object is a Java language array, returns whether a call to {@link #setValue} will always fail.
     *
     * <p>
     * If the base is a Java language array, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * If this resolver was constructed in read-only mode, this method will always return <code>true</code>. Otherwise, it
     * returns <code>false</code>.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The array to analyze. Only bases that are a Java language array are handled by this resolver.
     * @param property The index of the element in the array to return the acceptable type for. Will be coerced into an
     *                 integer, but otherwise ignored by this resolver.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * <code>true</code> if calling the <code>setValue</code> method will always fail or <code>false</code> if it is
     * possible that such a call may succeed; otherwise undefined.
     * @throws PropertyNotFoundException if the given index is out of bounds for this array.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        if (base == null)
            return false;
        if (!base.getClass().isArray())
            return false;
        context.setPropertyResolved(base, property);
        checkBounds(Array.getLength(base), toInt(property));
        return readyOnly;
    }

    /**
     * If the base object is a Java language array, attempts to set the value at the given index with the given value. The
     * index is specified by the <code>property</code> argument, and coerced into an integer. If the coercion could not be
     * performed, an <code>IllegalArgumentException</code> is thrown. If the index is out of bounds, a
     * <code>PropertyNotFoundException</code> is thrown.
     *
     * <p>
     * If the base is a Java language array, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller can safely assume no value was set.
     * </p>
     *
     * <p>
     * If this resolver was constructed in read-only mode, this method will always throw
     * <code>PropertyNotWritableException</code>.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The array to be modified. Only bases that are Java language arrays are handled by this resolver.
     * @param property The index of the value to be set. Will be coerced into an integer.
     * @param value    The value to be set at the given index.
     * @throws ClassCastException           if the class of the specified element prevents it from being added to this array.
     * @throws NullPointerException         if context is <code>null</code>.
     * @throws IllegalArgumentException     if the property could not be coerced into an integer, or if some aspect of the
     *                                      specified element prevents it from being added to this array.
     * @throws PropertyNotWritableException if this resolver was constructed in read-only mode.
     * @throws PropertyNotFoundException    if the given index is out of bounds for this array.
     * @throws ELException                  if an exception was thrown while performing the property or variable resolution. The thrown
     *                                      exception must be included as the cause property of this exception, if available.
     */
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        Objects.requireNonNull(context);
        if (base == null)
            return;
        if (!base.getClass().isArray())
            return;
        context.setPropertyResolved(base, property);
        if (readyOnly) {
            throw new PropertyNotWritableException();
        }
        checkBounds(Array.getLength(base), toInt(property));
        if (!base.getClass().getComponentType().isAssignableFrom(value.getClass()))
            throw new ClassCastException(String.format("Cannot cast %s to %s", value.getClass(), base.getClass().getComponentType()));
        Array.set(base, toInt(property), value);
    }

    private void checkBounds(int length, int index) {
        if (index < 0 || index >= length)
            throw new PropertyNotFoundException(String.format("Index %d is out of bounds; array length: %d", index, length));
    }

    private int toInt(Object property) {
        try {
            return Integer.parseInt(property.toString());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

}
