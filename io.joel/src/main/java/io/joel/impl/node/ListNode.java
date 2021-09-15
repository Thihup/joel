package io.joel.impl.node;

import jakarta.el.ELContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record ListNode(List<Node> values) implements Node {
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

    @Override
    public String prettyPrint() {
        return values.stream().map(Node::prettyPrint).collect(Collectors.joining(",", "[", "]"));
    }
}
