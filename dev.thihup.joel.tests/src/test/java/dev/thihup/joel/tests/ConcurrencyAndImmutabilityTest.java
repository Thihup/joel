package dev.thihup.joel.tests;

import jakarta.el.ELContext;
import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrencyAndImmutabilityTest {

    public static int add100(int x) {
        return x + 100;
    }

    public static int multiply2(int x) {
        return x * 2;
    }

    @Test
    @DisplayName("Verify that concurrent parsing of diverse expressions does not throw ConcurrentModificationException")
    void testConcurrentDiverseExpressionParsing() throws Exception {
        ExpressionFactory factory = ExpressionFactory.newInstance();
        int threads = 16;
        int operationsPerThread = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                tasks.add(() -> {
                    ELContext context = new StandardELContext(factory);
                    for (int i = 0; i < operationsPerThread; i++) {
                        int val = ThreadLocalRandom.current().nextInt(1000);
                        ValueExpression ve = factory.createValueExpression(context, "${" + val + " + 1}", Object.class);
                        Number result = (Number) ve.getValue(context);
                        assertEquals(val + 1, result.intValue());
                    }
                    return null;
                });
            }

            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    @DisplayName("Verify that shared AST nodes remain immutable and do not leak functions across distinct contexts")
    void testNoCrossContextFunctionLeakage() throws Exception {
        ExpressionFactory factory = ExpressionFactory.newInstance();

        ELProcessor procA = new ELProcessor();
        procA.defineFunction("fn", "calc", ConcurrencyAndImmutabilityTest.class.getMethod("add100", int.class));
        ELContext contextA = procA.getELManager().getELContext();

        ELProcessor procB = new ELProcessor();
        procB.defineFunction("fn", "calc", ConcurrencyAndImmutabilityTest.class.getMethod("multiply2", int.class));
        ELContext contextB = procB.getELManager().getELContext();

        // Both expressions share the exact same expression string with distinct function mappings in their contexts
        String expression = "${fn:calc(10)}";
        ValueExpression veA = factory.createValueExpression(contextA, expression, Object.class);
        ValueExpression veB = factory.createValueExpression(contextB, expression, Object.class);

        // contextA must evaluate to 10 + 100 = 110
        assertEquals(110, ((Number) veA.getValue(contextA)).intValue());

        // contextB must evaluate to 10 * 2 = 20 (must NOT leak contextA's method via the shared AST node)
        assertEquals(20, ((Number) veB.getValue(contextB)).intValue());

        // Successive re-evaluations must maintain the creation-time function bindings
        assertEquals(110, ((Number) veA.getValue(contextA)).intValue());
        assertEquals(20, ((Number) veB.getValue(contextB)).intValue());
    }

    @Test
    @DisplayName("Verify function isolation under multi-threaded concurrency for the same expression string")
    void testConcurrentCrossContextFunctionResolution() throws Exception {
        ExpressionFactory factory = ExpressionFactory.newInstance();
        Method methodAdd = ConcurrencyAndImmutabilityTest.class.getMethod("add100", int.class);
        Method methodMul = ConcurrencyAndImmutabilityTest.class.getMethod("multiply2", int.class);

        int threads = 16;
        int iterations = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final boolean useAdd = (t % 2 == 0);
                tasks.add(() -> {
                    ELProcessor proc = new ELProcessor();
                    proc.defineFunction("fn", "calc", useAdd ? methodAdd : methodMul);
                    ELContext ctx = proc.getELManager().getELContext();

                    for (int i = 0; i < iterations; i++) {
                        ValueExpression ve = factory.createValueExpression(ctx, "${fn:calc(10)}", Object.class);
                        Number val = (Number) ve.getValue(ctx);
                        int expected = useAdd ? 110 : 20;
                        assertEquals(expected, val.intValue());
                    }
                    return null;
                });
            }

            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
