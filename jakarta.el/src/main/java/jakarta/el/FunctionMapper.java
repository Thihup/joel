package jakarta.el;

import java.lang.reflect.Method;

/**
 * The interface to a map between Jakarta Expression Language function names and methods.
 *
 * <p>
 * A <code>FunctionMapper</code> maps <code>${prefix:name()}</code> style functions to a static method that can execute
 * that function.
 *
 * @since Jakarta Server Pages 2.1
 */
public abstract class FunctionMapper {
    /**
     * Adds a static method that can be used as a function.
     *
     * @param prefix    the prefix of the function, or "" if no prefix. For example, <code>"fn"</code> in
     *                  <code>${fn:method()}</code>, or <code>""</code> in <code>${method()}</code>.
     * @param localName the short name of the function. For example, <code>"method"</code> in <code>${fn:method()}</code>.
     * @param method    The static method that is to be invoked, when the function is referenced. The null value causes the
     *                  function to be removed from the map.
     * @since Jakarta Expression Language 3.0
     */
    public void mapFunction(String prefix, String localName, Method method) {
    }

    /**
     * Resolves the specified prefix and local name into a <code>java.lang.Method</code>.
     *
     * <p>
     * Returns <code>null</code> if no function could be found that matches the given prefix and local name.
     * </p>
     *
     * @param prefix    the prefix of the function, or "" if no prefix. For example, <code>"fn"</code> in
     *                  <code>${fn:method()}</code>, or <code>""</code> in <code>${method()}</code>.
     * @param localName the short name of the function. For example, <code>"method"</code> in <code>${fn:method()}</code>.
     * @return the static method to invoke, or <code>null</code> if no match was found.
     */
    public abstract Method resolveFunction(String prefix, String localName);
}
