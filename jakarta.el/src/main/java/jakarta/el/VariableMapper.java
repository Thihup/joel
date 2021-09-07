package jakarta.el;

/**
 * The interface to a map between Jakarta Expression Language variables and the Jakarta Expression Language expressions
 * they are associated with.
 *
 * @since Jakarta Server Pages 2.1
 */
public abstract class VariableMapper {
    /**
     * @param variable The variable name
     * @return the ValueExpression assigned to the variable, null if there is no previous assignment to this variable.
     */
    public abstract ValueExpression resolveVariable(String variable);

    /**
     * Assign a ValueExpression to an Jakarta Expression Language variable, replacing any previously assignment to the same
     * variable. The assignment for the variable is removed if the expression is <code>null</code>.
     *
     * @param variable The variable name
     * @param value    The ValueExpression to be assigned to the variable.
     * @return The previous ValueExpression assigned to this variable, null if there is no previous assignment to this
     * variable.
     */
    public abstract ValueExpression setVariable(String variable, ValueExpression value);
}
