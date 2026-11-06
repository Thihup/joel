package dev.thihup.joel.tests;

import dev.thihup.joel.impl.compiler.AstBytecodeCompiler;
import dev.thihup.joel.impl.compiler.AstMethodHandleCompiler;
import dev.thihup.joel.impl.compiler.CompiledExpression;
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AstBytecodeCompilerTest {

    private ExpressionFactory factory;
    private ELContext context;

    public record Person(String name, int age, boolean active, String email, List<String> tags) {}

    @BeforeEach
    void setUp() {
        factory = ExpressionFactory.newInstance();
        context = new StandardELContext(factory);
    }

    @Test
    void testIsHiddenClass() throws Throwable {
        CompiledExpression expr = AstBytecodeCompiler.compile("${1 + 1}");
        assertNotNull(expr);
        assertTrue(expr.getClass().isHidden(), "Bytecode compiled expression must be a Java Hidden Class");
        assertEquals(2L, expr.evaluate(context));
    }

    @Test
    void testArithmeticExpression() throws Throwable {
        CompiledExpression expr = AstBytecodeCompiler.compile("${1 + 2 * 3 - 4 / 2}");
        Object result = expr.evaluate(context);
        assertEquals(5.0, ((Number) result).doubleValue());
    }

    @Test
    void testMemberAccess() throws Throwable {
        Person person = new Person("Daniel", 32, true, "daniel@example.com", Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        CompiledExpression nameExpr = AstBytecodeCompiler.compile("${person.name}");
        CompiledExpression ageExpr = AstBytecodeCompiler.compile("${person.age}");

        assertEquals("Daniel", nameExpr.evaluate(context));
        assertEquals(32, ageExpr.evaluate(context));
    }

    @Test
    void testComplexExpression() throws Throwable {
        Person person = new Person("Daniel", 32, true, null, Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        CompiledExpression expr = AstBytecodeCompiler.compile("${person.age >= 18 && person.active ? person.name : 'Unknown'}");
        assertEquals("Daniel", expr.evaluate(context));

        Person inactivePerson = new Person("Carlos", 17, false, null, Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(inactivePerson, Person.class));

        assertEquals("Unknown", expr.evaluate(context));
    }

    @Test
    void testConditionalsAndElvis() throws Throwable {
        CompiledExpression elvis1 = AstBytecodeCompiler.compile("${false ?: 'default'}");
        assertEquals("default", elvis1.evaluate(context));

        CompiledExpression elvis2 = AstBytecodeCompiler.compile("${true ?: 'default'}");
        assertEquals(Boolean.TRUE, elvis2.evaluate(context));

        CompiledExpression nc1 = AstBytecodeCompiler.compile("${null ?? 'default'}");
        assertEquals("default", nc1.evaluate(context));

        CompiledExpression nc2 = AstBytecodeCompiler.compile("${'custom' ?? 'default'}");
        assertEquals("custom", nc2.evaluate(context));
    }

    @Test
    void testExecutionSpeedComparison() throws Throwable {
        Person person = new Person("Daniel", 32, true, null, Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        String exprStr = "${person.age >= 18 && person.active ? person.name : 'Unknown'}";

        // AST interpretada
        var astExpr = factory.createValueExpression(context, exprStr, Object.class);

        // Hidden Class MethodHandle
        CompiledExpression mhExpr = AstMethodHandleCompiler.compile(exprStr);

        // Hidden Class Bytecode direto
        CompiledExpression bytecodeExpr = AstBytecodeCompiler.compile(exprStr);

        // Warmup JIT
        for (int i = 0; i < 50_000; i++) {
            astExpr.getValue(context);
            mhExpr.evaluate(context);
            bytecodeExpr.evaluate(context);
        }

        int iterations = 1_000_000;

        // Mede AST interpretada
        long startAst = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            astExpr.getValue(context);
        }
        long durationAst = System.nanoTime() - startAst;

        // Mede MethodHandle
        long startMh = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            mhExpr.evaluate(context);
        }
        long durationMh = System.nanoTime() - startMh;

        // Mede Bytecode direto
        long startBytecode = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            bytecodeExpr.evaluate(context);
        }
        long durationBytecode = System.nanoTime() - startBytecode;

        double msAst = durationAst / 1_000_000.0;
        double msMh = durationMh / 1_000_000.0;
        double msBytecode = durationBytecode / 1_000_000.0;

        System.out.printf("%n=== 3-WAY SPEED COMPARISON (%d iterations) ===%n", iterations);
        System.out.printf("AST Interpreted:      %.2f ms%n", msAst);
        System.out.printf("MethodHandle Chain:   %.2f ms (%.2fx vs AST)%n", msMh, (double) durationAst / durationMh);
        System.out.printf("Bytecode Direct (CF): %.2f ms (%.2fx vs AST, %.2fx vs MH)%n%n", msBytecode, (double) durationAst / durationBytecode, (double) durationMh / durationBytecode);
    }
}

