package jakarta.el;

/**
 * Resolves a bean by its known name. This class can be extended to return a bean object given its name, to set a value
 * to an existing bean, or to create a bean with the value.
 *
 * @see BeanNameELResolver
 * @since Jakarta Expression Language 3.0
 */
public abstract class BeanNameResolver {

    public BeanNameResolver() {
    }

    public boolean canCreateBean(String beanName) {
        return false;
    }

    /**
     * Returns the bean known by its name.
     *
     * @param beanName The name of the bean.
     * @return The bean with the given name. Can be <code>null</code>.
     */
    public Object getBean(String beanName) {
        return null;
    }

    /**
     * Returns whether the given name is resolved by the BeanNameResolver
     *
     * @param beanName The name of the bean.
     * @return true if the name is resolved by this BeanNameResolver; false otherwise.
     */
    public boolean isNameResolved(String beanName) {
        return false;
    }

    /**
     * Indicates if the bean of the given name is read-only or writable
     *
     * @param beanName The name of the bean
     * @return <code>true</code> if the bean can be set to a new value. <code>false</code> otherwise.
     */
    public boolean isReadOnly(String beanName) {
        return true;
    }

    /**
     * Sets a value to a bean of the given name. If the bean of the given name does not exist and if {@link #canCreateBean}
     * is <code>true</code>, one is created with the given value.
     *
     * @param beanName The name of the bean
     * @param value    The value to set the bean to. Can be <code>null</code>.
     * @throws PropertyNotWritableException if the bean cannot be modified or created.
     */
    public void setBeanValue(String beanName, Object value) {
        throw new PropertyNotWritableException();
    }
}
