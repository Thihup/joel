package jakarta.el;

import java.util.List;

public class LambdaExpression {

    public LambdaExpression(List<String> formalParameters, ValueExpression expression){
    }

    public Object invoke(ELContext elContext, Object... arguments) {
        return null;
    }

    public Object invoke(Object... arguments) {
        return null;
    }

    public void setELContext(ELContext context){
    }

}
