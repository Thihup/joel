package jakarta.el;

import java.lang.reflect.Method;

public class ELManager {

    public ELManager() {
    }

    public static ExpressionFactory getExpressionFactory() {
        return ExpressionFactory.newInstance();
    }

    public void addBeanNameResolver(BeanNameResolver beanNameResolver) {
    }

    public void addELResolver(ELResolver elResolver) {
    }

    public void addEvaluationListener(EvaluationListener evaluationListener) {
    }

    public Object defineBean(String name, Object bean) {
        return null;
    }

    public StandardELContext getELContext() {
        return null;
    }

    public void importClass(String className) {
    }

    public void importPackage(String packageName) {
    }

    public void importStatic(String staticMemberName) {
    }

    public void mapFunction(String prefix, String function, Method method) {
    }

    public ELContext setELContext(ELContext context) {
        return null;
    }

    public void setVariable(String variable, ValueExpression expression) {
    }

}
