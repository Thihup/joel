package io.joel.impl.node;

import jakarta.el.ELContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record ListNode(List<ExpressionNode> values) implements ExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        return List.class;
    }

    @Override
    public Object getValue(ELContext context) {
        return values.stream()
                .map(x -> x.getValue(context))
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
