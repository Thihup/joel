package io.joel.impl;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class JoelMethodExpression extends MethodExpression {


    @Override
    public boolean isLiteralText() {
        return false;
    }

    @Override
    public String getExpressionString() {
        return null;
    }

    @Override
    public MethodInfo getMethodInfo(ELContext context) {
        return null;
    }

    @Override
    public Object invoke(ELContext context, Object[] params) {
        context.notifyBeforeEvaluation(getExpressionString());
        context.notifyAfterEvaluation(getExpressionString());
        return null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
