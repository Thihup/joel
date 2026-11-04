package dev.thihup.joel.impl;

import dev.thihup.joel.impl.node.CallExpressionNode;
import dev.thihup.joel.impl.node.DeferredExpressionNode;
import dev.thihup.joel.impl.node.DynamicExpressionNode;
import dev.thihup.joel.impl.node.IdentifierNode;
import dev.thihup.joel.impl.node.InfixExpressionNode;
import dev.thihup.joel.impl.node.LambdaNode;
import dev.thihup.joel.impl.node.ListNode;
import dev.thihup.joel.impl.node.MapNode;
import dev.thihup.joel.impl.node.MemberNode;
import dev.thihup.joel.impl.node.Node;
import dev.thihup.joel.impl.node.RelationalNode;
import dev.thihup.joel.impl.node.SemicolonNode;
import dev.thihup.joel.impl.node.SetNode;
import dev.thihup.joel.impl.node.TernaryNode;
import dev.thihup.joel.impl.node.UnaryEmptyNode;
import dev.thihup.joel.impl.node.UnaryMinusNode;
import dev.thihup.joel.impl.node.UnaryNotNode;
import jakarta.el.FunctionMapper;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class FunctionBinder {

    private FunctionBinder() {
    }

    public static FunctionMapper bind(Node node, FunctionMapper original) {
        if (node == null || original == null) {
            return original;
        }
        Map<String, Method> bound = new HashMap<>();
        collectFunctions(node, original, bound);
        if (bound.isEmpty()) {
            return original;
        }
        Map<String, Method> immutableBound = Map.copyOf(bound);
        return new FunctionMapper() {
            @Override
            public Method resolveFunction(String prefix, String localName) {
                String key = (prefix == null || prefix.isEmpty()) ? localName : prefix + ":" + localName;
                Method method = immutableBound.get(key);
                if (method != null) {
                    return method;
                }
                return original.resolveFunction(prefix, localName);
            }
        };
    }

    private static void collectFunctions(Node node, FunctionMapper mapper, Map<String, Method> result) {
        if (node instanceof CallExpressionNode call) {
            if (call.callee() instanceof IdentifierNode id) {
                String funcName = id.value();
                String[] split = funcName.split(":", 2);
                String prefix = split.length == 1 ? "" : split[0];
                String localName = split.length == 1 ? split[0] : split[1];
                Method method = mapper.resolveFunction(prefix, localName);
                if (method != null) {
                    try {
                        method.trySetAccessible();
                    } catch (Exception ignored) {
                    }
                    result.put(funcName, method);
                }
            } else {
                collectFunctions(call.callee(), mapper, result);
            }
            if (call.arguments() != null) {
                for (Node arg : call.arguments()) {
                    collectFunctions(arg, mapper, result);
                }
            }
        } else if (node instanceof InfixExpressionNode infix) {
            collectFunctions(infix.left(), mapper, result);
            collectFunctions(infix.right(), mapper, result);
        } else if (node instanceof RelationalNode rel) {
            collectFunctions(rel.left(), mapper, result);
            collectFunctions(rel.right(), mapper, result);
        } else if (node instanceof MemberNode member) {
            collectFunctions(member.object(), mapper, result);
            collectFunctions(member.property(), mapper, result);
        } else if (node instanceof TernaryNode ternary) {
            collectFunctions(ternary.condition(), mapper, result);
            collectFunctions(ternary.trueExpression(), mapper, result);
            collectFunctions(ternary.falseExpression(), mapper, result);
        } else if (node instanceof LambdaNode lambda) {
            collectFunctions(lambda.expression(), mapper, result);
        } else if (node instanceof ListNode list) {
            if (list.values() != null) {
                for (Node n : list.values()) {
                    collectFunctions(n, mapper, result);
                }
            }
        } else if (node instanceof SetNode set) {
            if (set.values() != null) {
                for (Node n : set.values()) {
                    collectFunctions(n, mapper, result);
                }
            }
        } else if (node instanceof MapNode map) {
            if (map.entries() != null) {
                for (var entry : map.entries()) {
                    if (entry.getKey() != null) collectFunctions(entry.getKey(), mapper, result);
                    if (entry.getValue() != null) collectFunctions(entry.getValue(), mapper, result);
                }
            }
        } else if (node instanceof SemicolonNode semi) {
            collectFunctions(semi.left(), mapper, result);
            collectFunctions(semi.right(), mapper, result);
        } else if (node instanceof UnaryEmptyNode empty) {
            collectFunctions(empty.node(), mapper, result);
        } else if (node instanceof UnaryMinusNode minus) {
            collectFunctions(minus.node(), mapper, result);
        } else if (node instanceof UnaryNotNode not) {
            collectFunctions(not.node(), mapper, result);
        } else if (node instanceof DeferredExpressionNode def) {
            collectFunctions(def.node(), mapper, result);
        } else if (node instanceof DynamicExpressionNode dyn) {
            collectFunctions(dyn.node(), mapper, result);
        }
    }
}

