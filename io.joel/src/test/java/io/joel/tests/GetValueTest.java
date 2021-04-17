package io.joel.tests;

import io.joel.impl.spi.JoelExpressionFactory;
import jakarta.el.StandardELContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GetValueTest {

    @Test
    void getTypeAndBooleanExpressionFalse() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true && false}", Boolean.class);
        assertEquals(Boolean.FALSE, valueExpression.getValue(context));
    }

    @Test
    void getTypeAndBooleanExpressionFalse2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${false && false}", Boolean.class);
        assertEquals(Boolean.FALSE, valueExpression.getValue(context));
    }

    @Test
    void getTypeAndBooleanExpressionTrue() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true && true}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }

    @Test
    void getTypeOrBooleanExpressionTrue() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true || false}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }


    @Test
    void getTypeOrBooleanExpressionTrue2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${false || true}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }

    @Test
    void getTypeOrBooleanExpressionTrue3() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true || true}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }


    @Test
    void getTypeOrBooleanExpressionFalse() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${false || false}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }

    @Test
    void getValueLongExpression() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1}", Long.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals(1L, valueExpression.getValue(context));
    }

    @Test
    void getValueDoubleExpression() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1.}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals(1.d, valueExpression.getValue(context));
    }

    @Test
    void getValueDoubleExpression2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1.0}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals(1.0d, valueExpression.getValue(context));
    }

    @Test
    void getValueDoubleExpression3() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1e0}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals(1e0, valueExpression.getValue(context));
    }

    @Test
    void getValueStringExpression() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${\"hi\"}", String.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals("hi", valueExpression.getValue(context));
    }

    @Test
    void getValueStringExpression2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${'hi'}", String.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals("hi", valueExpression.getValue(context));
    }

    @Test
    void getValueStaticMemberAccess() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${Integer.MAX_VALUE}", int.class);
        assertEquals(Integer.MAX_VALUE, valueExpression.getValue(context));
    }

    @Test
    void getValueStaticMemberAccess2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${Double.MIN_VALUE}", double.class);
        assertEquals(Double.MIN_VALUE, valueExpression.getValue(context));
    }

    @ParameterizedTest
    @CsvSource("Collections.EMPTY_LIST,Collections['EMPTY_LIST'],Collections['EMPTY' += '_LIST']")
    void getValueStaticMemberAccess3(String value) {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        context.getImportHandler().importPackage("java.util");
        var valueExpression = factory.createValueExpression(context, "${%s}".formatted(value), List.class);
        assertEquals(Collections.EMPTY_LIST, valueExpression.getValue(context));
    }

    @Test
    void getValuePlusOperator() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1 + 2}", double.class);
        assertEquals(3, valueExpression.getValue(context));
    }

}
