package jakarta.el;

import java.lang.reflect.Method;

/**
 * Manages Jakarta Expression Language parsing and evaluation environment. The ELManager maintains an instance of
 * ExpressionFactory and StandardELContext, for parsing and evaluating Jakarta Expression Language expressions.
 *
 * @since Jakarta Expression Language 3.0
 */
public class ELManager {

    private static final ExpressionFactory FACTORY = ExpressionFactory.newInstance();
    private StandardELContext context = new StandardELContext(FACTORY);

    public ELManager() {
    }

    /**
     * Return the ExpressionFactory instance used for Jakarta Expression Language evaluations.
     *
     * @return The ExpressionFactory
     */
    public static ExpressionFactory getExpressionFactory() {
        return FACTORY;
    }

    /**
     * Register a BeanNameResolver. Construct a BeanNameELResolver with the BeanNameResolver and add it to the list of
     * ELResolvers. Once registered, the BeanNameResolver cannot be removed.
     *
     * @param beanNameResolver The BeanNameResolver to be registered.
     */
    public void addBeanNameResolver(BeanNameResolver beanNameResolver) {
        addELResolver(new BeanNameELResolver(beanNameResolver));
    }

    /**
     * Add an user defined ELResolver to the list of ELResolvers. Can be called multiple times. The new ELResolver is placed
     * ahead of the default ELResolvers. The list of the ELResolvers added this way are ordered chronologically.
     *
     * @param elResolver The ELResolver to be added to the list of ELResolvers in ELContext.
     * @see StandardELContext#addELResolver
     */
    public void addELResolver(ELResolver elResolver) {
        context.addELResolver(elResolver);
    }

    /**
     * Register an evaluation listener.
     *
     * @param evaluationListener The evaluation listener to be added.
     */
    public void addEvaluationListener(EvaluationListener evaluationListener) {
        context.addEvaluationListener(evaluationListener);
    }

    /**
     * Define a bean in the local bean repository
     *
     * @param name The name of the bean
     * @param bean The bean instance to be defined. If null, the definition of the bean is removed.
     * @return the previous bean (if any) mapped to <code>name</code>
     */
    public Object defineBean(String name, Object bean) {
        Object oldValue = context.localBeanRepository.getBean(name);
        context.localBeanRepository.setBeanValue(name, bean);
        return oldValue;
    }

    /**
     * Return the ELContext used for parsing and evaluating Jakarta Expression Language expressions. If there is currently
     * no ELContext, a default instance of StandardELContext is returned.
     *
     * @return The ELContext used for parsing and evaluating Jakarta Expression Language expressions..
     */
    public StandardELContext getELContext() {
        return context;
    }

    /**
     * Import a class. The imported class must be loadable from the classloader at the expression evaluation time.
     *
     * @param className The full class name of the class to be imported
     * @throws ELException if the name is not a full class name.
     */
    public void importClass(String className) {
        context.getImportHandler().importClass(className);
    }

    /**
     * Import a package. At the expression evaluation time, the imported package name will be used to construct the full
     * class name, which will then be used to load the class. Inherently, this is less efficient than importing a class.
     *
     * @param packageName The package name to be imported
     */
    public void importPackage(String packageName) {
        context.getImportHandler().importPackage(packageName);
    }

    /**
     * Import a static field or method. The class of the static member must be loadable from the classloader, at class
     * resolution time.
     *
     * @param staticMemberName The full class name of the class to be imported
     * @throws ELException if the name is not a full class name.
     */
    public void importStatic(String staticMemberName) {
        context.getImportHandler().importStatic(staticMemberName);
    }

    /**
     * Maps a static method to Jakarta Expression Language function.
     *
     * @param prefix   The namespace of the functions, can be "".
     * @param function The name of the function.
     * @param method   The static method to be invoked when the function is used.
     */
    public void mapFunction(String prefix, String function, Method method) {
        context.getFunctionMapper().mapFunction(prefix, function, method);
    }

    /**
     * Set the ELContext used for parsing and evaluating Jakarta Expression Language expressions. The supplied ELContext
     * will not be modified, except for the context object map.
     *
     * @param context The new ELContext.
     * @return The previous ELContext, null if none.
     */
    public ELContext setELContext(ELContext context) {
        this.context = new StandardELContext(context);
        return null;
    }

    /**
     * Assign a ValueExpression to a Jakarta Expression Language variable, replacing any previous assignment to the same
     * variable. The assignment for the variable is removed if the expression is <code>null</code>.
     *
     * @param variable   The variable name
     * @param expression The ValueExpression to be assigned to the variable.
     */
    public void setVariable(String variable, ValueExpression expression) {
        context.getVariableMapper().setVariable(variable, expression);
    }

}
