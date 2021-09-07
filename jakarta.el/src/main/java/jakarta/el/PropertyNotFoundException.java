package jakarta.el;

/**
 * Thrown when a property could not be found while evaluating a {@link ValueExpression} or {@link MethodExpression}.
 *
 * <p>
 * For example, this could be triggered by an index out of bounds while setting an array value, or by an unreadable
 * property while getting the value of a JavaBeans property.
 * </p>
 *
 * @since Jakarta Server Pages 2.1
 */
public class PropertyNotFoundException extends ELException {

    /**
     * Creates a <code>PropertyNotFoundException</code> with no detail message.
     */
    public PropertyNotFoundException() {
        super();
    }

    /**
     * Creates a <code>PropertyNotFoundException</code> with the provided detail message.
     *
     * @param message the detail message
     */
    public PropertyNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a <code>PropertyNotFoundException</code> with the given detail message and root cause.
     *
     * @param message the detail message
     * @param cause   the originating cause of this exception
     */
    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a <code>PropertyNotFoundException</code> with the given root cause.
     *
     * @param cause the originating cause of this exception
     */
    public PropertyNotFoundException(Throwable cause) {
        super(cause);
    }
}
