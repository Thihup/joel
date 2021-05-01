package jakarta.el;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.beans.BeansLinker;
import jdk.dynalink.support.ChainedCallSite;

import java.beans.FeatureDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * Defines property resolution behavior on objects using the JavaBeans component architecture.
 *
 * <p>
 * This resolver handles base objects of any type, as long as the base is not <code>null</code>. It accepts any object
 * as a property or method, and coerces it to a string.
 *
 * <p>
 * For property resolution, the property string is used to find a JavaBeans compliant property on the base object. The
 * value is accessed using JavaBeans getters and setters.
 * </p>
 *
 * <p>
 * For method resolution, the method string is the name of the method in the bean. The parameter types can be optionally
 * specified to identify the method. If the parameter types are not specified, the parameter objects are used in the
 * method resolution.
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
 * <p>
 * Because this resolver handles base objects of any type, it should be placed near the end of a composite resolver.
 * Otherwise, it will claim to have resolved a property before any resolvers that come after it get a chance to test if
 * they can do so as well.
 * </p>
 *
 * @see CompositeELResolver
 * @see ELResolver
 * @since Jakarta Server Pages 2.1
 */
public class BeanELResolver extends ELResolver {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Module THIS_MODULE = BeanELResolver.class.getModule();
    private static final DynamicLinker LINKER = new DynamicLinkerFactory().createLinker();
    private static final CallSiteDescriptor GETTER_DESCRIPTOR = new CallSiteDescriptor(LOOKUP,
            StandardOperation.GET.withNamespaces(StandardNamespace.PROPERTY),
            MethodType.methodType(Object.class, Object.class, String.class));
    private static final CallSiteDescriptor SETTER_DESCRIPTOR = new CallSiteDescriptor(LOOKUP,
            StandardOperation.SET.withNamespaces(StandardNamespace.PROPERTY),
            MethodType.methodType(void.class, Object.class, String.class, Object.class));

    private static final CallSiteDescriptor METHOD_DESCRIPTOR = new CallSiteDescriptor(LOOKUP,
            StandardOperation.GET.withNamespaces(StandardNamespace.METHOD),
            MethodType.methodType(Object.class, Object.class, String.class));

    private static final CallSiteDescriptor CALL_NO_ARGS_DESCRIPTOR = new CallSiteDescriptor(LOOKUP, StandardOperation.CALL,
            MethodType.methodType(Object.class, Object.class, Object.class));

    private static final MethodHandle INVOKE_GETTER;
    private static final MethodHandle INVOKE_SETTER;
    private static final MethodHandle INVOKE_METHOD;
    private static final MethodHandle INVOKE_CALL_NO_ARGS;
    private static final ClassValue<Void> MODULE_CACHE = new ClassValue<Void>() {
        @Override
        protected Void computeValue(Class<?> type) {
            var module = type.getModule();
            if (!THIS_MODULE.canRead(module))
                THIS_MODULE.addReads(module);
            return null;
        }
    };


    static {
        INVOKE_GETTER = LINKER.link(new ChainedCallSite(GETTER_DESCRIPTOR)).dynamicInvoker();
        INVOKE_SETTER = LINKER.link(new ChainedCallSite(SETTER_DESCRIPTOR)).dynamicInvoker();
        INVOKE_METHOD = LINKER.link(new ChainedCallSite(METHOD_DESCRIPTOR)).dynamicInvoker();
        INVOKE_CALL_NO_ARGS = LINKER.link(new ChainedCallSite(CALL_NO_ARGS_DESCRIPTOR)).dynamicInvoker();
    }

    private final boolean readyOnly;

    /**
     * Creates a new read/write <code>BeanELResolver</code>.
     */
    public BeanELResolver() {
        this(false);
    }

    /**
     * Creates a new <code>BeanELResolver</code> whose read-only status is determined by the given parameter.
     *
     * @param isReadOnly <code>true</code> if this resolver cannot modify beans; <code>false</code> otherwise.
     */
    public BeanELResolver(boolean isReadOnly) {
        readyOnly = isReadOnly;
    }

    /**
     * If the base object is not <code>null</code>, returns the most general type that this resolver accepts for the
     * <code>property</code> argument. Otherwise, returns <code>null</code>.
     *
     * <p>
     * Assuming the base is not <code>null</code>, this method will always return <code>Object.class</code>. This is because
     * any object is accepted as a key and is coerced into a string.
     * </p>
     *
     * @param context The context of this evaluation.
     * @param base    The bean to analyze.
     * @return <code>null</code> if base is <code>null</code>; otherwise <code>Object.class</code>.
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base == null)
            return null;
        return Object.class;
    }

    /**
     * If the base object is not <code>null</code>, returns an <code>Iterator</code> containing the set of JavaBeans
     * properties available on the given object. Otherwise, returns <code>null</code>.
     *
     * <p>
     * The <code>Iterator</code> returned must contain zero or more instances of {@link java.beans.FeatureDescriptor}. Each
     * info object contains information about a property in the bean, as obtained by calling the
     * <code>BeanInfo.getPropertyDescriptors</code> method. The <code>FeatureDescriptor</code> is initialized using the same
     * fields as are present in the <code>PropertyDescriptor</code>, with the additional required named attributes
     * "<code>type</code>" and "<code>resolvableAtDesignTime</code>" set as follows:
     * <ul>
     * <li>{@link ELResolver#TYPE} - The runtime type of the property, from
     * <code>PropertyDescriptor.getPropertyType()</code>.</li>
     * <li>{@link ELResolver#RESOLVABLE_AT_DESIGN_TIME} - <code>true</code>.</li>
     * </ul>
     *
     * @param context The context of this evaluation.
     * @param base    The bean to analyze.
     * @return An <code>Iterator</code> containing zero or more <code>FeatureDescriptor</code> objects, each representing a
     * property on this bean, or <code>null</code> if the <code>base</code> object is <code>null</code>.
     */
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        if (base == null)
            return null;
        return Collections.emptyIterator();
    }

    /**
     * If the base object is not <code>null</code>, returns the most general acceptable type that can be set on this bean
     * property.
     *
     * <p>
     * If the base is not <code>null</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * The provided property will first be coerced to a <code>String</code>. If there is a <code>BeanInfoProperty</code> for
     * this property and there were no errors retrieving it, the <code>propertyType</code> of the
     * <code>propertyDescriptor</code> is returned. Otherwise, a <code>PropertyNotFoundException</code> is thrown.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The bean to analyze.
     * @param property The name of the property to analyze. Will be coerced to a <code>String</code>.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * the most general acceptable type; otherwise undefined.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws PropertyNotFoundException if <code>base</code> is not <code>null</code> and the specified property does not
     *                                   exist or is not readable.
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        if (base == null || property == null)
            return null;
        Class<?> aClass = base.getClass();
        addReads(aClass);
        if (!BeansLinker.getReadableInstancePropertyNames(aClass).contains(property.toString()))
            throw new PropertyNotFoundException("Property %s not found in %s".formatted(property, aClass));
        try {
            var result = INVOKE_GETTER.invokeExact(base, property.toString());
            context.setPropertyResolved(base, property);
            return result.getClass();
        } catch (Throwable throwable) {
            throw new ELException("Error getting property type %s.%s".formatted(aClass, property), throwable);
        }
    }

    /**
     * If the base object is not <code>null</code>, returns the current value of the given property on this bean.
     *
     * <p>
     * If the base is not <code>null</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * The provided property name will first be coerced to a <code>String</code>. If the property is a readable property of
     * the base object, as per the JavaBeans specification, then return the result of the getter call. If the getter throws
     * an exception, it is propagated to the caller. If the property is not found or is not readable, a
     * <code>PropertyNotFoundException</code> is thrown.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The bean on which to get the property.
     * @param property The name of the property to get. Will be coerced to a <code>String</code>.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * the value of the given property. Otherwise, undefined.
     * @throws NullPointerException      if context is <code>null</code>.
     * @throws PropertyNotFoundException if <code>base</code> is not <code>null</code> and the specified property does not
     *                                   exist or is not readable.
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        if (base == null || property == null)
            return null;
        Class<?> aClass = base.getClass();
        addReads(aClass);
        if (!BeansLinker.getReadableInstancePropertyNames(aClass).contains(property.toString()))
            throw new PropertyNotFoundException("Property %s not found in %s".formatted(property, aClass));
        try {
            var result = INVOKE_GETTER.invokeExact(base, property.toString());
            context.setPropertyResolved(base, property);
            return result;
        } catch (Throwable throwable) {
            throw new ELException("Error getting property %s.%s".formatted(aClass, property), throwable);
        }
    }

    /**
     * If the base object is not <code>null</code>, returns whether a call to {@link #setValue} will always fail.
     *
     * <p>
     * If the base is not <code>null</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller can safely assume no value was set.
     * </p>
     *
     * <p>
     * If this resolver was constructed in read-only mode, this method will always return <code>true</code>.
     * </p>
     *
     * <p>
     * The provided property name will first be coerced to a <code>String</code>. If property is a writable property of
     * <code>base</code>, <code>false</code> is returned. If the property is found but is not writable, <code>true</code> is
     * returned. If the property is not found, a <code>PropertyNotFoundException</code> is thrown.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The bean to analyze.
     * @param property The name of the property to analyzed. Will be coerced to a <code>String</code>.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * <code>true</code> if calling the <code>setValue</code> method will always fail or <code>false</code> if it is
     * possible that such a call may succeed; otherwise undefined.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws PropertyNotFoundException if <code>base</code> is not <code>null</code> and the specified property does not
     *                                   exist.
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(context);
        if (base == null || property == null)
            return false;
        if (readyOnly)
            throw new PropertyNotWritableException();
        Class<?> aClass = base.getClass();
        addReads(aClass);
        if (!BeansLinker.getReadableInstancePropertyNames(aClass).contains(property.toString()))
            throw new PropertyNotFoundException("Property %s not found in %s".formatted(property, base.getClass()));
        context.setPropertyResolved(base, property);
        return !BeansLinker.getWritableInstancePropertyNames(aClass).contains(property.toString());
    }

    /**
     * If the base object is not <code>null</code>, invoke the method, with the given parameters on this bean. The return
     * value from the method is returned.
     *
     * <p>
     * If the base is not <code>null</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
     * must be set to <code>true</code> by this resolver, before returning. If this property is not <code>true</code> after
     * this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * The provided method object will first be coerced to a <code>String</code>. The methods in the bean is then examined
     * and an attempt will be made to select one for invocation. If no suitable can be found, a
     * <code>MethodNotFoundException</code> is thrown.
     * <p>
     * If the given paramTypes is not <code>null</code>, select the method with the given name and parameter types.
     * <p>
     * Else select the method with the given name that has the same number of parameters. If there are more than one such
     * method, the method selection process is undefined.
     * <p>
     * Else select the method with the given name that takes a variable number of arguments.
     * <p>
     * Note the resolution for overloaded methods will likely be clarified in a future version of the spec.
     * <p>
     * The provide parameters are coerced to the corresponding parameter types of the method, and the method is then
     * invoked.
     *
     * @param context        The context of this evaluation.
     * @param base           The bean on which to invoke the method
     * @param methodName     The simple name of the method to invoke. Will be coerced to a <code>String</code>. If method is
     *                       "&lt;init&gt;"or "&lt;clinit&gt;" a MethodNotFoundException is thrown.
     * @param parameterTypes An array of Class objects identifying the method's formal parameter types, in declared order. Use
     *                       an empty array if the method has no parameters. Can be <code>null</code>, in which case the method's formal parameter
     *                       types are assumed to be unknown.
     * @param params         The parameters to pass to the method, or <code>null</code> if no parameters.
     * @return The result of the method invocation (<code>null</code> if the method has a <code>void</code> return type).
     * @throws MethodNotFoundException if no suitable method can be found.
     * @throws ELException             if an exception was thrown while performing (base, method) resolution. The thrown exception must
     *                                 be included as the cause property of this exception, if available. If the exception thrown is an
     *                                 <code>InvocationTargetException</code>, extract its <code>cause</code> and pass it to the <code>ELException</code>
     *                                 constructor.
     * @since Jakarta Expression Language 2.2
     */
    @Override
    public Object invoke(ELContext context, Object base, Object methodName, Class<?>[] parameterTypes, Object[]
            params) {
        Objects.requireNonNull(context);
        if (base == null || methodName == null)
            return null;
        Class<?> aClass = base.getClass();
        addReads(aClass);
        if (!BeansLinker.getInstanceMethodNames(aClass).contains(methodName.toString()))
            throw new MethodNotFoundException("Method %s not found in %s".formatted(methodName, aClass));
        try {
            var o = INVOKE_METHOD.invokeExact(base, methodName.toString());
            context.setPropertyResolved(base, methodName);
            var paramsLength = (params == null) ? 0 : params.length;
            if (paramsLength == 0) {
                return INVOKE_CALL_NO_ARGS.invokeExact(o, base);
            }
            var callSiteDescriptor = new CallSiteDescriptor(LOOKUP, StandardOperation.CALL, MethodType.genericMethodType(2 + paramsLength));
            return LINKER.link(new ChainedCallSite(callSiteDescriptor))
                .dynamicInvoker()
                .asSpreader(Object[].class, paramsLength)
                .invokeExact(o, base, params);
        } catch (Throwable e) {
            throw new ELException(e);
        }
    }

    /**
     * If the base object is not <code>null</code>, attempts to set the value of the given property on this bean.
     *
     * <p>
     * If the base is not <code>null</code>, the <code>propertyResolved</code> property of the <code>ELContext</code> object
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
     * The provided property name will first be coerced to a <code>String</code>. If property is a writable property of
     * <code>base</code> (as per the JavaBeans Specification), the setter method is called (passing <code>value</code>). If
     * the property exists but does not have a setter, then a <code>PropertyNotFoundException</code> is thrown. If the
     * property does not exist, a <code>PropertyNotFoundException</code> is thrown.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The bean on which to set the property.
     * @param property The name of the property to set. Will be coerced to a <code>String</code>.
     * @param value    The value to be associated with the specified key.
     * @throws NullPointerException         if context is <code>null</code>.
     * @throws PropertyNotFoundException    if <code>base</code> is not <code>null</code> and the specified property does not
     *                                      exist.
     * @throws PropertyNotWritableException if this resolver was constructed in read-only mode, or if there is no setter for
     *                                      the property.
     * @throws ELException                  if an exception was thrown while performing the property or variable resolution. The thrown
     *                                      exception must be included as the cause property of this exception, if available.
     */
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        Objects.requireNonNull(context);
        if (base == null || property == null)
            return;
        if (readyOnly)
            throw new PropertyNotWritableException();
        Class<?> aClass = base.getClass();
        addReads(aClass);
        var propertyName = property.toString();
        if (!BeansLinker.getReadableInstancePropertyNames(aClass).contains(propertyName))
            throw new PropertyNotFoundException("Property %s not found in %s".formatted(property, base.getClass()));

        try {
            INVOKE_SETTER.invokeExact(base, propertyName, value);
        } catch (Throwable throwable) {
            throw new PropertyNotWritableException(throwable);
        }
    }

    private void addReads(Class<?> klass) {
        MODULE_CACHE.get(klass);
    }
}
