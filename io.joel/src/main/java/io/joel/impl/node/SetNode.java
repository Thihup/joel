package io.joel.impl.node;

import jakarta.el.ELContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record SetNode(List<ExpressionNode> values) implements ExpressionNode {
    @Override
    public Class<?> getType(ELContext context) {
        return Set.class;
    }

    @Override
    public Object getValue(ELContext context) {
        return values.stream()
                .map(x -> x.getValue(context))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public String prettyPrint() {
        return values.stream().map(ExpressionNode::prettyPrint).collect(Collectors.joining(",", "{", "}"));
    }
}
