package jakarta.el;

import java.io.Serializable;

public abstract class ValueExpression extends Expression implements Serializable {

    public ValueReference getValueReference(ELContext context) {
        return null;
    }

    public abstract Class<?> getExpectedType();

    public abstract Class<?> getType(ELContext context);

    public abstract Object getValue(ELContext context);

    public abstract boolean isReadOnly(ELContext context);

    public abstract void setValue(ELContext context, Object value);
}
