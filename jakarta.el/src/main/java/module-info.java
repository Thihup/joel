module jakarta.el {
    requires transitive java.desktop;
    requires jdk.dynalink;
    exports jakarta.el;
    uses jakarta.el.ExpressionFactory;
}
