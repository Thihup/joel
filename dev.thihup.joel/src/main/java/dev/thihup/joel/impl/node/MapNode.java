package dev.thihup.joel.impl.node;

import jakarta.el.ELContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public value record MapNode(List<Map.Entry<Node, Node>> entries) implements Node {
    @Override
    public Object getValue(ELContext context) {
        Map<Object, Object> result = new LinkedHashMap<>();
        for (var entry : entries) {
            result.put(entry.getKey().getValue(context), entry.getValue().getValue(context));
        }
        return result;
    }

    @Override
    public Class<?> getType(ELContext context) {
        return Map.class;
    }
}
