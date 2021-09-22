package jakarta.el;

import java.io.Serial;

/**
 * Represents any of the exception conditions that can arise during expression evaluation.
 *
 * @since Jakarta Server Pages 2.1
 */
public class ELException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4129157481800354613L;

    /**
     * Creates an <code>ELException</code> with no detail message.
     */
    public ELException() {
    }

    /**
     * Creates an <code>ELException</code> with the provided detail message.
     *
     * @param message the detail message
     */
    public ELException(String message) {
        super(message);
    }

    /**
     * Creates an ELException with the given detail message and root cause.
     *
     * @param message the detail message
     * @param cause   the originating cause of this exception
     */

    public ELException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an <code>ELException</code> with the given cause.
     *
     * @param cause the originating cause of this exception
     */
    public ELException(Throwable cause) {
        super(cause);
    }

}
