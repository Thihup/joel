package io.joel.tests;

import io.joel.impl.spi.JoelExpressionFactory;
import jakarta.el.StandardELContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GetTypeTest {

    @Test
    void getTypeBooleanExpression() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true && false}", Boolean.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeLongExpression() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1}", Long.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeDoubleExpression() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1.}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeDoubleExpression2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1.0}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeDoubleExpression3() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${1e0}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeStringExpression() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${\"hi\"}", String.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeStringExpression2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${'hi'}", String.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeStaticMemberAccess() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${Integer.MAX_VALUE}", int.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeStaticMemberAccess2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${Double.MIN_VALUE}", double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @ParameterizedTest
    @CsvSource("Collections.EMPTY_LIST,Collections['EMPTY_LIST'],Collections['EMPTY' += '_LIST']")
    void getTypeStaticMemberAccess3() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        context.getImportHandler().importPackage("java.util");
        var valueExpression = factory.createValueExpression(context, "${Collections.EMPTY_LIST}", List.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeTernaryExpression() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true ? true : false}", Boolean.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeTernaryExpression2() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true ? 2 : 3.0}", Long.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeTernaryExpression3() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${false ? 2 : 3.0}", Double.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getTypeTernaryExpression4() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${true ? Integer.MAX_VALUE : 3.0}", int.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

    @Test
    void getValueUnaryEmpty() {
        var factory = new JoelExpressionFactory();
        var context = new StandardELContext(factory);
        var valueExpression = factory.createValueExpression(context, "${empty ''}", Boolean.class);
        assertEquals(valueExpression.getExpectedType(), valueExpression.getType(context));
    }

}
