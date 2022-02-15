package dev.thihup.joel.tests;

import jakarta.el.ELProcessor;
import jakarta.el.PropertyNotWritableException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ELProcessorTest {

    @Test
    void defineBean() {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("test", "hi!");
        assertEquals("hi!", elProcessor.eval("test"));
    }

    @Test
    void defineBean2() {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("customer", new Customer("John", 25));
        assertTrue(elProcessor.eval("customer") instanceof Customer);
    }


    @Test
    void defineBean3() {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("customers", List.of(new Customer("John", 25)));
        var eval = (String) elProcessor.eval("customers.stream().map(x -> x.name).findFirst().orElse(null)");
        assertEquals("John", eval);
    }

    @Test
    void setBean() {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("customer", new Customer("John", 25));
        elProcessor.setValue("customer.age", 10);
        assertEquals(10, elProcessor.<Integer>eval("customer.age"));
    }

    @Test
    void setBean2() {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.setValue("value", 10);
        assertEquals(10, elProcessor.<Integer>eval("value"));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "a = null; a + b",
            "a = null; a - b",
            "a = null; a * b",
            "a = null; a / b",
            "a = null; a % b",
            "a = null; a div b",
            "a = null; a mod b"
    })
    void nullTest(String expression) {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.setValue("b", null);
        assertEquals(0L, elProcessor.eval(expression));
    }

    @Test
    void illegalAssignOperator() {
        ELProcessor elProcessor = new ELProcessor();
        assertThrows(PropertyNotWritableException.class, () -> elProcessor.eval("1 + 1 == 2 ? 10 : a = 5"));
    }
}
