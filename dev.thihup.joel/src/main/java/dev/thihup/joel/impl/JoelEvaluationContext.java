package dev.thihup.joel.impl;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.EvaluationListener;
import jakarta.el.FunctionMapper;
import jakarta.el.ImportHandler;
import jakarta.el.VariableMapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class JoelEvaluationContext extends ELContext {

    private final ELContext delegate;
    private final FunctionMapper functionMapper;
    private final VariableMapper variableMapper;

    public JoelEvaluationContext(ELContext delegate, FunctionMapper functionMapper, VariableMapper variableMapper) {
        this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        this.functionMapper = functionMapper;
        this.variableMapper = variableMapper;
    }

    @Override
    public ELResolver getELResolver() {
        return delegate.getELResolver();
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return functionMapper != null ? functionMapper : delegate.getFunctionMapper();
    }

    @Override
    public VariableMapper getVariableMapper() {
        return variableMapper != null ? variableMapper : delegate.getVariableMapper();
    }

    @Override
    public void setPropertyResolved(boolean resolved) {
        delegate.setPropertyResolved(resolved);
    }

    @Override
    public void setPropertyResolved(Object base, Object property) {
        delegate.setPropertyResolved(base, property);
    }

    @Override
    public boolean isPropertyResolved() {
        return delegate.isPropertyResolved();
    }

    @Override
    public void putContext(Class<?> key, Object contextObject) {
        delegate.putContext(key, contextObject);
    }

    @Override
    public Object getContext(Class<?> key) {
        return delegate.getContext(key);
    }

    @Override
    public ImportHandler getImportHandler() {
        return delegate.getImportHandler();
    }

    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

    @Override
    public void setLocale(Locale locale) {
        delegate.setLocale(locale);
    }

    @Override
    public void addEvaluationListener(EvaluationListener listener) {
        delegate.addEvaluationListener(listener);
    }

    @Override
    public List<EvaluationListener> getEvaluationListeners() {
        return delegate.getEvaluationListeners();
    }

    @Override
    public void notifyBeforeEvaluation(String expr) {
        delegate.notifyBeforeEvaluation(expr);
    }

    @Override
    public void notifyAfterEvaluation(String expr) {
        delegate.notifyAfterEvaluation(expr);
    }

    @Override
    public void notifyPropertyResolved(Object base, Object property) {
        delegate.notifyPropertyResolved(base, property);
    }

    @Override
    public <T> T convertToType(Object obj, Class<T> targetType) {
        return delegate.convertToType(obj, targetType);
    }

    @Override
    public void enterLambdaScope(Map<String, Object> arguments) {
        delegate.enterLambdaScope(arguments);
    }

    @Override
    public void exitLambdaScope() {
        delegate.exitLambdaScope();
    }

    @Override
    public boolean isLambdaArgument(String arg) {
        return delegate.isLambdaArgument(arg);
    }

    @Override
    public Object getLambdaArgument(String arg) {
        return delegate.getLambdaArgument(arg);
    }
}
