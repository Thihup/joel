package jakarta.el;

import java.io.Serial;
import java.io.Serializable;

/**
 * This encapsulates a base model object and one of its properties.
 *
 * @since Jakarta Expression Language 2.2
 */
public class ValueReference implements Serializable {
    @Serial
    private static final long serialVersionUID = 1665440650760183774L;
    private final Object base;
    private final Object property;

    public ValueReference(Object base, Object property) {
        this.base = base;
        this.property = property;
    }

    public Object getBase() {
        return base;
    }

    public Object getProperty() {
        return property;
    }

}
