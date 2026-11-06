package dev.thihup.joel.tests;

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

public class AstMethodHandleCompilerTest {

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
        CompiledExpression expr = AstMethodHandleCompiler.compile("${1 + 1}");
        assertNotNull(expr);
        assertTrue(expr.getClass().isHidden(), "Compiled expression must be an instance of a Java Hidden Class");
        assertEquals(2L, expr.evaluate(context));
    }

    @Test
    void testArithmeticExpression() throws Throwable {
        CompiledExpression expr = AstMethodHandleCompiler.compile("${1 + 2 * 3 - 4 / 2}");
        Object result = expr.evaluate(context);
        assertEquals(5.0, ((Number) result).doubleValue());
    }

    @Test
    void testMemberAccess() throws Throwable {
        Person person = new Person("Daniel", 32, true, "daniel@example.com", Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        CompiledExpression nameExpr = AstMethodHandleCompiler.compile("${person.name}");
        CompiledExpression ageExpr = AstMethodHandleCompiler.compile("${person.age}");

        assertEquals("Daniel", nameExpr.evaluate(context));
        assertEquals(32, ageExpr.evaluate(context));
    }

    @Test
    void testComplexExpression() throws Throwable {
        Person person = new Person("Daniel", 32, true, null, Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        CompiledExpression expr = AstMethodHandleCompiler.compile(
                "${person.age >= 18 && person.active ? person.name : 'Unknown'}"
        );

        assertEquals("Daniel", expr.evaluate(context));

        Person inactive = new Person("Daniel", 32, false, null, Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(inactive, Person.class));
        assertEquals("Unknown", expr.evaluate(context));

        Person minor = new Person("Alice", 15, true, null, Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(minor, Person.class));
        assertEquals("Unknown", expr.evaluate(context));
    }

    @Test
    void testRelationalAndLogical() throws Throwable {
        CompiledExpression trueExpr = AstMethodHandleCompiler.compile("${10 > 5 && 'abc' == 'abc'}");
        assertEquals(Boolean.TRUE, trueExpr.evaluate(context));

        CompiledExpression falseExpr = AstMethodHandleCompiler.compile("${5 > 10 || 2 == 3}");
        assertEquals(Boolean.FALSE, falseExpr.evaluate(context));
    }

    @Test
    void testElvisAndNullCoalescing() throws Throwable {
        CompiledExpression elvis1 = AstMethodHandleCompiler.compile("${false ?: 'default'}");
        assertEquals("default", elvis1.evaluate(context));

        CompiledExpression elvis2 = AstMethodHandleCompiler.compile("${true ?: 'default'}");
        assertEquals(Boolean.TRUE, elvis2.evaluate(context));

        CompiledExpression nc1 = AstMethodHandleCompiler.compile("${null ?? 'default'}");
        assertEquals("default", nc1.evaluate(context));

        CompiledExpression nc2 = AstMethodHandleCompiler.compile("${'custom' ?? 'default'}");
        assertEquals("custom", nc2.evaluate(context));
    }

    @Test
    void testExecutionSpeedComparison() throws Throwable {
        Person person = new Person("Daniel", 32, true, null, Collections.emptyList());
        context.getVariableMapper().setVariable("person", factory.createValueExpression(person, Person.class));

        String exprStr = "${person.age >= 18 && person.active ? person.name : 'Unknown'}";

        // AST interpretada (ValueExpression normal)
        var astExpr = factory.createValueExpression(context, exprStr, Object.class);

        // Hidden Class compilada
        CompiledExpression compiledExpr = AstMethodHandleCompiler.compile(exprStr);

        // Warmup JIT
        for (int i = 0; i < 50_000; i++) {
            astExpr.getValue(context);
            compiledExpr.evaluate(context);
        }

        int iterations = 1_000_000;

        // Mede AST interpretada
        long startAst = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            astExpr.getValue(context);
        }
        long durationAst = System.nanoTime() - startAst;

        // Mede Hidden Class compilada
        long startCompiled = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            compiledExpr.evaluate(context);
        }
        long durationCompiled = System.nanoTime() - startCompiled;

        double msAst = durationAst / 1_000_000.0;
        double msCompiled = durationCompiled / 1_000_000.0;
        double speedup = (double) durationAst / durationCompiled;

        System.out.printf("%n=== SPEED COMPARISON (%d iterations) ===%n", iterations);
        System.out.printf("AST Interpreted:  %.2f ms%n", msAst);
        System.out.printf("Hidden Class MH:  %.2f ms%n", msCompiled);
        System.out.printf("Speedup:          %.2fx faster%n%n", speedup);
    }
}
