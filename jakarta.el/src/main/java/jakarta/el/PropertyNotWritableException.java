package jakarta.el;

public class PropertyNotWritableException extends ELException {
    public PropertyNotWritableException() {
    }

    public PropertyNotWritableException(String message) {
        super(message);
    }

    public PropertyNotWritableException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyNotWritableException(Throwable cause) {
        super(cause);
    }
}
