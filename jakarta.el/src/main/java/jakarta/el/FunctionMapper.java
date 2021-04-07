package jakarta.el;

import java.lang.reflect.Method;

public abstract class FunctionMapper {
    public void mapFunction(String prefix, String function, Method method) {
    }

    public abstract Method resolveFunction(String prefix, String localName);
}
