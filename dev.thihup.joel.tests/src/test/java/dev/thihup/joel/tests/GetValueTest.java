package dev.thihup.joel.tests;

import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GetValueTest {

    @Test
    void getTypeAndBooleanExpressionFalse() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true && false}", Boolean.class);
        assertEquals(Boolean.FALSE, valueExpression.getValue(context));
    }

    @Test
    void getTypeAndBooleanExpressionFalse2() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${false && false}", Boolean.class);
        assertEquals(Boolean.FALSE, valueExpression.getValue(context));
    }

    @Test
    void getTypeAndBooleanExpressionTrue() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true && true}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }

    @Test
    void getTypeOrBooleanExpressionTrue() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true || false}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }


    @Test
    void getTypeOrBooleanExpressionTrue2() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${false || true}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }

    @Test
    void getTypeOrBooleanExpressionTrue3() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true || true}", Boolean.class);
        assertEquals(Boolean.TRUE, valueExpression.getValue(context));
    }


    @Test
    void getTypeOrBooleanExpressionFalse() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${false || false}", Boolean.class);
        assertEquals(Boolean.FALSE, valueExpression.getValue(context));
    }

    @Test
    void getValueLongExpression() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1}", Long.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals(1L, valueExpression.<Long>getValue(context));
    }

    @Test
    void getValueDoubleExpression() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1.}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals(1.d, valueExpression.getValue(context));
    }

    @Test
    void getValueDoubleExpression2() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1.0}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals(1.0d, valueExpression.getValue(context));
    }

    @Test
    void getValueDoubleExpression3() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1e0}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals(1e0, valueExpression.getValue(context));
    }

    @Test
    void getValueStringExpression() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${\"hi\"}", String.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals("hi", valueExpression.getValue(context));
    }

    @Test
    void getValueStringExpression2() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${'hi'}", String.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getValue(context).getClass());
        assertEquals("hi", valueExpression.getValue(context));
    }

    @Test
    void getValueStaticMemberAccess() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${Integer.MAX_VALUE}", int.class);
        assertEquals(Integer.MAX_VALUE, valueExpression.<Integer>getValue(context));
    }

    @Test
    void getValueStaticMemberAccess2() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${Double.MIN_VALUE}", double.class);
        assertEquals(Double.MIN_VALUE, valueExpression.getValue(context));
    }

    @ParameterizedTest
    @CsvSource("Collections.EMPTY_LIST,Collections['EMPTY_LIST'],Collections['EMPTY' += '_LIST']")
    void getValueStaticMemberAccess3(String value) {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        context.getImportHandler().importPackage("java.util");
        var valueExpression = factory.createValueExpression(context, "${%s}".formatted(value), List.class);
        assertEquals(Collections.EMPTY_LIST, valueExpression.getValue(context));
    }

    @Test
    void getValuePlusOperator() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1 + 2}", double.class);
        assertEquals(3.0, valueExpression.getValue(context));
    }

    @Test
    void getValueUnaryMinus() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${-1}", int.class);
        assertEquals(-1, valueExpression.<Integer>getValue(context));
    }

    @Test
    void getValueUnaryMinus2() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${-Double.MAX_VALUE}", double.class);
        assertEquals(-Double.MAX_VALUE, valueExpression.getValue(context));
    }

    @Test
    void getValueUnaryNot() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${! true}", boolean.class);
        assertEquals(false, valueExpression.getValue(context));
    }

    @Test
    void getValueUnaryNot2() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${not true}", boolean.class);
        assertEquals(false, valueExpression.getValue(context));
    }

    @Test
    void getValueUnaryEmpty() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${empty ''}", boolean.class);
        assertEquals(true, valueExpression.getValue(context));
    }

    @Test
    void getValueUnaryEmpty2() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${empty 'a'}", boolean.class);
        assertEquals(false, valueExpression.getValue(context));
    }

    @Test
    void compositeExpression() {
        var factory = ExpressionFactory.newInstance();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${'hello'} ${'composite'}", String.class);
        assertEquals("hello composite", valueExpression.getValue(context));
    }

}
