package dev.thihup.joel.integration;

import jakarta.el.ExpressionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Joel ExpressionFactory SPI Discovery")
public class JoelSpiDiscoveryTest {

    @Test
    @DisplayName("ServiceLoader discovers JoelExpressionFactory")
    void testServiceLoaderDiscoversJoel() {
        ServiceLoader<ExpressionFactory> loader = ServiceLoader.load(ExpressionFactory.class);
        boolean foundJoel = false;
        for (ExpressionFactory factory : loader) {
            if ("dev.thihup.joel.impl.spi.JoelExpressionFactory".equals(factory.getClass().getName())) {
                foundJoel = true;
                break;
            }
        }
        assertTrue(foundJoel, "JoelExpressionFactory must be discoverable via ServiceLoader");
    }

    @Test
    @DisplayName("ExpressionFactory.newInstance() returns JoelExpressionFactory")
    void testNewInstanceReturnsJoel() {
        ExpressionFactory factory = ExpressionFactory.newInstance();
        assertNotNull(factory);
        assertEquals("dev.thihup.joel.impl.spi.JoelExpressionFactory", factory.getClass().getName());
    }

    @Test
    @DisplayName("ExpressionFactory.newInstance(Properties) returns JoelExpressionFactory")
    void testNewInstanceWithPropertiesReturnsJoel() {
        Properties properties = new Properties();
        properties.setProperty("jakarta.el.cacheSize", "1000");
        ExpressionFactory factory = ExpressionFactory.newInstance(properties);
        assertNotNull(factory);
        assertEquals("dev.thihup.joel.impl.spi.JoelExpressionFactory", factory.getClass().getName());
    }
}

