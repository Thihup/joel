package jakarta.el;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides information about the method to which a method expression resolves.
 */
public class MethodReference {

    private final Object base;
    private final MethodInfo methodInfo;
    private final Annotation[] annotations;
    private final Object[] evaluatedParameters;

    public MethodReference(Object base, MethodInfo methodInfo, Annotation[] annotations, Object[] evaluatedParameters) {
        this.base = base;
        this.methodInfo = methodInfo;
        this.annotations = annotations;
        this.evaluatedParameters = evaluatedParameters;
    }

    public Object getBase() {
        return base;
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public Object[] getEvaluatedParameters() {
        return evaluatedParameters;
    }

    @Override
    public int hashCode() {
        int result = 31 + Arrays.hashCode(annotations);
        result = 31 * result + (base == null ? 0 : base.hashCode());
        result = 31 * result + Arrays.deepHashCode(evaluatedParameters);
        return 31 * result + (methodInfo == null ? 0 : methodInfo.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodReference other)) {
            return false;
        }
        return Arrays.equals(annotations, other.annotations)
                && Arrays.deepEquals(evaluatedParameters, other.evaluatedParameters)
                && Objects.equals(base, other.base)
                && Objects.equals(methodInfo, other.methodInfo);
    }
}
