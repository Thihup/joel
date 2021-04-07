package jakarta.el;

public abstract class BeanNameResolver {

    public BeanNameResolver() {
    }

    public boolean canCreateBean(String beanName){
        return false;
    }

    public Object getBean(String beanName) {
        return null;
    }

    public boolean isNameResolved(String beanName) {
        return false;
    }

    public boolean isReadOnly(String beanName) {
        return false;
    }

    public void setBeanValue(String beanName, Object value) {
    }
}
