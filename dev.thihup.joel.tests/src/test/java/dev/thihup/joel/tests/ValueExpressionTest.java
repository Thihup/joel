package dev.thihup.joel.tests;

import jakarta.el.ELContext;
import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueExpressionTest {

    public static int times10(int a) {
        return a * 10;
    }

    public static class MutableBean {
        private int value;

        public MutableBean(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Test
    void reevaluateFunctionArgumentsWhenBeanMutates() throws NoSuchMethodException {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineFunction("tst", "times10", ValueExpressionTest.class.getMethod("times10", int.class));
        MutableBean bean = new MutableBean(1);
        elProcessor.defineBean("x", bean);

        ELContext context = elProcessor.getELManager().getELContext();
        ValueExpression ve = ExpressionFactory.newInstance()
                .createValueExpression(context, "${tst:times10(x.value)}", Object.class);

        assertEquals(10, ((Number) ve.getValue(context)).intValue());

        bean.setValue(5);
        assertEquals(50, ((Number) ve.getValue(context)).intValue());
    }

    @Test
    void reevaluatePropertyWhenBeanMutates() {
        ELProcessor elProcessor = new ELProcessor();
        MutableBean bean = new MutableBean(1);
        elProcessor.defineBean("x", bean);

        ELContext context = elProcessor.getELManager().getELContext();
        ValueExpression ve = ExpressionFactory.newInstance()
                .createValueExpression(context, "${x.value}", Object.class);

        assertEquals(1, ((Number) ve.getValue(context)).intValue());

        bean.setValue(5);
        assertEquals(5, ((Number) ve.getValue(context)).intValue());
    }
}
