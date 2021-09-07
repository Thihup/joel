package jakarta.el;

/**
 * Thrown when a method could not be found while evaluating a {@link MethodExpression}.
 *
 * @see MethodExpression
 * @since Jakarta Server Pages 2.1
 */
public class MethodNotFoundException extends ELException {

    /**
     * Creates a <code>MethodNotFoundException</code> with no detail message.
     */
    public MethodNotFoundException() {
        super();
    }

    /**
     * Creates a <code>MethodNotFoundException</code> with the provided detail message.
     *
     * @param message the detail message
     */
    public MethodNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a <code>MethodNotFoundException</code> with the given detail message and root cause.
     *
     * @param message the detail message
     * @param cause   the originating cause of this exception
     */
    public MethodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a <code>MethodNotFoundException</code> with the given root cause.
     *
     * @param cause the originating cause of this exception
     */
    public MethodNotFoundException(Throwable cause) {
        super(cause);
    }
}
