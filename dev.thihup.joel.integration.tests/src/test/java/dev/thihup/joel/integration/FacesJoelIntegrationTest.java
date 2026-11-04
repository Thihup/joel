package dev.thihup.joel.integration;

import com.sun.faces.el.ELContextImpl;
import com.sun.faces.facelets.el.TagMethodExpression;
import com.sun.faces.facelets.el.TagValueExpression;
import com.sun.faces.facelets.tag.TagAttributeImpl;
import dev.thihup.joel.integration.beans.IntegrationBeans.AddressBean;
import dev.thihup.joel.integration.beans.IntegrationBeans.OrderBean;
import dev.thihup.joel.integration.beans.IntegrationBeans.UserBean;
import jakarta.el.*;
import jakarta.faces.view.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jakarta Faces (Mojarra) and Facelets integration with Joel")
public class FacesJoelIntegrationTest {

    private ExpressionFactory expressionFactory;
    private ELContextImpl elContext;

    private UserBean user;
    private OrderBean order;

    @BeforeEach
    void setUp() {
        expressionFactory = ELManager.getExpressionFactory();

        CompositeELResolver resolver = new CompositeELResolver();
        resolver.add(new MapELResolver());
        resolver.add(new ListELResolver());
        resolver.add(new ArrayELResolver());
        resolver.add(new BeanELResolver());

        elContext = new ELContextImpl(resolver);

        AddressBean address = new AddressBean("Florianopolis", "Brazil");
        user = new UserBean("u1", "Thiago", "Admin", true, 150, address);
        order = new OrderBean("ORD-12345", user);

        VariableMapper varMapper = elContext.getVariableMapper();
        varMapper.setVariable("user", expressionFactory.createValueExpression(user, UserBean.class));
        varMapper.setVariable("order", expressionFactory.createValueExpression(order, OrderBean.class));
    }

    @Test
    @DisplayName("ELManager.getExpressionFactory() must return JoelExpressionFactory")
    void testFacesUsesJoelExpressionFactory() {
        assertNotNull(expressionFactory, "ExpressionFactory must not be null");
        assertEquals("dev.thihup.joel.impl.spi.JoelExpressionFactory",
                expressionFactory.getClass().getName(),
                "Faces ELManager must resolve JoelExpressionFactory");
    }

    @Test
    @DisplayName("Faces ValueExpression resolves bean properties")
    void testValueExpressionBeanProperty() {
        ValueExpression nameExpr = expressionFactory.createValueExpression(elContext, "#{user.name}", String.class);
        assertEquals("Thiago", nameExpr.getValue(elContext));

        ValueExpression pointsExpr = expressionFactory.createValueExpression(elContext, "#{user.points}", Integer.class);
        assertEquals(150, (int) pointsExpr.getValue(elContext));
    }

    @Test
    @DisplayName("Faces ValueExpression resolves nested bean properties")
    void testValueExpressionNestedProperty() {
        ValueExpression cityExpr = expressionFactory.createValueExpression(elContext, "#{user.address.city}", String.class);
        assertEquals("Florianopolis", cityExpr.getValue(elContext));

        ValueExpression countryExpr = expressionFactory.createValueExpression(elContext, "#{order.customer.address.country}", String.class);
        assertEquals("Brazil", countryExpr.getValue(elContext));
    }

    @Test
    @DisplayName("Faces ValueExpression updates bean properties (postback/form submit)")
    void testValueExpressionSetValue() {
        ValueExpression nameExpr = expressionFactory.createValueExpression(elContext, "#{user.name}", String.class);
        nameExpr.setValue(elContext, "Thiago Updated");

        assertEquals("Thiago Updated", user.getName());
        assertEquals("Thiago Updated", nameExpr.getValue(elContext));
    }

    @Test
    @DisplayName("Faces ValueExpression evaluates boolean expressions (e.g. rendered attribute)")
    void testRenderedExpression() {
        ValueExpression renderedExpr1 = expressionFactory.createValueExpression(
                elContext, "#{user.active and user.points > 100}", Boolean.class);
        assertEquals(Boolean.TRUE, renderedExpr1.getValue(elContext));

        ValueExpression renderedExpr2 = expressionFactory.createValueExpression(
                elContext, "#{!user.active or user.points < 50}", Boolean.class);
        assertEquals(Boolean.FALSE, renderedExpr2.getValue(elContext));
    }

    @Test
    @DisplayName("Faces ValueExpression evaluates ternary operator expressions")
    void testTernaryExpression() {
        ValueExpression statusExpr = expressionFactory.createValueExpression(
                elContext, "#{user.active ? 'Account Active' : 'Account Inactive'}", String.class);
        assertEquals("Account Active", statusExpr.getValue(elContext));
    }

    @Test
    @DisplayName("Faces ValueExpression evaluates composite text and EL expressions")
    void testCompositeExpression() {
        ValueExpression compositeExpr = expressionFactory.createValueExpression(
                elContext, "Welcome #{user.name}, you have #{user.points} points!", String.class);
        assertEquals("Welcome Thiago, you have 150 points!", compositeExpr.getValue(elContext));
    }

    @Test
    @DisplayName("Facelets TagValueExpression wraps Joel ValueExpression")
    void testFaceletsTagValueExpression() {
        ValueExpression underlyingExpr = expressionFactory.createValueExpression(elContext, "#{user.name}", String.class);
        TagAttributeImpl attr = new TagAttributeImpl(new Location("index.xhtml", 10, 5), "", "value", "value", "#{user.name}");
        TagValueExpression tagValueExpr = new TagValueExpression(attr, underlyingExpr);

        assertEquals("Thiago", tagValueExpr.getValue(elContext));
        assertEquals(String.class, tagValueExpr.getType(elContext));

        tagValueExpr.setValue(elContext, "Thiago Via Facelets");
        assertEquals("Thiago Via Facelets", user.getName());
        assertEquals("Thiago Via Facelets", tagValueExpr.getValue(elContext));
    }

    @Test
    @DisplayName("Faces MethodExpression invokes action without parameters")
    void testMethodExpressionActionNoParams() {
        MethodExpression actionExpr = expressionFactory.createMethodExpression(
                elContext, "#{order.submitOrder}", String.class, new Class<?>[0]);

        assertFalse(order.isSubmitted());
        Object result = actionExpr.invoke(elContext, new Object[0]);
        assertEquals("order-confirmed", result);
        assertTrue(order.isSubmitted());
    }

    @Test
    @DisplayName("Facelets TagMethodExpression wraps Joel MethodExpression")
    void testFaceletsTagMethodExpression() {
        MethodExpression underlyingExpr = expressionFactory.createMethodExpression(
                elContext, "#{order.submitOrder}", String.class, new Class<?>[0]);
        TagAttributeImpl attr = new TagAttributeImpl(new Location("index.xhtml", 15, 5), "", "action", "action", "#{order.submitOrder}");
        TagMethodExpression tagMethodExpr = new TagMethodExpression(attr, underlyingExpr);

        assertFalse(order.isSubmitted());
        Object result = tagMethodExpr.invoke(elContext, new Object[0]);
        assertEquals("order-confirmed", result);
        assertTrue(order.isSubmitted());
    }

    @Test
    @DisplayName("Faces MethodExpression invokes method with parameters")
    void testMethodExpressionActionWithParams() {
        MethodExpression addExpr = expressionFactory.createMethodExpression(
                elContext, "#{order.addItem('Laptop', 3)}", Integer.class, new Class<?>[0]);

        Object result = addExpr.invoke(elContext, new Object[0]);
        assertEquals(3, result);
        assertEquals(List.of("Laptop", "Laptop", "Laptop"), order.getItems());
    }

    @Test
    @DisplayName("Faces EL evaluation with Map and List structures")
    void testCollectionsInFaces() {
        Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("locale", "pt_BR");
        sessionMap.put("cart", List.of("ItemA", "ItemB", "ItemC"));

        elContext.getVariableMapper().setVariable(
                "sessionScope", expressionFactory.createValueExpression(sessionMap, Map.class));

        ValueExpression localeExpr = expressionFactory.createValueExpression(
                elContext, "#{sessionScope['locale']}", String.class);
        assertEquals("pt_BR", localeExpr.getValue(elContext));

        ValueExpression cartItemExpr = expressionFactory.createValueExpression(
                elContext, "#{sessionScope.cart[1]}", String.class);
        assertEquals("ItemB", cartItemExpr.getValue(elContext));
    }
}
