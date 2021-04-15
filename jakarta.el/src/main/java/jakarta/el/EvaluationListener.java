package jakarta.el;

/**
 * The listener interface for receiving notification when a Jakarta Expression Language expression is evaluated.
 *
 * @since Jakarta Expression Language 3.0
 */
public abstract class EvaluationListener {

    /**
     * Receives notification after a Jakarta Expression Language expression is evaluated
     *
     * @param context    The ELContext
     * @param expression The Jakarta Expression Language expression string to be evaluated
     */
    public void afterEvaluation(ELContext context, String expression) {
    }

    /**
     * Receives notification before a Jakarta Expression Language expression is evaluated
     *
     * @param context    The ELContext
     * @param expression The Jakarta Expression Language expression string to be evaluated
     */
    public void beforeEvaluation(ELContext context, String expression) {
    }

    /**
     * Receives notification when the (base, property) pair is resolved
     *
     * @param context  The ELContext
     * @param base     The base object
     * @param property The property object
     */
    public void propertyResolved(ELContext context, Object base, Object property) {
    }
}
