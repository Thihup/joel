package io.joel.tests;

import jakarta.el.ELProcessor;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        String eval = elProcessor.eval("customers.stream().map(x -> x.name).findFirst().orElse(null)");
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

}
