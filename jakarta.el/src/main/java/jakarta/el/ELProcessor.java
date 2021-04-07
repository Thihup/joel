package jakarta.el;

import java.lang.reflect.Method;

public class ELProcessor {

    public ELProcessor() {
    }

    public void defineBean(String name, Object bean) {
    }

    public void defineFunction(String prefix, String function, Method method) {
    }

    public void defineFunction(String prefix, String function, String className, String method) {
    }

    public Object eval(String expression) {
        return null;
    }

    public ELManager getELManager() {
        return null;
    }

    public Object getValue(String expression, Class<?> expectedType) {
        return null;
    }

    public void setValue(String expression, Object value) {
    }

    public void setVariable(String variable, String expression) {
    }
}
