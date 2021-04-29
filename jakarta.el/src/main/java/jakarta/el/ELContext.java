package jakarta.el;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Context information for expression parsing and evaluation.
 *
 * <p>
 * To parse or evaluate an {@link Expression}, an <code>ELContext</code> must be provided. The <code>ELContext</code>
 * holds:
 * <ul>
 * <li>a reference to {@link FunctionMapper} that will be used to resolve Jakarta Expression Language Functions. This is
 * used only in parsing.</li>
 * <li>a reference to {@link VariableMapper} that will be used to resolve Jakarta Expression Language Variables. This is
 * used only in parsing.</li>
 * <li>a reference to the base {@link ELResolver} that will be consulted to resolve model objects and their
 * properties</li>
 * <li>a collection of all the relevant context objects for use by <code>ELResolver</code>s</li>
 * <li>state information during the evaluation of an expression, such as whether a property has been resolved yet</li>
 * <li>a reference to {@link ImportHandler} that will be consulted to resolve classes that have been imported</li>
 * <li>a reference to the arguments for the active {@link LambdaExpression}s</li>
 * <li>a reference to the list of registered evaluation listeners</li>
 * </ul>
 *
 * <p>
 * The collection of context objects is necessary because each <code>ELResolver</code> may need access to a different
 * context object. For example, Jakarta Server Pages and Jakarta Faces resolvers need access to a
 * <code>jakarta.servlet.jsp.JspContext</code> and a <code>jakarta.faces.context.FacesContext</code>, respectively.
 *
 * <p>
 * When used in a web container, the creation of <code>ELContext</code> objects is controlled through the underlying
 * technology. For example, in Jakarta Server Pages the <code>JspContext.getELContext()</code> factory method is used.
 * Some technologies provide the ability to add an {@link ELContextListener} so that applications and frameworks can
 * ensure their own context objects are attached to any newly created <code>ELContext</code>.
 *
 * <p>
 * When used in a stand-alone environment, {@link StandardELContext} provides a default <code>ELContext</code>, which is
 * managed and modified by {@link ELManager}.
 *
 * <p>
 * Because it stores state during expression evaluation, an <code>ELContext</code> object is not thread-safe. Care
 * should be taken to never share an <code>ELContext</code> instance between two or more threads.
 *
 * @see ELContextListener
 * @see ELContextEvent
 * @see ELResolver
 * @see FunctionMapper
 * @see VariableMapper
 * @see ImportHandler
 * @see LambdaExpression
 * @see StandardELContext
 * @since Jakarta Expression Language 2.1 and Jakarta Expression Language 3.0
 */
public abstract class ELContext {

    private final List<EvaluationListener> listeners = new ArrayList<>();
    private final Map<Class<?>, Object> contexts = new HashMap<>();
    private final Deque<Map<String, Object>> lambdaArguments = new ArrayDeque<>();
    private final ImportHandler importHandler = new ImportHandler();
    private Locale locale = Locale.getDefault();
    private boolean propertyResolved;

    /**
     * Registers an evaluation listener to the ELContext.
     *
     * @param listener The listener to be added.
     * @since Jakarta Expression Language 3.0
     */
    public void addEvaluationListener(EvaluationListener listener) {
        listeners.add(listener);
    }


    /**
     * Converts an object to a specific type. If a custom converter in the <code>ELResolver</code> handles this conversion,
     * it is used. Otherwise the standard coercions is applied.
     *
     * <p>
     * An <code>ELException</code> is thrown if an error occurs during the conversion.
     *
     * @param object     The object to convert.
     * @param targetType The target type for the conversion.
     * @return object converted to <code>targetType</code>
     * @throws ELException thrown if errors occur.
     * @since Jakarta Expression Language 3.0
     */
    public Object convertToType(Object object, Class<?> targetType) {
        boolean isCurrentResolved = isPropertyResolved();
        setPropertyResolved(false);
        try {
            Object t = getELResolver().convertToType(this, object, targetType);
            if (isPropertyResolved())
                return t;
        } finally {
            if (isCurrentResolved)
                setPropertyResolved(true);
        }
        ExpressionFactory factory = getFactory();
        return factory.coerceToType(object, targetType);
    }

    private ExpressionFactory getFactory() {
        var factory = (ExpressionFactory) contexts.get(ExpressionFactory.class);
        if (factory == null){
            var expressionFactory = ExpressionFactory.newInstance();
            contexts.put(ExpressionFactory.class, expressionFactory);
            return expressionFactory;
        }
        return factory;
    }

    /**
     * Installs a Lambda argument map, in preparation for the evaluation of a Lambda expression. The arguments in the map
     * will be in scope during the evaluation of the Lambda expression.
     *
     * @param arguments The Lambda arguments map
     * @since Jakarta Expression Language 3.0
     */
    public void enterLambdaScope(Map<String, Object> arguments) {
        lambdaArguments.addLast(arguments);
    }

    /**
     * Exits the Lambda expression evaluation. The Lambda argument map that was previously installed is removed.
     *
     * @since Jakarta Expression Language 3.0
     */
    public void exitLambdaScope() {
        lambdaArguments.removeLast();
    }

    /**
     * Returns the context object associated with the given key.
     *
     * <p>
     * The <code>ELContext</code> maintains a collection of context objects relevant to the evaluation of an expression.
     * These context objects are used by <code>ELResolver</code>s. This method is used to retrieve the context with the
     * given key from the collection.
     * </p>
     *
     * <p>
     * By convention, the object returned will be of the type specified by the <code>key</code>. However, this is not
     * required and the key is used strictly as a unique identifier.
     * </p>
     *
     * @param key The unique identifier that was used to associate the context object with this <code>ELContext</code>.
     * @return The context object associated with the given key, or null if no such context was found.
     * @throws NullPointerException if key is null.
     */
    public Object getContext(Class<?> key) {
        Objects.requireNonNull(key);
        return contexts.get(key);
    }

    /**
     * Returns the list of registered evaluation listeners.
     *
     * @return The list of registered evaluation listeners.
     * @since Jakarta Expression Language 3.0
     */
    public List<EvaluationListener> getEvaluationListeners() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Retrieves the <code>ImportHandler</code> associated with this <code>ELContext</code>.
     *
     * @return The import handler to manage imports of classes and packages.
     * @since Jakarta Expression Language 3.0
     */
    public ImportHandler getImportHandler() {
        return importHandler;
    }

    /**
     * Retrieves the Lambda argument associated with a formal parameter. If the Lambda expression is nested within other
     * Lambda expressions, the arguments for the current Lambda expression is first searched, and if not found, the
     * arguments for the immediate nesting Lambda expression then searched, and so on.
     *
     * @param argument The formal parameter for the Lambda argument
     * @return The object associated with formal parameter. Null if no object has been associated with the parameter.
     * @since Jakarta Expression Language 3.0
     */
    public Object getLambdaArgument(String argument) {
        return lambdaArguments.stream()
                .map(x -> x.get(argument))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the <code>Locale</code> stored by a previous invocation to {@link #setLocale}. If this method returns non
     * <code>null</code>, this <code>Locale</code> must be used for all localization needs in the implementation. The
     * <code>Locale</code> must not be cached to allow for applications that change <code>Locale</code> dynamically.
     *
     * @return The <code>Locale</code> in which this instance is operating. Used primarily for message localization.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the <code>Locale</code> for this instance.
     *
     * <p>
     * This method may be called by the party creating the instance, such as Jakarta Faces or Jakarta Server Pages, to
     * enable the Jakarta Expression Language implementation to provide localized messages to the user. If no
     * <code>Locale</code> is set, the implementation must use the locale returned by <code>Locale.getDefault( )</code>.
     *
     * @param locale the locale for this instance
     */
    public void setLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale);
    }

    /**
     * Inquires if the name is a LambdaArgument
     *
     * @param argument A possible Lambda formal parameter name
     * @return true if arg is a LambdaArgument, false otherwise.
     */
    public boolean isLambdaArgument(String argument) {
        return lambdaArguments.stream().anyMatch(x -> x.containsKey(argument));
    }

    /**
     * Returns whether an {@link ELResolver} has successfully resolved a given (base, property) pair.
     *
     * <p>
     * The {@link CompositeELResolver} checks this property to determine whether it should consider or skip other component
     * resolvers.
     *
     * @return true if the property has been resolved, or false if not.
     * @see CompositeELResolver
     */
    public boolean isPropertyResolved() {
        return propertyResolved;
    }

    /**
     * Called to indicate that a <code>ELResolver</code> has successfully resolved a given (base, property) pair. Use
     * {@link #setPropertyResolved(Object, Object)} if resolved is true and to notify {@link EvaluationListener}s.
     *
     * <p>
     * The {@link CompositeELResolver} checks this property to determine whether it should consider or skip other component
     * resolvers.
     * </p>
     *
     * @param resolved true if the property has been resolved, or false if not.
     * @see CompositeELResolver
     */
    public void setPropertyResolved(boolean resolved) {
        propertyResolved = resolved;
    }

    /**
     * Notifies the listeners after an Jakarta Expression Language expression is evaluated
     *
     * @param expression The Jakarta Expression Language expression string that has been evaluated
     */
    public void notifyAfterEvaluation(String expression) {
        listeners.forEach(e -> e.afterEvaluation(this, expression));
    }

    /**
     * Notifies the listeners before an Jakarta Expression Language expression is evaluated
     *
     * @param expression The Jakarta Expression Language expression string to be evaluated
     */
    public void notifyBeforeEvaluation(String expression) {
        listeners.forEach(e -> e.beforeEvaluation(this, expression));
    }

    /**
     * Notifies the listeners when the (base, property) pair is resolved
     *
     * @param base     The base object
     * @param property The property Object
     */
    public void notifyPropertyResolved(Object base, Object property) {
        listeners.forEach(e -> e.propertyResolved(this, base, property));
    }

    /**
     * Associates a context object with this <code>ELContext</code>.
     *
     * <p>
     * The <code>ELContext</code> maintains a collection of context objects relevant to the evaluation of an expression.
     * These context objects are used by <code>ELResolver</code>s. This method is used to add a context object to that
     * collection.
     * </p>
     *
     * <p>
     * By convention, the <code>contextObject</code> will be of the type specified by the <code>key</code>. However, this is
     * not required and the key is used strictly as a unique identifier.
     * </p>
     *
     * @param key           The key used by an @{link ELResolver} to identify this context object.
     * @param contextObject The context object to add to the collection.
     * @throws NullPointerException if key is null or contextObject is null.
     */
    public void putContext(Class<?> key, Object contextObject) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(contextObject);
        contexts.put(key, contextObject);
    }

    public void setPropertyResolved(Object base, Object property) {
        setPropertyResolved(true);
        notifyPropertyResolved(base, property);
    }

    /**
     * Retrieves the <code>ELResolver</code> associated with this context.
     *
     * <p>
     * The <code>ELContext</code> maintains a reference to the <code>ELResolver</code> that will be consulted to resolve
     * variables and properties during an expression evaluation. This method retrieves the reference to the resolver.
     * </p>
     *
     * <p>
     * Once an <code>ELContext</code> is constructed, the reference to the <code>ELResolver</code> associated with the
     * context cannot be changed.
     * </p>
     *
     * @return The resolver to be consulted for variable and property resolution during expression evaluation.
     */
    public abstract ELResolver getELResolver();

    /**
     * Retrieves the <code>FunctionMapper</code> associated with this <code>ELContext</code>.
     *
     * @return The function mapper to be consulted for the resolution of Jakarta Expression Language functions.
     */
    public abstract FunctionMapper getFunctionMapper();

    /**
     * Retrieves the <code>VariableMapper</code> associated with this <code>ELContext</code>.
     *
     * @return The variable mapper to be consulted for the resolution of Jakarta Expression Language variables.
     */
    public abstract VariableMapper getVariableMapper();
}
