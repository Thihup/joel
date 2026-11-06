import dev.thihup.joel.impl.spi.JoelExpressionFactory;

@SuppressWarnings("requires-automatic")
module dev.thihup.joel {
    exports dev.thihup.joel.impl.spi;
    exports dev.thihup.joel.impl.compiler;
    exports dev.thihup.joel.impl.node;
    requires transitive jakarta.el;

    requires org.antlr.antlr4.runtime;
    requires java.desktop;
    provides jakarta.el.ExpressionFactory with JoelExpressionFactory;
}
