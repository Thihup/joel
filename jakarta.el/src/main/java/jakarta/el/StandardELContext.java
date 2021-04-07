package jakarta.el;

public class StandardELContext extends ELContext {

    public StandardELContext(ELContext context) {
    }

    public StandardELContext(ExpressionFactory expressionFactory) {
    }

    public void addELResolver(ELResolver elResolver) {
    }

    @Override
    public ELResolver getELResolver() {
        return null;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return null;
    }

    @Override
    public VariableMapper getVariableMapper() {
        return null;
    }
}
