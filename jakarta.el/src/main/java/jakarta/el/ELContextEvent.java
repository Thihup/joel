package jakarta.el;

import java.util.EventObject;

public class ELContextEvent extends EventObject {
    public ELContextEvent(ELContext source) {
        super(source);
    }

    public ELContext getELContext() {
        return ((ELContext) getSource());
    }

}
