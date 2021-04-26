package io.joel.tests;

import jakarta.el.ELProcessor;
import jakarta.el.LambdaExpression;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LambdaTest {

    @Test
    void testSimpleLambda() {
        Object eval = new ELProcessor().eval("(a)->5");
        assertTrue(eval instanceof LambdaExpression);
    }

    @Test
    void testSimpleLambda2() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<List<?>>eval("[5, 15].stream().filter(x -> 15 > 10).toList()");
        assertEquals(2, eval.size());
        assertEquals(5L, eval.get(0));
        assertEquals(15L, eval.get(1));
    }

    @Test
    void testSimpleLambda3() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<List<?>>eval("[5, 15].stream().filter(x -> 15 < 10).toList()");
        assertEquals(0, eval.size());
    }

    @Test
    void testSimpleLambda4() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<List<?>>eval("[5, 15].stream().map(x -> 10).toList()");
        assertEquals(2, eval.size());
        assertEquals(10L, eval.get(0));
        assertEquals(10L, eval.get(1));
    }

    @Test
    void testSimpleLambda5() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<List<?>>eval("[5, 15].stream().map(x -> ['a']).toList()");
        assertEquals(2, eval.size());
        assertTrue(eval.get(0) instanceof List);
        assertTrue(eval.get(1) instanceof List);
    }


    @Test
    void testSimpleLambda6() {
        var elProcessor = new ELProcessor();
        var eval = (List<?>) elProcessor.eval("[5, 15].stream().filter(x -> x > 10).toList()");
        assertEquals(1, eval.size());
        assertEquals(15L, eval.get(0));
    }

    @Test
    void testSimpleLambda7() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Long>eval("[5, 15].stream().count()");
        assertEquals(2, eval);
    }


    @Test
    void testSimpleLambda8() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Boolean>eval("[5, 15].stream().anyMatch(x -> x > 10)");
        assertTrue(eval);
    }

    @Test
    void testSimpleLambda9() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Long>eval("[5, 15].stream().findFirst().orElse(54)");
        assertEquals(5L, eval);
    }


    @Test
    void testSimpleLambda10() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Long>eval("[].stream().findFirst().orElse(54)");
        assertEquals(54L, eval);
    }


    @Test
    void testSimpleLambda11() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<List<?>>eval("['a', 'b', 'b', 'c'].stream().distinct().toList()");
        assertEquals(3, eval.size());
    }

    @Test
    void testSimpleLambda12() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<List<?>>eval("[1,3,2,4].stream().sorted().toList()");
        assertEquals(4, eval.size());
        assertEquals(1L, eval.get(0));
        assertEquals(2L, eval.get(1));
        assertEquals(3L, eval.get(2));
        assertEquals(4L, eval.get(3));
    }

    @Test
    void testSimpleLambda13() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<List<?>>eval("[1,3,2,4].stream().sorted((p, q) -> p > q ? p : p == q ? 0 : -1).toList()");
        assertEquals(4, eval.size());
        assertEquals(1L, eval.get(0));
        assertEquals(2L, eval.get(1));
        assertEquals(3L, eval.get(2));
        assertEquals(4L, eval.get(3));
    }

    @Test
    void testSimpleLambda14() {
        var elProcessor = new ELProcessor();
        var eval = (Long) elProcessor.eval("[1,2,3,4,5].stream().reduce(0, (l,r)->l+r)");
        assertEquals(15L, eval);
    }

    @Test
    void testSimpleLambda15() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Optional<Long>>eval("[1,2,3,4,5].stream().reduce((l,r)->l+r)");
        assertEquals(15L, eval.get());
    }

    @Test
    void testLambda16() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Long>eval("[].stream().min().orElseGet(() -> 5)");
        assertEquals(5L, eval);
    }

    @Test
    void testLambda17() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Long>eval("(() -> 5)()");
        assertEquals(5L, eval);
    }

    @Test
    void testLambda18() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Long>eval("(x->(y->x + y)(5))(10)");
        assertEquals(15L, eval);
    }

    @Test
    void testLambda19() {
        var elProcessor = new ELProcessor();
        elProcessor.defineBean("a", 5);
        elProcessor.defineBean("b", 15);
        var eval = elProcessor.<Long>eval("(()->y->y + a)()(b)");
        assertEquals(20L, eval);
    }

    @Test
    void testLambda20() {
        var elProcessor = new ELProcessor();
        var eval = elProcessor.<Long>eval("f = ()->5; f()");
        assertEquals(5L, eval);
    }

    @Test
    void testLambda21() {
        var elProcessor = new ELProcessor();
        elProcessor.defineBean("a", 5);
        elProcessor.defineBean("b", 15);
        var eval = elProcessor.<Long>eval("f = (x)->(tem=x; y->tem + y); f(a)(b)");
        assertEquals(20L, eval);
    }

}
