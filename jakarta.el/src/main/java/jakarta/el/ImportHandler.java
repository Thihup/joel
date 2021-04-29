package jakarta.el;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Handles imports of class names and package names. An imported package name implicitly imports all the classes in the
 * package. A class that has been imported can be used without its package name. The name is resolved to its full
 * (package and class) name at evaluation time.
 */
public class ImportHandler {

    private final Set<String> packages = new HashSet<>();
    private final Map<String, String> staticImports = new HashMap<>();

    public ImportHandler() {
        packages.add("java.lang");
    }

    /**
     * Import a class.
     *
     * @param className The full class name of the class to be imported
     * @throws ELException if the name does not include a ".".
     */
    public void importClass(String className) {
        int index = className.lastIndexOf('.');
        if (index == -1) {
            throw new ELException("Not fully qualified classname: %s".formatted(className));
        }
        packages.add(className.substring(0, index));
    }

    /**
     * Import all the classes in a package.
     *
     * @param packageName The package name to be imported
     */
    public void importPackage(String packageName) {
        if (packageName.indexOf('.') == -1) {
            throw new ELException("Package must have a dot: %s".formatted(packageName));
        }
        packages.add(packageName);
    }

    /**
     * Import a static field or method.
     *
     * @param name The static member name, including the full class name, to be imported
     * @throws ELException if the name does not include a ".".
     */
    public void importStatic(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1) {
            throw new ELException("Not fully qualified classname: %s".formatted(name));
        }
        staticImports.putIfAbsent(name.substring(index + 1), name.substring(0, index));
    }

    /**
     * Resolve a class name.
     *
     * @param className The name of the class (without package name) to be resolved.
     * @return If the class has been imported previously, with {@link #importClass} or {@link #importPackage}, then its
     * Class instance. Otherwise <code>null</code>.
     * @throws ELException if the class is abstract or is an interface, or not public.
     */
    public Class<?> resolveClass(String className) {
        return packages.stream()
                .map(x -> loadClass(x + "." + className))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Resolve a static field or method name.
     *
     * @param name The name of the member(without package and class name) to be resolved.
     * @return If the field or method has been imported previously, with {@link #importStatic}, then the class object
     * representing the class that declares the static field or method. Otherwise <code>null</code>.
     * @throws ELException if the class is not public, or is abstract or is an interface.
     */
    public Class<?> resolveStatic(String name) {
        String s = staticImports.get(name);
        if (s == null) {
            return null;
        }
        return loadClass(s);
    }

    private Class<?> loadClass(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
