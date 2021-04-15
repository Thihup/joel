package jakarta.el;

import java.util.EventListener;

/**
 * The listener interface for receiving notification when an {@link ELContext} is created.
 *
 * @see ELContext
 * @see ELContextEvent
 * @since Jakarta Server Pages 2.1
 */
public interface ELContextListener extends EventListener {

    /**
     * Invoked when a new <code>ELContext</code> has been created.
     *
     * @param elContextEvent the notification event.
     */
    void contextCreated(ELContextEvent elContextEvent);

}
