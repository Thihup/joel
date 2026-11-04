package dev.thihup.joel.benchmark.util;

import dev.thihup.joel.impl.spi.JoelExpressionFactory;
import jakarta.el.ExpressionFactory;

public enum ELImplementation {
    JOEL {
        @Override
        public ExpressionFactory createFactory() {
            return new JoelExpressionFactory();
        }
    },
    GLASSFISH {
        @Override
        public ExpressionFactory createFactory() {
            try {
                return (ExpressionFactory) Class.forName("org.glassfish.expressly.ExpressionFactoryImpl")
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (Exception e) {
                throw new RuntimeException("GlassFish Expressly not found on classpath", e);
            }
        }
    },
    TOMCAT {
        @Override
        public ExpressionFactory createFactory() {
            try {
                return (ExpressionFactory) Class.forName("org.apache.el.ExpressionFactoryImpl")
                        .getDeclaredConstructor()
                        .newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Tomcat Jasper EL not found on classpath", e);
            }
        }
    };

    public abstract ExpressionFactory createFactory();
}
