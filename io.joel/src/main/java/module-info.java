module io.joel {
    exports io.joel.impl.spi;
    exports io.joel.impl.mapper;
    requires transitive jakarta.el;

    requires org.antlr.antlr4.runtime;
    requires java.desktop;
    provides jakarta.el.ExpressionFactory with io.joel.impl.spi.JoelExpressionFactory;
}
