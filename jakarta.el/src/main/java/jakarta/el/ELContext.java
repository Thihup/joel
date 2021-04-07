package jakarta.el;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class ELContext {

    public ELContext() {
    }

    public void addEvaluationListener(EvaluationListener listener) {
    }

    public Object coerceToType(Object object, Class<?> targetType) {
        return null;
    }

    public Object convertToType(Object object, Class<?> targetType) {
        return null;
    }

    public void enterLambdaScope(Map<String, Object> arguments) {
    }

    public void exitLambdaScope() {
    }

    public Object getContext(Class key) {
        return null;
    }

    public List<EvaluationListener> getEvaluationListeners() {
        return Collections.emptyList();
    }

    public ImportHandler getImportHandler() {
        return null;
    }

    public Object getLambdaArgument(String argument) {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public boolean isLambdaArgument(String argument) {
        return false;
    }

    public boolean isPropertyResolved() {
        return false;
    }

    public void notifyAfterEvaluation(String expression) {
    }

    public void notifyBeforeEvaluation(String expression) {
    }

    public void notifyPropertyResolved(Object base, Object property) {
    }

    public void putContext(Class key, Object contextObject) {
    }

    public void setLocale(Locale locale) {
    }

    public void setPropertyResolved(boolean resolved) {
    }

    public void setPropertyResolved(Object base, Object property) {
    }

    public abstract ELResolver getELResolver();
    public abstract FunctionMapper getFunctionMapper();
    public abstract VariableMapper getVariableMapper();
}
