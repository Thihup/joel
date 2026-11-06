package dev.thihup.joel.impl.compiler;

import jakarta.el.ELContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class CompiledExpressionTemplate implements CompiledExpression {

    public CompiledExpressionTemplate() {
    }

    private static final MethodHandle TARGET;

    static {
        try {
            TARGET = MethodHandles.classData(MethodHandles.lookup(), "_", MethodHandle.class);
        } catch (IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public Object evaluate(ELContext context) throws Throwable {
        return TARGET.invokeExact(context);
    }
}
