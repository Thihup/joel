module io.joel {
    exports io.joel.impl.spi;
    exports io.joel.impl;
    requires transitive jakarta.el;
    requires com.headius.invokebinder;
    requires jdk.dynalink;
    requires org.antlr.antlr4.runtime;
    requires java.desktop;
    provides jakarta.el.ExpressionFactory with io.joel.impl.spi.JoelExpressionFactory;
}
