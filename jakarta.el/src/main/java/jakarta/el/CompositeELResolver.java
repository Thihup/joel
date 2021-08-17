package jakarta.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Maintains an ordered composite list of child <code>ELResolver</code>s.
 *
 * <p>
 * Though only a single <code>ELResolver</code> is associated with an <code>ELContext</code>, there are usually multiple
 * resolvers considered for any given variable or property resolution. <code>ELResolver</code>s are combined together
 * using a <code>CompositeELResolver</code>, to define rich semantics for evaluating an expression.
 * </p>
 *
 * <p>
 * For the {@link #getValue}, {@link #getType}, {@link #setValue} and {@link #isReadOnly} methods, an
 * <code>ELResolver</code> is not responsible for resolving all possible (base, property) pairs. In fact, most resolvers
 * will only handle a <code>base</code> of a single type. To indicate that a resolver has successfully resolved a
 * particular (base, property) pair, it must set the <code>propertyResolved</code> property of the
 * <code>ELContext</code> to <code>true</code>. If it could not handle the given pair, it must leave this property
 * alone. The caller must ignore the return value of the method if <code>propertyResolved</code> is <code>false</code>.
 * </p>
 *
 * <p>
 * The <code>CompositeELResolver</code> initializes the <code>ELContext.propertyResolved</code> flag to
 * <code>false</code>, and uses it as a stop condition for iterating through its component resolvers.
 * </p>
 *
 * <p>
 * The <code>ELContext.propertyResolved</code> flag is not used for the design-time methods
 * {@link #getFeatureDescriptors} and {@link #getCommonPropertyType}. Instead, results are collected and combined from
 * all child <code>ELResolver</code>s for these methods.
 * </p>
 *
 * @see ELContext
 * @see ELResolver
 * @since Jakarta Server Pages 2.1
 */
public class CompositeELResolver extends ELResolver {

    private final List<ELResolver> resolvers = new ArrayList<>();

    /**
     * Adds the given resolver to the list of component resolvers.
     *
     * <p>
     * Resolvers are consulted in the order in which they are added.
     * </p>
     *
     * @param elResolver The component resolver to add.
     * @throws NullPointerException If the provided resolver is <code>null</code>.
     */
    public void add(ELResolver elResolver) {
        resolvers.add(Objects.requireNonNull(elResolver));
    }

    /**
     * Returns the most general type that this resolver accepts for the <code>property</code> argument, given a
     * <code>base</code> object. One use for this method is to assist tools in auto-completion. The result is obtained by
     * querying all component resolvers.
     *
     * <p>
     * The <code>Class</code> returned is the most specific class that is a common superclass of all the classes returned by
     * each component resolver's <code>getCommonPropertyType</code> method. If <code>null</code> is returned by a resolver,
     * it is skipped.
     *
     * @param context The context of this evaluation.
     * @param base    The base object to return the most general property type for, or <code>null</code> to enumerate the set
     *                of top-level variables that this resolver can evaluate.
     * @return <code>null</code> if this <code>ELResolver</code> does not know how to handle the given <code>base</code>
     * object; otherwise <code>Object.class</code> if any type of <code>property</code> is accepted; otherwise the most
     * general <code>property</code> type accepted for the given <code>base</code>.
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return resolvers.stream()
                .map(x -> Optional.ofNullable(x.getCommonPropertyType(context, base)))
                .flatMap(Optional::stream)
                .min((x, y) -> x == y ? 0 : x.isAssignableFrom(y) ? -1 : 1)
                .orElse(null);
    }

    /**
     * Returns information about the set of variables or properties that can be resolved for the given <code>base</code>
     * object. One use for this method is to assist tools in auto-completion. The results are collected from all component
     * resolvers.
     *
     * <p>
     * The <code>propertyResolved</code> property of the <code>ELContext</code> is not relevant to this method. The results
     * of all <code>ELResolver</code>s are concatenated.
     * </p>
     *
     * <p>
     * The <code>Iterator</code> returned is an iterator over the collection of <code>FeatureDescriptor</code> objects
     * returned by the iterators returned by each component resolver's <code>getFeatureDescriptors</code> method. If
     * <code>null</code> is returned by a resolver, it is skipped.
     * </p>
     *
     * @param context The context of this evaluation.
     * @param base    The base object whose set of valid properties is to be enumerated, or <code>null</code> to enumerate the
     *                set of top-level variables that this resolver can evaluate.
     * @return An <code>Iterator</code> containing zero or more (possibly infinitely more) <code>FeatureDescriptor</code>
     * objects, or <code>null</code> if this resolver does not handle the given <code>base</code> object or that the results
     * are too complex to represent with this method
     */
    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return resolvers.stream()
                .map(x -> x.getFeatureDescriptors(context, base))
                .filter(Objects::nonNull)
                .flatMap(x -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(x, 0), false))
                .iterator();
    }

    /**
     * For a given <code>base</code> and <code>property</code>, attempts to identify the most general type that is
     * acceptable for an object to be passed as the <code>value</code> parameter in a future call to the {@link #setValue}
     * method. The result is obtained by querying all component resolvers.
     *
     * <p>
     * If this resolver handles the given (base, property) pair, the <code>propertyResolved</code> property of the
     * <code>ELContext</code> object must be set to <code>true</code> by the resolver, before returning. If this property is
     * not <code>true</code> after this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * First, <code>propertyResolved</code> is set to <code>false</code> on the provided <code>ELContext</code>.
     * </p>
     *
     * <p>
     * Next, for each component resolver in this composite:
     * <ol>
     * <li>The <code>getType()</code> method is called, passing in the provided <code>context</code>, <code>base</code> and
     * <code>property</code>.</li>
     * <li>If the <code>ELContext</code>'s <code>propertyResolved</code> flag is <code>false</code> then iteration
     * continues.</li>
     * <li>Otherwise, iteration stops and no more component resolvers are considered. The value returned by
     * <code>getType()</code> is returned by this method.</li>
     * </ol>
     *
     * <p>
     * If none of the component resolvers were able to perform this operation, the value <code>null</code> is returned and
     * the <code>propertyResolved</code> flag remains set to <code>false</code>.
     * </p>
     *
     *
     * <p>
     * Any exception thrown by component resolvers during the iteration is propagated to the caller of this method.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The base object whose property value is to be analyzed, or <code>null</code> to analyze a top-level
     *                 variable.
     * @param property The property or variable to return the acceptable type for.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * the most general acceptable type; otherwise undefined.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws PropertyNotFoundException if the given (base, property) pair is handled by this <code>ELResolver</code> but
     *                                   the specified variable or property does not exist or is not readable.
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        context.setPropertyResolved(false);
        return resolvers.stream()
                .map(x -> Optional.ofNullable(x.getType(context, base, property)))
                .filter(x -> context.isPropertyResolved())
                .findFirst()
                .flatMap(Function.identity())
                .orElse(null);
    }

    /**
     * Attempts to resolve the given <code>property</code> object on the given <code>base</code> object by querying all
     * component resolvers.
     *
     * <p>
     * If this resolver handles the given (base, property) pair, the <code>propertyResolved</code> property of the
     * <code>ELContext</code> object must be set to <code>true</code> by the resolver, before returning. If this property is
     * not <code>true</code> after this method is called, the caller should ignore the return value.
     *
     * <p>
     * First, <code>propertyResolved</code> is set to <code>false</code> on the provided <code>ELContext</code>.
     *
     * <p>
     * Next, for each component resolver in this composite:
     * <ol>
     * <li>The <code>getValue()</code> method is called, passing in the provided <code>context</code>, <code>base</code> and
     * <code>property</code>.</li>
     * <li>If the <code>ELContext</code>'s <code>propertyResolved</code> flag is <code>false</code> then iteration
     * continues.</li>
     * <li>Otherwise, iteration stops and no more component resolvers are considered. The value returned by
     * <code>getValue()</code> is returned by this method.</li>
     * </ol>
     *
     * <p>
     * If none of the component resolvers were able to perform this operation, the value <code>null</code> is returned and
     * the <code>propertyResolved</code> flag remains set to <code>false</code>.
     *
     * <p>
     * Any exception thrown by component resolvers during the iteration is propagated to the caller of this method.
     *
     * @param context  The context of this evaluation.
     * @param base     The base object whose property value is to be returned, or <code>null</code> to resolve a top-level
     *                 variable.
     * @param property The property or variable to be resolved.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * the result of the variable or property resolution; otherwise undefined.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws PropertyNotFoundException if the given (base, property) pair is handled by this <code>ELResolver</code> but
     *                                   the specified variable or property does not exist or is not readable.
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        context.setPropertyResolved(false);
        return resolvers.stream()
                .map(x -> Optional.ofNullable(x.getValue(context, base, property)))
                .filter(x -> context.isPropertyResolved())
                .findFirst()
                .flatMap(Function.identity())
                .orElse(null);
    }

    /**
     * For a given <code>base</code> and <code>property</code>, attempts to determine whether a call to {@link #setValue}
     * will always fail. The result is obtained by querying all component resolvers.
     *
     * <p>
     * If this resolver handles the given (base, property) pair, the <code>propertyResolved</code> property of the
     * <code>ELContext</code> object must be set to <code>true</code> by the resolver, before returning. If this property is
     * not <code>true</code> after this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * First, <code>propertyResolved</code> is set to <code>false</code> on the provided <code>ELContext</code>.
     * </p>
     *
     * <p>
     * Next, for each component resolver in this composite:
     * <ol>
     * <li>The <code>isReadOnly()</code> method is called, passing in the provided <code>context</code>, <code>base</code>
     * and <code>property</code>.</li>
     * <li>If the <code>ELContext</code>'s <code>propertyResolved</code> flag is <code>false</code> then iteration
     * continues.</li>
     * <li>Otherwise, iteration stops and no more component resolvers are considered. The value returned by
     * <code>isReadOnly()</code> is returned by this method.</li>
     * </ol>
     *
     * <p>
     * If none of the component resolvers were able to perform this operation, the value <code>false</code> is returned and
     * the <code>propertyResolved</code> flag remains set to <code>false</code>
     * </p>
     * .
     *
     * <p>
     * Any exception thrown by component resolvers during the iteration is propagated to the caller of this method.
     * </p>
     *
     * @param context  The context of this evaluation.
     * @param base     The base object whose property value is to be analyzed, or <code>null</code> to analyze a top-level
     *                 variable.
     * @param property The property or variable to return the read-only status for.
     * @return If the <code>propertyResolved</code> property of <code>ELContext</code> was set to <code>true</code>, then
     * <code>true</code> if the property is read-only or <code>false</code> if not; otherwise undefined.
     * @throws NullPointerException      if context is <code>null</code>
     * @throws PropertyNotFoundException if the given (base, property) pair is handled by this <code>ELResolver</code> but
     *                                   the specified variable or property does not exist.
     * @throws ELException               if an exception was thrown while performing the property or variable resolution. The thrown
     *                                   exception must be included as the cause property of this exception, if available.
     */
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);
        context.setPropertyResolved(false);
        return resolvers.stream()
                .map(x -> x.isReadOnly(context, base, property))
                .filter(x -> context.isPropertyResolved())
                .findFirst()
                .orElse(false);
    }

    /**
     * Attempts to set the value of the given <code>property</code> object on the given <code>base</code> object. All
     * component resolvers are asked to attempt to set the value.
     *
     * <p>
     * If this resolver handles the given (base, property) pair, the <code>propertyResolved</code> property of the
     * <code>ELContext</code> object must be set to <code>true</code> by the resolver, before returning. If this property is
     * not <code>true</code> after this method is called, the caller can safely assume no value has been set.
     *
     * <p>
     * First, <code>propertyResolved</code> is set to <code>false</code> on the provided <code>ELContext</code>.
     *
     * <p>
     * Next, for each component resolver in this composite:
     * <ol>
     * <li>The <code>setValue()</code> method is called, passing in the provided <code>context</code>, <code>base</code>,
     * <code>property</code> and <code>value</code>.</li>
     * <li>If the <code>ELContext</code>'s <code>propertyResolved</code> flag is <code>false</code> then iteration
     * continues.</li>
     * <li>Otherwise, iteration stops and no more component resolvers are considered.</li>
     * </ol>
     *
     * <p>
     * If none of the component resolvers were able to perform this operation, the <code>propertyResolved</code> flag
     * remains set to <code>false</code>.
     *
     * <p>
     * Any exception thrown by component resolvers during the iteration is propagated to the caller of this method.
     *
     * @param context  The context of this evaluation.
     * @param base     The base object whose property value is to be set, or <code>null</code> to set a top-level variable.
     * @param property The property or variable to be set.
     * @param value    The value to set the property or variable to.
     * @throws NullPointerException         if context is <code>null</code>
     * @throws PropertyNotFoundException    if the given (base, property) pair is handled by this <code>ELResolver</code> but
     *                                      the specified variable or property does not exist.
     * @throws PropertyNotWritableException if the given (base, property) pair is handled by this <code>ELResolver</code>
     *                                      but the specified variable or property is not writable.
     * @throws ELException                  if an exception was thrown while attempting to set the property or variable. The thrown exception
     *                                      must be included as the cause property of this exception, if available.
     */
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        Objects.requireNonNull(context);
        context.setPropertyResolved(false);
        for (ELResolver resolver : resolvers) {
            resolver.setValue(context, base, property, value);
            if (context.isPropertyResolved())
                return;
        }
    }

    /**
     * Attempts to resolve and invoke the given <code>method</code> on the given <code>base</code> object by querying all
     * component resolvers.
     *
     * <p>
     * If this resolver handles the given (base, method) pair, the <code>propertyResolved</code> property of the
     * <code>ELContext</code> object must be set to <code>true</code> by the resolver, before returning. If this property is
     * not <code>true</code> after this method is called, the caller should ignore the return value.
     * </p>
     *
     * <p>
     * First, <code>propertyResolved</code> is set to <code>false</code> on the provided <code>ELContext</code>.
     * </p>
     *
     * <p>
     * Next, for each component resolver in this composite:
     * <ol>
     * <li>The <code>invoke()</code> method is called, passing in the provided <code>context</code>, <code>base</code>,
     * <code>method</code>, <code>paramTypes</code>, and <code>params</code>.</li>
     * <li>If the <code>ELContext</code>'s <code>propertyResolved</code> flag is <code>false</code> then iteration
     * continues.</li>
     * <li>Otherwise, iteration stops and no more component resolvers are considered. The value returned by
     * <code>invoke()</code> is returned by this method.</li>
     * </ol>
     *
     * <p>
     * If none of the component resolvers were able to perform this operation, the value <code>null</code> is returned and
     * the <code>propertyResolved</code> flag remains set to <code>false</code>
     * </p>
     * .
     *
     * <p>
     * Any exception thrown by component resolvers during the iteration is propagated to the caller of this method.
     * </p>
     *
     * @param context        The context of this evaluation.
     * @param base           The bean on which to invoke the method
     * @param method         The simple name of the method to invoke. Will be coerced to a <code>String</code>.
     * @param parameterTypes An array of Class objects identifying the method's formal parameter types, in declared order. Use
     *                       an empty array if the method has no parameters. Can be <code>null</code>, in which case the method's formal parameter
     *                       types are assumed to be unknown.
     * @param params         The parameters to pass to the method, or <code>null</code> if no parameters.
     * @return The result of the method invocation (<code>null</code> if the method has a <code>void</code> return type).
     * @since Jakarta Expression Language 2.2
     */
    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] parameterTypes, Object[] params) {
        context.setPropertyResolved(false);
        return resolvers.stream()
                .map(x -> Optional.ofNullable(x.invoke(context, base, method, parameterTypes, params)))
                .filter(x -> context.isPropertyResolved())
                .findFirst()
                .flatMap(Function.identity())
                .orElse(null);
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
     * @param targetType The target type for the convertion.
     * @throws ELException thrown if errors occur.
     * @since Jakarta Expression Language 3.0
     */
    @Override
    public Object convertToType(ELContext context, Object object, Class<?> targetType) {
        context.setPropertyResolved(false);
        return resolvers.stream()
                .map(x -> Optional.ofNullable(x.convertToType(context, object, targetType)))
                .filter(x -> context.isPropertyResolved())
                .findFirst()
                .flatMap(Function.identity())
                .orElse(null);
    }
}
