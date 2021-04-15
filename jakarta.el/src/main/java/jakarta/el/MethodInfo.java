package jakarta.el;

/**
 * Holds information about a method that a {@link MethodExpression} evaluated to.
 *
 * @since Jakarta Server Pages 2.1
 */
public class MethodInfo {
    private final String name;
    private final Class<?> returnType;
    private final Class<?>[] parameterTypes;

    /**
     * Creates a new instance of <code>MethodInfo</code> with the given information.
     *
     * @param name           The name of the method
     * @param returnType     The return type of the method
     * @param parameterTypes The types of each of the method's parameters
     */
    public MethodInfo(String name, Class<?> returnType, Class<?>[] parameterTypes) {
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    /**
     * Returns the name of the method
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the return type of the method
     *
     * @return the return type of the method
     */
    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Returns the parameter types of the method
     *
     * @return the parameter types of the method
     */
    public Class<?>[] getParamTypes() {
        return parameterTypes;
    }
}
