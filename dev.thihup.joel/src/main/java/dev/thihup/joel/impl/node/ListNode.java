package dev.thihup.joel.impl.node;

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
        int size = values.size();
        ArrayList<Object> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(values.get(i).getValue(context));
        }
        return result;
    }

    @Override
    public String prettyPrint() {
        return values.stream().map(Node::prettyPrint).collect(Collectors.joining(",", "[", "]"));
    }
}
