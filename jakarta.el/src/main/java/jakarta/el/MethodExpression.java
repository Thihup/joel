package jakarta.el;

public abstract class MethodExpression extends Expression {
    public abstract MethodInfo getMethodInfo(ELContext context);

    public abstract Object invoke(ELContext context, Object[] params);

    public boolean isParametersProvided() {
        return false;
    }

    @Deprecated
    public boolean isParmetersProvided() {
        return false;
    }

}
