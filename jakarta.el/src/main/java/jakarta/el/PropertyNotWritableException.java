package jakarta.el;

import java.io.Serial;

/**
 * Thrown when a property could not be written to while setting the value on a {@link ValueExpression}.
 *
 * <p>
 * For example, this could be triggered by trying to set a map value on an unmodifiable map.
 * </p>
 *
 * @since Jakarta Server Pages 2.1
 */
public class PropertyNotWritableException extends ELException {
    @Serial
    private static final long serialVersionUID = 3062351653639276188L;

    /**
     * Creates a <code>PropertyNotWritableException</code> with no detail message.
     */
    public PropertyNotWritableException() {
        super();
    }

    /**
     * Creates a <code>PropertyNotWritableException</code> with the provided detail message.
     *
     * @param message the detail message
     */
    public PropertyNotWritableException(String message) {
        super(message);
    }

    /**
     * Creates a <code>PropertyNotWritableException</code> with the given detail message and root cause.
     *
     * @param message the detail message
     * @param cause   the originating cause of this exception
     */
    public PropertyNotWritableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a <code>PropertyNotWritableException</code> with the given root cause.
     *
     * @param cause the originating cause of this exception
     */
    public PropertyNotWritableException(Throwable cause) {
        super(cause);
    }
}
