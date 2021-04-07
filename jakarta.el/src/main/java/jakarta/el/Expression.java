package jakarta.el;

import java.io.Serializable;

public abstract class Expression implements Serializable {

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public abstract boolean isLiteralText();

    public abstract String getExpressionString();
}
