package io.joel.impl.node;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import static io.joel.impl.node.ExpressionNode.BooleanNode;
import static io.joel.impl.node.ExpressionNode.NullNode;
import static io.joel.impl.node.ExpressionNode.NumberNode;
import static io.joel.impl.node.ExpressionNode.StringNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ExpressionNodeTest {

    private static final ELContext CONTEXT = new StandardELContext(ExpressionFactory.newInstance());

    @Test
    void testLiteral() throws Throwable {
        assertEquals(10L, (Long) new NumberNode(10L).compile(MethodHandles.empty(MethodType.methodType(Object.class, ELContext.class))).invokeExact(CONTEXT));
    }

    @Test
    void testLiteral2() throws Throwable {
        assertEquals(Boolean.TRUE, (Boolean) BooleanNode.TRUE.compile(MethodHandles.empty(MethodType.methodType(Object.class, ELContext.class))).invokeExact(CONTEXT));
        assertEquals(Boolean.FALSE, (Boolean) BooleanNode.FALSE.compile(MethodHandles.empty(MethodType.methodType(Object.class, ELContext.class))).invokeExact(CONTEXT));
    }

    @Test
    void testLiteral3() throws Throwable {
        assertEquals("Hi!", (String) new StringNode("Hi!").compile(MethodHandles.empty(MethodType.methodType(Object.class, ELContext.class))).invokeExact(CONTEXT));
    }

    @Test
    void testLiteral4() throws Throwable {
        assertNull((Object) NullNode.INSTANCE.compile(MethodHandles.empty(MethodType.methodType(Object.class, ELContext.class))).invokeExact(CONTEXT));
    }

    @Test
    void testAddition() throws Throwable {
        var five = new NumberNode(5L);
        var ten = new NumberNode(10L);
        var add = new AddExpressionNode(five, ten);
        assertEquals(15L, add.compile(MethodHandles.empty(MethodType.methodType(Object.class, ELContext.class))).invokeExact(CONTEXT));
    }

    @Test
    void testAddition2() throws Throwable {
        var five = new NumberNode(5d);
        var ten = new NumberNode(10d);
        var add = new AddExpressionNode(five, ten);
        assertEquals(15d, add.compile(MethodHandles.empty(MethodType.methodType(Object.class, ELContext.class))).invokeExact(CONTEXT));
    }

    @Test
    void testAddition3() throws Throwable {
        var five = new NumberNode(BigDecimal.valueOf(5));
        var ten = new NumberNode(10d);
        var add = new AddExpressionNode(five, ten);
        var compile = add.compile(MethodHandles.empty(MethodType.methodType(Object.class, ELContext.class)));
        assertEquals(BigDecimal.valueOf(15), compile.invokeExact(CONTEXT));
    }

}
