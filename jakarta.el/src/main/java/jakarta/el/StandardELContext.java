package jakarta.el;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A standard ELContext suitable for use in a stand alone environment. This class provides a default implementation of
 * an ELResolver that contains a number of useful ELResolvers. It also provides local repositories for the
 * FunctionMapper, VariableMapper, and BeanNameResolver.
 *
 * @since Jakarta Expression Language 3.0
 */
public class StandardELContext extends ELContext {

    final BeanNameResolver localBeanRepository = new LocalBeanRepository();
    private final CompositeELResolver customResolvers = new CompositeELResolver();
    private final FunctionMapper functionMapper;
    private final VariableMapper variableMapper;
    private final CompositeELResolver rootResolver;

    /**
     * Construct a StandardELContext from another ELContext.
     *
     * @param context The ELContext that acts as a delegate in most cases
     */
    public StandardELContext(ELContext context) {
        functionMapper = context.getFunctionMapper();
        variableMapper = context.getVariableMapper();
        setLocale(context.getLocale());

        rootResolver = new CompositeELResolver();
        rootResolver.add(new BeanNameELResolver(localBeanRepository));
        rootResolver.add(context.getELResolver());
    }

    /**
     * Construct a default ELContext for a stand-alone environment.
     *
     * @param expressionFactory The ExpressionFactory
     */
    public StandardELContext(ExpressionFactory expressionFactory) {
        functionMapper = new DefaultFunctionMapper(expressionFactory.getInitFunctionMap());
        variableMapper = new DefaultVariableMapper();
        rootResolver = computeResolver(expressionFactory.getStreamELResolver());
        putContext(ExpressionFactory.class, expressionFactory);
    }

    /**
     * Add a custom ELResolver to the context. The list of the custom ELResolvers will be accessed in the order they are
     * added. A custom ELResolver added to the context cannot be removed.
     *
     * @param elResolver The new ELResolver to be added to the context
     */
    public void addELResolver(ELResolver elResolver) {
        customResolvers.add(elResolver);
    }

    /**
     * Construct (if needed) and return a default ELResolver.
     *
     * <p>
     * Retrieves the <code>ELResolver</code> associated with this context. This is a <code>CompositeELResover</code>
     * consists of an ordered list of <code>ELResolver</code>s.
     *
     * <ol>
     * <li>A {@link BeanNameELResolver} for beans defined locally</li>
     * <li>Any custom <code>ELResolver</code>s</li>
     * <li>An <code>ELResolver</code> supporting the collection operations</li>
     * <li>A {@link StaticFieldELResolver} for resolving static fields</li>
     * <li>A {@link MapELResolver} for resolving Map properties</li>
     * <li>A {@link ResourceBundleELResolver} for resolving ResourceBundle properties</li>
     * <li>A {@link ListELResolver} for resolving List properties</li>
     * <li>An {@link ArrayELResolver} for resolving array properties</li>
     * <li>A {@link BeanELResolver} for resolving bean properties</li>
     * </ol>
     *
     * @return The ELResolver for this context.
     */
    @Override
    public ELResolver getELResolver() {
        return rootResolver;
    }

    private CompositeELResolver computeResolver(ELResolver streamELResolver) {
        CompositeELResolver resolver = new CompositeELResolver();
        resolver.add(new BeanNameELResolver(localBeanRepository));
        resolver.add(customResolvers);
        if (streamELResolver != null)
            resolver.add(streamELResolver);
        // "Collection operations"
        resolver.add(new StaticFieldELResolver());
        resolver.add(new MapELResolver());
        resolver.add(new ResourceBundleELResolver());
        resolver.add(new ListELResolver());
        resolver.add(new ArrayELResolver());
        resolver.add(new BeanELResolver());
        return resolver;
    }

    /**
     * Construct (if needed) and return a default FunctionMapper.
     *
     * @return The default FunctionMapper
     */
    @Override
    public FunctionMapper getFunctionMapper() {
        return functionMapper;
    }

    /**
     * Construct (if needed) and return a default VariableMapper.
     *
     * @return The default Variable
     */
    @Override
    public VariableMapper getVariableMapper() {
        return variableMapper;
    }

    private static class LocalBeanRepository extends BeanNameResolver {
        private final Map<String, Object> localBeans = new HashMap<>();

        @Override
        public boolean isReadOnly(String beanName) {
            return false;
        }

        @Override
        public boolean canCreateBean(String beanName) {
            return true;
        }

        @Override
        public Object getBean(String beanName) {
            return localBeans.get(beanName);
        }

        @Override
        public boolean isNameResolved(String beanName) {
            return localBeans.containsKey(beanName);
        }

        @Override
        public void setBeanValue(String beanName, Object value) {
            if (localBeans.containsKey(beanName) && value == null) {
                localBeans.remove(beanName);
                return;
            }
            localBeans.put(beanName, value);
        }
    }

    private static final class DefaultFunctionMapper extends FunctionMapper {
        private final Map<String, Method> mapper;

        private DefaultFunctionMapper(Map<String, Method> mapper) {
            this.mapper = (mapper == null) ? new HashMap<>() : new HashMap<>(mapper);
        }

        @Override
        public void mapFunction(String prefix, String localName, Method method) {
            mapper.put(prefix + ":" + localName, method);
        }

        @Override
        public Method resolveFunction(String prefix, String localName) {
            return mapper.get(prefix + ":" + localName);
        }
    }

    private static final class DefaultVariableMapper extends VariableMapper {
        private final Map<String, ValueExpression> mapper = new HashMap<>();

        @Override
        public ValueExpression resolveVariable(String variable) {
            return mapper.get(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression value) {
            return mapper.put(variable, value);
        }
    }

}
