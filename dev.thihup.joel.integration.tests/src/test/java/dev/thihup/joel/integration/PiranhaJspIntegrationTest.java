package dev.thihup.joel.integration;

import cloud.piranha.core.impl.DefaultWebApplication;
import cloud.piranha.embedded.EmbeddedPiranha;
import cloud.piranha.embedded.EmbeddedPiranhaBuilder;
import cloud.piranha.embedded.EmbeddedRequest;
import cloud.piranha.embedded.EmbeddedRequestBuilder;
import cloud.piranha.embedded.EmbeddedResponse;
import cloud.piranha.extension.wasp.WaspInitializer;
import dev.thihup.joel.integration.beans.IntegrationBeans.AddressBean;
import dev.thihup.joel.integration.beans.IntegrationBeans.UserBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Piranha Cloud JSP (WaSP) Integration Tests with Joel")
@EnabledForJreRange(min = JRE.JAVA_21, disabledReason = "Piranha Cloud requires Java 21+")
public class PiranhaJspIntegrationTest {

    @Test
    @DisplayName("Render full JSP page with EL expressions, streams, lambda, and bean properties")
    void testRenderJspPage() throws Exception {
        DefaultWebApplication webApp = new DefaultWebApplication();

        UserBean user = new UserBean("usr-1", "Thiago", "Admin", true, 250, new AddressBean("Florianopolis", "Brazil"));
        user.setRoles(List.of("ADMIN", "DEVELOPER", "ARCHITECT"));

        EmbeddedPiranha piranha = new EmbeddedPiranhaBuilder()
                .webApplication(webApp)
                .stringResource("/dashboard.jsp", """
                        <%@page contentType="text/html" pageEncoding="UTF-8"%>
                        <!DOCTYPE html>
                        <html>
                        <head><title>Dashboard</title></head>
                        <body>
                            <h1>User Profile</h1>
                            <div class="user-info">
                                <p id="name">Name: ${user.name}</p>
                                <p id="role">Role: ${user.role}</p>
                                <p id="city">City: ${user.address.city}</p>
                                <p id="status">Status: ${user.active ? 'ACTIVE' : 'INACTIVE'}</p>
                                <p id="vip">VIP Status: ${user.points > 200 ? 'VIP Member' : 'Regular'}</p>
                            </div>

                            <h2>Calculations & Formatting</h2>
                            <div class="math-ops">
                                <p id="bonus">Bonus Points: ${user.points * 2 + 50}</p>
                                <p id="concat">Greeting: ${'Welcome back, ' += user.name += '!'}</p>
                            </div>

                            <h2>Collection & Stream Operations (EL 3.0+ / EL 6.0)</h2>
                            <div class="collections">
                                <p id="filtered-roles">Roles: ${user.roles.stream().filter(r -> r.length() > 5).toList()}</p>
                                <p id="mapped-scores">Scores: ${[1, 2, 3, 4, 5].stream().map(x -> x * 10).toList()}</p>
                                <p id="map-literal">Config Value: ${ {'theme': 'dark', 'lang': 'pt-BR'}['theme'] }</p>
                                <p id="set-literal">Unique IDs: ${ {100, 200, 300}.size() }</p>
                            </div>
                        </body>
                        </html>
                        """)
                .initializer(WaspInitializer.class.getName())
                .buildAndStart();

        EmbeddedRequest request = new EmbeddedRequestBuilder()
                .servletPath("/dashboard.jsp")
                .build();
        request.setAttribute("user", user);

        EmbeddedResponse response = new EmbeddedResponse();

        piranha.service(request, response);
        piranha.stop().destroy();

        assertEquals(200, response.getStatus());
        String body = response.getResponseAsString();

        assertTrue(body.contains("<p id=\"name\">Name: Thiago</p>"), "User name rendered");
        assertTrue(body.contains("<p id=\"role\">Role: Admin</p>"), "User role rendered");
        assertTrue(body.contains("<p id=\"city\">City: Florianopolis</p>"), "Nested bean property rendered");
        assertTrue(body.contains("<p id=\"status\">Status: ACTIVE</p>"), "Ternary expression rendered");
        assertTrue(body.contains("<p id=\"vip\">VIP Status: VIP Member</p>"), "Relational expression rendered");
        assertTrue(body.contains("<p id=\"bonus\">Bonus Points: 550</p>"), "Arithmetic expression rendered");
        assertTrue(body.contains("<p id=\"concat\">Greeting: Welcome back, Thiago!</p>"), "String concat rendered");
        assertTrue(body.contains("<p id=\"filtered-roles\">Roles: [DEVELOPER, ARCHITECT]</p>"), "EL Stream filtering rendered");
        assertTrue(body.contains("<p id=\"mapped-scores\">Scores: [10, 20, 30, 40, 50]</p>"), "EL Stream mapping rendered");
        assertTrue(body.contains("<p id=\"map-literal\">Config Value: dark</p>"), "Map literal lookup rendered");
        assertTrue(body.contains("<p id=\"set-literal\">Unique IDs: 3</p>"), "Set literal size rendered");
    }

    @Test
    @DisplayName("Render JSP page with custom EL functions from TLD using Joel EL")
    void testRenderJspWithCustomFunctionsTld() throws Exception {
        DefaultWebApplication webApp = new DefaultWebApplication();

        UserBean user = new UserBean("usr-2", "antigravity", "Developer", true, 300, new AddressBean("Mountain View", "USA"));

        EmbeddedPiranha piranha = new EmbeddedPiranhaBuilder()
                .webApplication(webApp)
                .stringResource("/WEB-INF/test-functions.tld", """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <taglib xmlns="https://jakarta.ee/xml/ns/jakartaee"
                                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-jsptaglibrary_3_1.xsd"
                                version="3.1">
                            <tlib-version>1.0</tlib-version>
                            <short-name>test</short-name>
                            <uri>/WEB-INF/test-functions.tld</uri>
                            <function>
                                <name>toUpperCase</name>
                                <function-class>dev.thihup.joel.integration.beans.JspFunctions</function-class>
                                <function-signature>java.lang.String toUpperCase(java.lang.String)</function-signature>
                            </function>
                            <function>
                                <name>join</name>
                                <function-class>dev.thihup.joel.integration.beans.JspFunctions</function-class>
                                <function-signature>java.lang.String join(java.lang.String, java.lang.String)</function-signature>
                            </function>
                            <function>
                                <name>add</name>
                                <function-class>dev.thihup.joel.integration.beans.JspFunctions</function-class>
                                <function-signature>int add(int, int)</function-signature>
                            </function>
                        </taglib>
                        """)
                .stringResource("/functions.jsp", """
                        <%@page contentType="text/html" pageEncoding="UTF-8"%>
                        <%@taglib uri="/WEB-INF/test-functions.tld" prefix="fn"%>
                        <!DOCTYPE html>
                        <html>
                        <head><title>Functions Test</title></head>
                        <body>
                            <p id="upper">Upper: ${fn:toUpperCase(user.name)}</p>
                            <p id="joined">Joined: ${fn:join(user.name, user.role)}</p>
                            <p id="calc">Sum: ${fn:add(user.points, 100)}</p>
                            <p id="chained">Chained: ${fn:toUpperCase(fn:join('hello', 'world'))}</p>
                        </body>
                        </html>
                        """)
                .initializer(WaspInitializer.class.getName())
                .buildAndStart();

        EmbeddedRequest request = new EmbeddedRequestBuilder()
                .servletPath("/functions.jsp")
                .build();
        request.setAttribute("user", user);

        EmbeddedResponse response = new EmbeddedResponse();

        piranha.service(request, response);
        piranha.stop().destroy();

        assertEquals(200, response.getStatus());
        String body = response.getResponseAsString();

        assertTrue(body.contains("<p id=\"upper\">Upper: ANTIGRAVITY</p>"), "Upper function rendered");
        assertTrue(body.contains("<p id=\"joined\">Joined: antigravity-Developer</p>"), "Join function rendered");
        assertTrue(body.contains("<p id=\"calc\">Sum: 400</p>"), "Add function rendered");
        assertTrue(body.contains("<p id=\"chained\">Chained: HELLO-WORLD</p>"), "Chained function rendered");
    }
}
