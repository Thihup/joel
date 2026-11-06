package dev.thihup.joel.impl.compiler;

import jakarta.el.ELContext;

@FunctionalInterface
public interface CompiledExpression {
    Object evaluate(ELContext context) throws Throwable;
}

