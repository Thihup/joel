package dev.thihup.joel.integration;

import cloud.piranha.core.impl.DefaultWebApplication;
import cloud.piranha.embedded.EmbeddedPiranha;
import cloud.piranha.embedded.EmbeddedPiranhaBuilder;
import cloud.piranha.embedded.EmbeddedRequest;
import cloud.piranha.embedded.EmbeddedRequestBuilder;
import cloud.piranha.embedded.EmbeddedResponse;
import com.sun.faces.RIConstants;
import dev.thihup.joel.integration.beans.FacesCartBean;
import jakarta.enterprise.inject.spi.BeanManager;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Piranha Cloud Jakarta Faces (Mojarra) Integration Tests with Joel")
public class PiranhaFacesIntegrationTest {

    private static Weld weld;
    private static WeldContainer container;

    @BeforeAll
    static void initCdi() {
        weld = new Weld();
        container = weld.initialize();
    }

    @AfterAll
    static void shutdownCdi() {
        if (weld != null) {
            weld.shutdown();
        }
    }

    @Test
    @DisplayName("Render simple Faces page with Joel EL arithmetic and string operations")
    void testRenderSimpleFacesPage() throws Exception {
        BeanManager beanManager = container.getBeanManager();

        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.setInitParameter("com.sun.faces.forceLoadConfiguration", "true");

        EmbeddedPiranha piranha = new EmbeddedPiranhaBuilder()
                .webApplication(webApp)
                .stringResource("/simple.xhtml", """
                        <?xml version='1.0' encoding='UTF-8' ?>
                        <!DOCTYPE html>
                        <html xmlns="http://www.w3.org/1999/xhtml"
                              xmlns:h="jakarta.faces.html">
                            <h:head>
                                <title>Faces Test</title>
                            </h:head>
                            <h:body>
                                <h1>Sum is #{10 + 25}</h1>
                                <p>Message: #{'Welcome to ' += 'Jakarta Faces with Joel!'}</p>
                            </h:body>
                        </html>
                        """)
                .attribute(RIConstants.CDI_BEAN_MANAGER, beanManager)
                .attribute("org.jboss.weld.environment.servlet.jakarta.enterprise.inject.spi.BeanManager", beanManager)
                .listener("com.sun.faces.config.ConfigureListener")
                .servlet("FacesServlet", "jakarta.faces.webapp.FacesServlet")
                .servletMapping("FacesServlet", "*.xhtml")
                .buildAndStart();

        EmbeddedRequest request = new EmbeddedRequestBuilder()
                .servletPath("/simple.xhtml")
                .build();
        EmbeddedResponse response = new EmbeddedResponse();

        BoundRequestContext requestContext = container.select(BoundRequestContext.class).get();
        Map<String, Object> requestDataStore = new HashMap<>();
        requestContext.associate(requestDataStore);
        requestContext.activate();

        try {
            piranha.service(request, response);
        } finally {
            requestContext.invalidate();
            requestContext.deactivate();
            requestContext.dissociate(requestDataStore);
            piranha.stop().destroy();
        }

        assertEquals(200, response.getStatus());
        String body = response.getResponseAsString();
        assertTrue(body.contains("Sum is 35"), () -> "Expected sum in output: " + body);
        assertTrue(body.contains("Welcome to Jakarta Faces with Joel!"), () -> "Expected message in output: " + body);
    }

    @Test
    @DisplayName("Render full Facelet with CDI @Named backing bean, UI repeat, and conditional components")
    void testRenderFullFaceletWithCdiBeanAndComponents() throws Exception {
        BeanManager beanManager = container.getBeanManager();

        DefaultWebApplication webApp = new DefaultWebApplication();
        webApp.setInitParameter("com.sun.faces.forceLoadConfiguration", "true");

        EmbeddedPiranha piranha = new EmbeddedPiranhaBuilder()
                .webApplication(webApp)
                .stringResource("/checkout.xhtml", """
                        <?xml version='1.0' encoding='UTF-8' ?>
                        <!DOCTYPE html>
                        <html xmlns="http://www.w3.org/1999/xhtml"
                              xmlns:h="jakarta.faces.html"
                              xmlns:ui="jakarta.faces.facelets">
                            <h:head>
                                <title>Checkout - Shopping Cart</title>
                            </h:head>
                            <h:body>
                                <h1>Shopping Cart for <h:outputText value="#{cartBean.customerName}"/></h1>

                                <div class="membership">
                                    <p>Tier: <h:outputText value="#{cartBean.premiumMember ? 'VIP GOLD' : 'STANDARD'}"/></p>
                                    <p>Points: <h:outputText value="#{cartBean.loyaltyPoints}"/></p>
                                    <h:panelGroup id="vipBadge" rendered="#{cartBean.loyaltyPoints > 200}">
                                        <span class="badge">Eligible for 10% Member Discount</span>
                                    </h:panelGroup>
                                    <h:panelGroup id="guestNotice" rendered="#{not cartBean.premiumMember}">
                                        <span>Standard shipping fees apply</span>
                                    </h:panelGroup>
                                </div>

                                <h2>Cart Items</h2>
                                <ul class="item-list">
                                    <ui:repeat value="#{cartBean.items}" var="item">
                                        <li>
                                            <h:outputText value="#{item.name}"/> - 
                                            Qty: <h:outputText value="#{item.quantity}"/> @ 
                                            $<h:outputText value="#{item.price}"/> = 
                                            $<h:outputText value="#{item.subtotal}"/>
                                        </li>
                                    </ui:repeat>
                                </ul>

                                <div class="summary">
                                    <p>Total Items: <h:outputText value="#{cartBean.items.size()}"/></p>
                                    <p>Item Names: <h:outputText value="#{cartBean.items.stream().map(i -> i.name).toList()}"/></p>
                                    <p>Subtotal: $<h:outputText value="#{cartBean.totalAmount}"/></p>
                                    <p>Discounted Total: $<h:outputText value="#{cartBean.discountedTotal}"/></p>
                                </div>
                            </h:body>
                        </html>
                        """)
                .attribute(RIConstants.CDI_BEAN_MANAGER, beanManager)
                .attribute("org.jboss.weld.environment.servlet.jakarta.enterprise.inject.spi.BeanManager", beanManager)
                .listener("com.sun.faces.config.ConfigureListener")
                .servlet("FacesServlet", "jakarta.faces.webapp.FacesServlet")
                .servletMapping("FacesServlet", "*.xhtml")
                .buildAndStart();

        EmbeddedRequest request = new EmbeddedRequestBuilder()
                .servletPath("/checkout.xhtml")
                .build();
        EmbeddedResponse response = new EmbeddedResponse();

        BoundRequestContext requestContext = container.select(BoundRequestContext.class).get();
        Map<String, Object> requestDataStore = new HashMap<>();
        requestContext.associate(requestDataStore);
        requestContext.activate();

        try {
            piranha.service(request, response);
        } finally {
            requestContext.invalidate();
            requestContext.deactivate();
            requestContext.dissociate(requestDataStore);
            piranha.stop().destroy();
        }

        assertEquals(200, response.getStatus());
        String body = response.getResponseAsString();

        // Customer & Tier
        assertTrue(body.contains("Thiago"), "Customer name evaluated via CDI bean");
        assertTrue(body.contains("VIP GOLD"), "Ternary expression evaluated");
        assertTrue(body.contains("350"), "Loyalty points evaluated");

        // Conditional rendering
        assertTrue(body.contains("Eligible for 10% Member Discount"), "Rendered condition (loyaltyPoints > 200) true");
        assertFalse(body.contains("Standard shipping fees apply"), "Rendered condition (not premiumMember) false");

        // Iteration & Calculations
        assertTrue(body.contains("Mechanical Keyboard"), "Item 1 evaluated");
        assertTrue(body.contains("120.0"), "Item 1 price evaluated");
        assertTrue(body.contains("Ergonomic Mouse"), "Item 2 evaluated");
        assertTrue(body.contains("90.0"), "Item 2 subtotal evaluated");
        assertTrue(body.contains("USB-C Hub"), "Item 3 evaluated");
        assertTrue(body.contains("35.0"), "Item 3 subtotal evaluated");

        // Totals & Stream operations
        assertTrue(body.contains("Total Items:3"), "Size evaluated");
        assertTrue(body.contains("[Mechanical Keyboard, Ergonomic Mouse, USB-C Hub]"), "EL Stream mapping evaluated");
        assertTrue(body.contains("Subtotal: $245.0"), "Subtotal evaluated");
        assertTrue(body.contains("Discounted Total: $220.5"), "Discounted total (10% off) evaluated");
    }
}
