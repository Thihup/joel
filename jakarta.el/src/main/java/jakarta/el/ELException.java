package jakarta.el;

public class ELException extends RuntimeException {

    public ELException() {
    }

    public ELException(String message) {
        super(message);
    }

    public ELException(String message, Throwable cause) {
        super(message, cause);
    }

    public ELException(Throwable cause) {
        super(cause);
    }

}
