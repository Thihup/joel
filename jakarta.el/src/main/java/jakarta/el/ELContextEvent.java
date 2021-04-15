package jakarta.el;

import java.util.EventObject;

/**
 * An event which indicates that an {@link ELContext} has been created. The source object is the ELContext that was
 * created.
 *
 * @see ELContext
 * @see ELContextListener
 * @since Jakarta Server Pages 2.1
 */
public class ELContextEvent extends EventObject {
    /**
     * Constructs an ELContextEvent object to indicate that an <code>ELContext</code> has been created.
     *
     * @param source the <code>ELContext</code> that was created.
     */
    public ELContextEvent(ELContext source) {
        super(source);
    }

    /**
     * Returns the <code>ELContext</code> that was created. This is a type-safe equivalent of the {@link #getSource} method.
     *
     * @return the ELContext that was created.
     */
    public ELContext getELContext() {
        return (ELContext) getSource();
    }

}
