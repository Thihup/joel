package jakarta.el;

/**
 * A runtime representation of a Class in the Jakarta Expression Language expressions. It encapsulates the
 * java.lang.Class instance.
 *
 * <p>
 * This class is used only in {@link StaticFieldELResolver} and will probably only be of interest to Jakarta Expression
 * Language implementors, and not Jakarta Expression Language users.
 *
 * @since Jakarta Expression Language 3.0
 */
public class ELClass {
    private final Class<?> klass;

    /**
     * Constructor
     *
     * @param klass The Class instance
     */
    public ELClass(Class<?> klass) {
        this.klass = klass;
    }

    /**
     * Returns the Class instance
     *
     * @return The Class instance
     */
    public Class<?> getKlass() {
        return klass;
    }
}
