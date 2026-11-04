package dev.thihup.joel.integration;

import dev.thihup.joel.integration.beans.IntegrationBeans.AddressBean;
import dev.thihup.joel.integration.beans.IntegrationBeans.JspFunctions;
import dev.thihup.joel.integration.beans.IntegrationBeans.OrderBean;
import dev.thihup.joel.integration.beans.IntegrationBeans.UserBean;
import jakarta.el.*;
import org.apache.jasper.el.ELContextImpl;
import org.apache.jasper.runtime.JspApplicationContextImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jakarta Pages (JSP) and Tomcat Jasper integration with Joel")
public class JspJoelIntegrationTest {

    private JspApplicationContextImpl jspApplicationContext;
    private ExpressionFactory expressionFactory;
    private ELContextImpl elContext;

    private UserBean user;
    private OrderBean order;

    static class SimpleVariableMapper extends VariableMapper {
        private final Map<String, ValueExpression> vars = new HashMap<>();

        @Override
        public ValueExpression resolveVariable(String variable) {
            return vars.get(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            return vars.put(variable, expression);
        }
    }

    static class SimpleFunctionMapper extends FunctionMapper {
        private final Map<String, Method> functions = new HashMap<>();

        public void addFunction(String prefix, String localName, Method method) {
            functions.put(prefix + ":" + localName, method);
        }

        @Override
        public Method resolveFunction(String prefix, String localName) {
            return functions.get(prefix + ":" + localName);
        }
    }

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        jspApplicationContext = new JspApplicationContextImpl();
        expressionFactory = jspApplicationContext.getExpressionFactory();

        CompositeELResolver resolver = new CompositeELResolver();
        resolver.add(new MapELResolver());
        resolver.add(new ListELResolver());
        resolver.add(new ArrayELResolver());
        resolver.add(new BeanELResolver());

        elContext = new ELContextImpl(resolver);

        SimpleVariableMapper varMapper = new SimpleVariableMapper();
        AddressBean address = new AddressBean("Joinville", "Brazil");
        user = new UserBean("u2", "Mateus", "Editor", true, 200, address);
        order = new OrderBean("ORD-9876", user);

        varMapper.setVariable("user", expressionFactory.createValueExpression(user, UserBean.class));
        varMapper.setVariable("order", expressionFactory.createValueExpression(order, OrderBean.class));
        varMapper.setVariable("tags", expressionFactory.createValueExpression(List.of("jsp", "jakarta", "joel"), List.class));

        SimpleFunctionMapper funcMapper = new SimpleFunctionMapper();
        funcMapper.addFunction("fn", "toUpperCase", JspFunctions.class.getMethod("toUpperCase", String.class));
        funcMapper.addFunction("fn", "join", JspFunctions.class.getMethod("join", String.class, String.class));
        funcMapper.addFunction("fn", "add", JspFunctions.class.getMethod("add", int.class, int.class));

        elContext.setVariableMapper(varMapper);
        elContext.setFunctionMapper(funcMapper);
    }

    @Test
    @DisplayName("JspApplicationContext must use JoelExpressionFactory")
    void testJspApplicationContextUsesJoel() {
        assertNotNull(expressionFactory);
        assertEquals("dev.thihup.joel.impl.spi.JoelExpressionFactory",
                expressionFactory.getClass().getName(),
                "JSP container must use Joel ExpressionFactory");
    }

    @Test
    @DisplayName("JSP EL evaluates bean property navigation")
    void testJspBeanPropertyAccess() {
        ValueExpression nameExpr = expressionFactory.createValueExpression(elContext, "${user.name}", String.class);
        assertEquals("Mateus", nameExpr.getValue(elContext));

        ValueExpression cityExpr = expressionFactory.createValueExpression(elContext, "${user.address.city}", String.class);
        assertEquals("Joinville", cityExpr.getValue(elContext));
    }

    @Test
    @DisplayName("JSP EL evaluates functions (FunctionMapper)")
    void testJspFunctionMapper() {
        ValueExpression upperExpr = expressionFactory.createValueExpression(
                elContext, "${fn:toUpperCase(user.name)}", String.class);
        assertEquals("MATEUS", upperExpr.getValue(elContext));

        ValueExpression joinExpr = expressionFactory.createValueExpression(
                elContext, "${fn:join(user.name, user.address.city)}", String.class);
        assertEquals("Mateus-Joinville", joinExpr.getValue(elContext));

        ValueExpression addExpr = expressionFactory.createValueExpression(
                elContext, "${fn:add(user.points, 50)}", Integer.class);
        assertEquals(250, (int) addExpr.getValue(elContext));
    }

    @Test
    @DisplayName("JSP EL evaluates collections and lists")
    void testJspCollections() {
        ValueExpression tagExpr = expressionFactory.createValueExpression(elContext, "${tags[1]}", String.class);
        assertEquals("jakarta", tagExpr.getValue(elContext));
    }

    @Test
    @DisplayName("JSP EL evaluates composite and arithmetic expressions")
    void testJspArithmeticAndComposite() {
        ValueExpression arithExpr = expressionFactory.createValueExpression(
                elContext, "${user.points * 2 + 10}", Integer.class);
        assertEquals(410, (int) arithExpr.getValue(elContext));

        ValueExpression compExpr = expressionFactory.createValueExpression(
                elContext, "User: ${user.name} has ${user.points} points", String.class);
        assertEquals("User: Mateus has 200 points", compExpr.getValue(elContext));
    }

    @Test
    @DisplayName("JSP EL updates bean property with setValue()")
    void testJspSetValue() {
        ValueExpression nameExpr = expressionFactory.createValueExpression(elContext, "${user.name}", String.class);
        nameExpr.setValue(elContext, "Mateus Updated");

        assertEquals("Mateus Updated", user.getName());
        assertEquals("Mateus Updated", nameExpr.getValue(elContext));
    }

    @Test
    @DisplayName("JSP deferred #{...} and dynamic ${...} expressions evaluation")
    void testJspDeferredAndDynamicExpressions() {
        ValueExpression dynamicExpr = expressionFactory.createValueExpression(elContext, "${user.role}", String.class);
        ValueExpression deferredExpr = expressionFactory.createValueExpression(elContext, "#{user.role}", String.class);

        assertEquals("Editor", dynamicExpr.getValue(elContext));
        assertEquals("Editor", deferredExpr.getValue(elContext));
    }
}
