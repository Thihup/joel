package jakarta.el;

import java.util.EventListener;

public interface ELContextListener extends EventListener {

    void contextCreated(ELContextEvent elContextEvent);

}
