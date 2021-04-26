package jakarta.el;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Encapsulates a parameterized {@link ValueExpression}.
 *
 * <p>
 * A <code>LambdaExpression</code> is a representation of the Jakarta Expression Language Lambda expression syntax. It
 * consists of a list of the formal parameters and a body, represented by a {@link ValueExpression}. The body can be any
 * valid <code>Expression</code>, including another <code>LambdaExpression</code>.
 *
 * <p>
 * A <code>LambdaExpression</code> is created when an Jakarta Expression Language expression containing a Lambda
 * expression is evaluated.
 *
 * <p>
 * A <code>LambdaExpression</code> can be invoked by calling {@link LambdaExpression#invoke}, with an
 * {@link ELContext} and a list of the actual arguments. Alternately, a <code>LambdaExpression</code> can be
 * invoked without passing a <code>ELContext</code>, in which case the <code>ELContext</code> previously set by calling
 * {@link LambdaExpression#setELContext} will be used. The evaluation of the <code>ValueExpression</code> in the body
 * uses the {@link ELContext} to resolve references to the parameters, and to evaluate the lambda expression. The result
 * of the evaluation is returned.
 *
 * @see ELContext#getLambdaArgument
 * @see ELContext#enterLambdaScope
 * @see ELContext#exitLambdaScope
 */
public class LambdaExpression {

    private final List<String> formalParameters;
    private final ValueExpression expression;
    private ELContext context;

    /**
     * Creates a new LambdaExpression.
     *
     * @param formalParameters The list of String representing the formal parameters.
     * @param expression       The <code>ValueExpression</code> representing the body.
     */
    public LambdaExpression(List<String> formalParameters, ValueExpression expression) {
        this.formalParameters = formalParameters;
        this.expression = expression;
    }

    /**
     * Invoke the encapsulated Lambda expression.
     * <p>
     * The supplied arguments are matched, in the same order, to the formal parameters. If there are more arguments than the
     * formal parameters, the extra arguments are ignored. If there are less arguments than the formal parameters, an
     * <code>ELException</code> is thrown.
     * </p>
     *
     * <p>
     * The actual Lambda arguments are added to the ELContext and are available during the evaluation of the Lambda
     * expression. They are removed after the evaluation.
     * </p>
     *
     * @param elContext The ELContext used for the evaluation of the expression The ELContext set by {@link #setELContext}
     *                  is ignored.
     * @param arguments The arguments to invoke the Lambda expression. For calls with no arguments, an empty array must be
     *                  provided. A Lambda argument can be <code>null</code>.
     * @return The result of invoking the Lambda expression
     * @throws ELException          if not enough arguments are provided
     * @throws NullPointerException is elContext is null
     */
    public Object invoke(ELContext elContext, Object... arguments) {
        Objects.requireNonNull(elContext);

        if (arguments.length < formalParameters.size())
            throw new ELException("Not enough arguments provided");

        Map<String, Object> collect = IntStream.range(0, formalParameters.size())
                .boxed()
                .collect(HashMap::new, (map, value) -> map.put(formalParameters.get(value), arguments[value]), HashMap::putAll);

        try {
            elContext.enterLambdaScope(collect);
            return expression.getValue(elContext);
        } finally {
            elContext.exitLambdaScope();
        }
    }

    /**
     * Invoke the encapsulated Lambda expression.
     * <p>
     * The supplied arguments are matched, in the same order, to the formal parameters. If there are more arguments than the
     * formal parameters, the extra arguments are ignored. If there are less arguments than the formal parameters, an
     * <code>ELException</code> is thrown.
     * </p>
     *
     * <p>
     * The actual Lambda arguments are added to the ELContext and are available during the evaluation of the Lambda
     * expression. They are removed after the evaluation.
     * </p>
     * <p>
     * The ELContext set by {@link LambdaExpression#setELContext} is used in the evaluation of the lambda Expression.
     *
     * @param arguments The arguments to invoke the Lambda expression. For calls with no arguments, an empty array must be
     *                  provided. A Lambda argument can be <code>null</code>.
     * @return The result of invoking the Lambda expression
     * @throws ELException if not enough arguments are provided
     */
    public Object invoke(Object... arguments) {
        return invoke(context, arguments);
    }

    /**
     * Set the ELContext to use in evaluating the LambdaExpression. The ELContext must to be set prior to the invocation of
     * the LambdaExpression, unless it is supplied with {@link LambdaExpression#invoke}.
     *
     * @param context The ELContext to use in evaluating the LambdaExpression.
     */
    public void setELContext(ELContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "LambdaExpression{" + formalParameters + " -> " + expression.toString() + "}";
    }
}
