package io.joel.impl.mapper;

import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Only used to pass the TCK, as the TCK requires a public VariableMapper
 */
public final class JoelVariableMapper extends VariableMapper {
    private final Map<String, ValueExpression> mapper = new HashMap<>();

    @Override
    public ValueExpression resolveVariable(String variable) {
        return mapper.get(variable);
    }

    @Override
    public ValueExpression setVariable(String variable, ValueExpression value) {
        return mapper.put(variable, value);
    }
}
