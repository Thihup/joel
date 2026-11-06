package dev.thihup.joel.tests;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValueClassTest {

    @Test
    void testNodeRecordsAreValueClasses() throws Exception {
        List<String> nodeClassNames = List.of(
                "dev.thihup.joel.impl.node.AddExpressionNode",
                "dev.thihup.joel.impl.node.AndNode",
                "dev.thihup.joel.impl.node.AssignNode",
                "dev.thihup.joel.impl.node.CallExpressionNode",
                "dev.thihup.joel.impl.node.ConcatNode",
                "dev.thihup.joel.impl.node.DeferredExpressionNode",
                "dev.thihup.joel.impl.node.DivExpressionNode",
                "dev.thihup.joel.impl.node.DynamicExpressionNode",
                "dev.thihup.joel.impl.node.ElvisNode",
                "dev.thihup.joel.impl.node.EqualNode",
                "dev.thihup.joel.impl.node.GreaterEqualNode",
                "dev.thihup.joel.impl.node.GreaterThanNode",
                "dev.thihup.joel.impl.node.IdentifierNode",
                "dev.thihup.joel.impl.node.LambdaNode",
                "dev.thihup.joel.impl.node.LessEqualNode",
                "dev.thihup.joel.impl.node.LessThanNode",
                "dev.thihup.joel.impl.node.ListNode",
                "dev.thihup.joel.impl.node.MapNode",
                "dev.thihup.joel.impl.node.MemberNode",
                "dev.thihup.joel.impl.node.ModExpressionNode",
                "dev.thihup.joel.impl.node.MulExpressionNode",
                "dev.thihup.joel.impl.node.NotEqualNode",
                "dev.thihup.joel.impl.node.NullCoalescingNode",
                "dev.thihup.joel.impl.node.NumberNode",
                "dev.thihup.joel.impl.node.ObjectNode",
                "dev.thihup.joel.impl.node.OrNode",
                "dev.thihup.joel.impl.node.SemicolonNode",
                "dev.thihup.joel.impl.node.SetNode",
                "dev.thihup.joel.impl.node.StringNode",
                "dev.thihup.joel.impl.node.SubExpressionNode",
                "dev.thihup.joel.impl.node.TernaryNode",
                "dev.thihup.joel.impl.node.UnaryEmptyNode",
                "dev.thihup.joel.impl.node.UnaryMinusNode",
                "dev.thihup.joel.impl.node.UnaryNotNode"
        );

        for (String className : nodeClassNames) {
            Class<?> clazz = Class.forName(className);
            assertTrue(clazz.isValue(), () -> clazz.getSimpleName() + " should be a value class");
        }
    }

    @Test
    void testOperationRecordsAreValueClasses() throws Exception {
        String[] opClasses = {
                "dev.thihup.joel.impl.calculator.Operation$Addition",
                "dev.thihup.joel.impl.calculator.Operation$Subtraction",
                "dev.thihup.joel.impl.calculator.Operation$Multiplication",
                "dev.thihup.joel.impl.calculator.Operation$Division",
                "dev.thihup.joel.impl.calculator.Operation$Modulo"
        };
        for (String opClass : opClasses) {
            Class<?> clazz = Class.forName(opClass);
            assertTrue(clazz.isValue(), () -> clazz.getSimpleName() + " should be a value class");
        }
    }

    @Test
    void testUtilityClassesAreValueClasses() throws Exception {
        String[] utilClasses = {
                "dev.thihup.joel.impl.FunctionBinder",
                "dev.thihup.joel.impl.FunctionResolutionCache",
                "dev.thihup.joel.impl.FunctionResolutionCache$ResolvedFunction",
                "dev.thihup.joel.impl.TypeConverter",
                "dev.thihup.joel.impl.Utils",
                "dev.thihup.joel.impl.antlr.JoelExpressionParser"
        };
        for (String utilClass : utilClasses) {
            Class<?> clazz = Class.forName(utilClass);
            assertTrue(clazz.isValue(), () -> clazz.getSimpleName() + " should be a value class");
        }
    }
}
