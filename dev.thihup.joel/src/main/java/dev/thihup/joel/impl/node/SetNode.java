package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public value record SetNode(List<Node> values) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        return Set.class;
    }

    @Override
    public Object getValue(ELContext context) {
        int size = values.size();
        HashSet<Object> result = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            result.add(values.get(i).getValue(context));
        }
        return result;
    }

    @Override
    public String prettyPrint() {
        return values.stream().map(Node::prettyPrint).collect(Collectors.joining(",", "{", "}"));
    }
}
