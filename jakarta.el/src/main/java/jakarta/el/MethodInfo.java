package jakarta.el;

public class MethodInfo {
    private String name;
    private Class<?> returnType;
    private Class<?>[] parameterTypes;

    public MethodInfo(String name, Class<?> returnType, Class<?>[] parameterTypes) {
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getParamTypes() {
        return parameterTypes;
    }
}
