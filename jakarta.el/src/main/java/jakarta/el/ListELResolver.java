package jakarta.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Defines property resolution behavior on instances of {@link java.util.List}.
 *
 * <p>
 * This resolver handles base objects of type <code>java.util.List</code>. It accepts any object as a property and
 * coerces that object into an integer index into the list. The resulting value is the value in the list at that index.
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
 * @see java.util.List
 * @since Jakarta Server Pages 2.1
 */
public class ListELResolver extends ELResolver {
    private final boolean readyOnly;

    /**
     * Creates a new read/write <code>ListELResolver</code>.
     */
    public ListELResolver() {
        this(false);
    }

    /**
     * Creates a new <code>ListELResolver</code> whose read-only status is determined by the given parameter.
     *
     * @param isReadOnly <code>true</code> if this resolver cannot modify lists; <code>false</code> otherwise.
     */
    public ListELResolver(boolean isReadOnly) {
        readyOnly = isReadOnly;
    }

    /**
     * If the base object is a list, returns the most general type that this resolver accepts for the <code>property</code>
     * argument. Otherwise, returns <code>null</code>.
     *
     * <p>
     * Assuming the base is a <code>List</code>, this method will always return <code>Integer.class</code>. This is because
     * <code>List</code>s accept integers as their index.
     * </p>
     *
     * @param context The context of this evaluation.
     * @param base    The list to analyze. Only bases of type <code>List</code> are handled by this resolver.
     * @return <code>null</code> if base is not a <code>List</code>; otherwise <code>Integer.class</code>.
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base instanceof List<?>)
            return Integer.class;
        return null;
    }

    /**
     * Always returns <code>null</code>, since there is no reason to iterate through set set of all integers.
     *
     * <p>
     * The {@link #getCommonPropertyType} method returns sufficient information about what properties this resolver accepts.
     * </p>
     *
     * @param context The context of this evaluation.
     * @param base    The list. Only bases of type <code>List</code> are handled by this resolver.
     * @return <code>null</code>.
     */
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    /**
     * If the base object is a list, returns the most general acceptable type for a value in this list.
     *
     * <p>
     * If the base is a <code>List</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * Assuming the base is a <code>List</code>, this method will always return <code>Object.class</code>. This is because
     * <code>List</code>s accept any object as an element.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The list to analyze. Only bases of type <code>List</code> are handled by this resolver.
     * @param property The index of the element in the list to return the acceptable type for. Will be coerced into an
     *                 integer, but otherwise ignored by this resolver.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * the most general acceptable type; otherwise undefined.
     * @throws PropertyNotFoundException if the given index is out of bounds for this list.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        if (!(base instanceof List<?>))
            return null;
        context.setPropertyResolved(base, property);
        int index = toString(property);
        if (index < 0 || index >= ((List<?>) base).size()) {
            throw new PropertyNotFoundException();
        }
        return Object.class;
    }

    /**
     * If the base object is a list, returns the value at the given index. The index is specified by the
     * <code>property</code> argument, and coerced into an integer. If the coercion could not be performed, an
     * <code>IllegalArgumentException</code> is thrown. If the index is out of bounds, <code>null</code> is returned.
     *
     * <p>
     * If the base is a <code>List</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The list to be analyzed. Only bases of type <code>List</code> are handled by this resolver.
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
        if (!(base instanceof List<?>))
            return null;
        context.setPropertyResolved(base, property);
        int index = toString(property);
        if (index < 0 || index >= ((List<?>) base).size()) {
            return null;
        }
        return ((List<?>) base).get(index);
    }

    /**
     * If the base object is a list, returns whether a call to {@link #setValue} will always fail.
     *
     * <p>
     * If the base is a <code>List</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * If this resolver was constructed in read-only mode, this method will always return <code>true</code>.
     * </p>
     *
     * <p>
     * If a <code>List</code> was created using {@link java.util.Collections#unmodifiableList}, this method must return
     * <code>true</code>. Unfortunately, there is no Collections API method to detect this. However, an implementation can
     * create a prototype unmodifiable <code>List</code> and query its runtime type to see if it matches the runtime type of
     * the base object as a workaround.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The list to analyze. Only bases of type <code>List</code> are handled by this resolver.
     * @param property The index of the element in the list to return the acceptable type for. Will be coerced into an
     *                 integer, but otherwise ignored by this resolver.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * <code>true</code> if calling the <code>setValue</code> method will always fail or <code>false</code> if it is
     * possible that such a call may succeed; otherwise undefined.
     * @throws PropertyNotFoundException if the given index is out of bounds for this list.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        if (!(base instanceof List<?>))
            return false;
        context.setPropertyResolved(base, property);
        int index = toString(property);
        if (index < 0 || index >= ((List<?>) base).size()) {
            throw new PropertyNotFoundException();
        }
        if (readyOnly)
            return true;
        try {
            ((List<?>) base).add(null);
            return false;
        } catch (Exception ignored) {
            return true;
        }
    }

    /**
     * If the base object is a list, attempts to set the value at the given index with the given value. The index is
     * specified by the <code>property</code> argument, and coerced into an integer. If the coercion could not be performed,
     * an <code>IllegalArgumentException</code> is thrown. If the index is out of bounds, a
     * <code>PropertyNotFoundException</code> is thrown.
     *
     * <p>
     * If the base is a <code>List</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller can safely assume no value was set.
     * </p>
     *
     * <p>
     * If this resolver was constructed in read-only mode, this method will always throw
     * <code>PropertyNotWritableException</code>.
     * </p>
     *
     * <p>
     * If a <code>List</code> was created using {@link java.util.Collections#unmodifiableList}, this method must throw
     * <code>PropertyNotWritableException</code>. Unfortunately, there is no Collections API method to detect this. However,
     * an implementation can create a prototype unmodifiable <code>List</code> and query its runtime type to see if it
     * matches the runtime type of the base object as a workaround.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The list to be modified. Only bases of type <code>List</code> are handled by this resolver.
     * @param property The index of the value to be set. Will be coerced into an integer.
     * @param value    The value to be set at the given index.
     * @throws ClassCastException           if the class of the specified element prevents it from being added to this list.
     * @throws NullPointerException         if context is <code>null</code>, or if the value is <code>null</code> and this
     *                                      <code>List</code> does not support <code>null</code> elements.
     * @throws IllegalArgumentException     if the property could not be coerced into an integer, or if some aspect of the
     *                                      specified element prevents it from being added to this list.
     * @throws PropertyNotWritableException if this resolver was constructed in read-only mode, or if the set operation is
     *                                      not supported by the underlying list.
     * @throws PropertyNotFoundException    if the given index is out of bounds for this list.
     * @throws ELException                  if an exception was thrown while performing the property or variable resolution. The thrown
     *                                      exception must be included as the cause property of this exception, if available.
     */
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        Objects.requireNonNull(context);
        if (!(base instanceof List<?>))
            return;
        context.setPropertyResolved(base, property);
        int index = toString(property);
        if (index < 0 || index >= ((List<?>) base).size()) {
            throw new PropertyNotFoundException();
        }
        if (readyOnly)
            throw new PropertyNotWritableException();
        try {
            ((List<Object>) base).set(index, value);
        } catch (Exception ignored) {
            throw new PropertyNotWritableException();
        }
    }

    private int toString(Object value) {
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
