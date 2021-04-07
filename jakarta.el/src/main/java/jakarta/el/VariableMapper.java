package jakarta.el;

public abstract class VariableMapper {
    public abstract ValueExpression resolveVariable(String variable);
    public abstract ValueExpression setVariable(String variable, ValueExpression value);
}
