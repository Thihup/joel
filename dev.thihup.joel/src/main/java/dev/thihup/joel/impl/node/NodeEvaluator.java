package dev.thihup.joel.impl.node;

import dev.thihup.joel.impl.FunctionResolutionCache;
import dev.thihup.joel.impl.Utils;
import dev.thihup.joel.impl.calculator.Operation;
import jakarta.el.ELClass;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ImportHandler;
import jakarta.el.LambdaExpression;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueReference;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"cast", "rawtypes", "unchecked"})
public final class NodeEvaluator {

    private static final Object[] EMPTY_ARGS = new Object[0];

    static final ClassValue<ELClass> EL_CLASS_CACHE = new ClassValue<>() {
        @Override
        protected ELClass computeValue(Class<?> type) {
            return new ELClass(type);
        }
    };

    private NodeEvaluator() {
    }

    public static Object eval(Node node, ELContext context) {
        return switch (node) {
            case LiteralNode l -> evalLiteral(l);
            case InfixExpressionNode in -> evalInfix(in, context);
            case RelationalNode r -> evalRelational(r, context);
            case StructureNode s -> evalStructure(s, context);
            case ConditionalNode c -> evalConditional(c, context);
            case UnaryNode u -> evalUnary(u, context);
            case CollectionNode col -> evalCollection(col, context);
            case WrapperNode w -> evalWrapper(w, context);
        };
    }

    private static Object evalLiteral(LiteralNode l) {
        return switch (l) {
            case NumberNode n -> n.value();
            case StringNode s -> s.value();
            case BooleanNode b -> b == BooleanNode.TRUE ? Boolean.TRUE : Boolean.FALSE;
            case NullNode _ -> null;
            case ObjectNode o -> o.value();
        };
    }

    private static Object evalInfix(InfixExpressionNode in, ELContext context) {
        return switch (in) {
            case AddExpressionNode add -> Operation.ADDITION.calculate(eval(add.left(), context), eval(add.right(), context), context);
            case SubExpressionNode sub -> Operation.SUBTRACTION.calculate(eval(sub.left(), context), eval(sub.right(), context), context);
            case MulExpressionNode mul -> Operation.MULTIPLICATION.calculate(eval(mul.left(), context), eval(mul.right(), context), context);
            case DivExpressionNode div -> Operation.DIVISION.calculate(eval(div.left(), context), eval(div.right(), context), context);
            case ModExpressionNode mod -> Operation.MODULO.calculate(eval(mod.left(), context), eval(mod.right(), context), context);
            case ConcatNode concat -> evalConcat(concat, context);
            case AssignNode assign -> evalAssign(assign, context);
        };
    }

    private static Object evalRelational(RelationalNode r, ELContext context) {
        return switch (r) {
            case EqualNode eq -> EqualNode.equals(eval(eq.left(), context), eval(eq.right(), context), context);
            case NotEqualNode neq -> !EqualNode.equals(eval(neq.left(), context), eval(neq.right(), context), context);
            case LessThanNode lt -> evalLessThan(lt, context);
            case LessEqualNode le -> evalLessEqual(le, context);
            case GreaterThanNode gt -> evalGreaterThan(gt, context);
            case GreaterEqualNode ge -> evalGreaterEqual(ge, context);
            case AndNode and -> evalAnd(and, context);
            case OrNode or -> evalOr(or, context);
        };
    }

    private static Object evalStructure(StructureNode s, ELContext context) {
        return switch (s) {
            case IdentifierNode id -> evalIdentifier(id, context);
            case MemberNode member -> evalMember(member, context);
            case CallExpressionNode call -> evalCall(call, context);
        };
    }

    private static Object evalConditional(ConditionalNode c, ELContext context) {
        return switch (c) {
            case TernaryNode ternary -> evalTernary(ternary, context);
            case ElvisNode elvis -> evalElvis(elvis, context);
            case NullCoalescingNode nc -> evalNullCoalescing(nc, context);
        };
    }

    private static Object evalUnary(UnaryNode u, ELContext context) {
        return switch (u) {
            case UnaryNotNode not -> evalNot(not, context);
            case UnaryMinusNode minus -> evalMinus(minus, context);
            case UnaryEmptyNode empty -> evalEmpty(empty, context);
        };
    }

    private static Object evalCollection(CollectionNode col, ELContext context) {
        return switch (col) {
            case ListNode list -> evalList(list, context);
            case MapNode map -> evalMap(map, context);
            case SetNode set -> evalSet(set, context);
        };
    }

    private static Object evalWrapper(WrapperNode w, ELContext context) {
        return switch (w) {
            case DeferredExpressionNode def -> eval(def.node(), context);
            case DynamicExpressionNode dyn -> eval(dyn.node(), context);
            case LambdaNode lambda -> new LambdaExpression(lambda.parameters(), lambda.bodyExpr());
            case SemicolonNode sc -> {
                eval(sc.left(), context);
                yield eval(sc.right(), context);
            }
        };
    }

    private static Object evalLessThan(LessThanNode lt, ELContext context) {
        var leftValue = eval(lt.left(), context);
        var rightValue = eval(lt.right(), context);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        return RelationalNode.compare(leftValue, rightValue, context) < 0;
    }

    private static Object evalLessEqual(LessEqualNode le, ELContext context) {
        var leftValue = eval(le.left(), context);
        var rightValue = eval(le.right(), context);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        return RelationalNode.compare(leftValue, rightValue, context) <= 0;
    }

    private static Object evalGreaterThan(GreaterThanNode gt, ELContext context) {
        var leftValue = eval(gt.left(), context);
        var rightValue = eval(gt.right(), context);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        return RelationalNode.compare(leftValue, rightValue, context) > 0;
    }

    private static Object evalGreaterEqual(GreaterEqualNode ge, ELContext context) {
        var leftValue = eval(ge.left(), context);
        var rightValue = eval(ge.right(), context);
        if (leftValue == null || rightValue == null) {
            return false;
        }
        return RelationalNode.compare(leftValue, rightValue, context) >= 0;
    }

    private static Object evalAnd(AndNode and, ELContext context) {
        Object leftVal = eval(and.left(), context);
        if (!toBoolean(leftVal, context))
            return false;
        Object rightVal = eval(and.right(), context);
        return toBoolean(rightVal, context);
    }

    private static Object evalOr(OrNode or, ELContext context) {
        Object leftVal = eval(or.left(), context);
        if (toBoolean(leftVal, context))
            return true;
        Object rightVal = eval(or.right(), context);
        return toBoolean(rightVal, context);
    }

    private static boolean toBoolean(Object val, ELContext context) {
        if (val instanceof Boolean b) return b;
        return (boolean) context.convertToType(val, boolean.class);
    }

    private static Object evalNot(UnaryNotNode not, ELContext context) {
        Object val = eval(not.node(), context);
        if (val instanceof Boolean b) {
            return !b;
        }
        return !(boolean) context.convertToType(val, boolean.class);
    }

    private static Object evalMinus(UnaryMinusNode minus, ELContext context) {
        Object value = eval(minus.node(), context);
        if (value == null)
            return 0L;
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.negate();
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.negate();
        }
        if (value instanceof String asString) {
            if (Utils.isFloatingPointNumber(asString)) {
                return -(Double) context.convertToType(asString, Double.class);
            }
            return -(Long) context.convertToType(asString, Long.class);
        }
        if (value instanceof Long asLong) {
            return -asLong;
        }
        if (value instanceof Integer asInt) {
            return -asInt;
        }
        if (value instanceof Double asDouble) {
            return -asDouble;
        }
        if (value instanceof Float asFloat) {
            return -asFloat;
        }
        if (value instanceof Short asShort) {
            return (short) -asShort;
        }
        if (value instanceof Byte asByte) {
            return (byte) -asByte;
        }
        throw new ELException("Could not perform unary minus on: " + value);
    }

    private static Object evalEmpty(UnaryEmptyNode empty, ELContext context) {
        Object value = eval(empty.node(), context);
        if (value == null)
            return true;
        if (value instanceof String newValue && newValue.isEmpty())
            return true;
        if (value.getClass().isArray() && Array.getLength(value) == 0)
            return true;
        if (value instanceof Map<?, ?> newValue && newValue.isEmpty())
            return true;
        return value instanceof Collection<?> newValue && newValue.isEmpty();
    }

    private static Object evalTernary(TernaryNode ternary, ELContext context) {
        if ((boolean) context.convertToType(eval(ternary.condition(), context), boolean.class)) {
            return eval(ternary.trueExpression(), context);
        }
        return eval(ternary.falseExpression(), context);
    }

    private static Object evalElvis(ElvisNode elvis, ELContext context) {
        Object val = eval(elvis.expression(), context);
        if ((boolean) context.convertToType(val, boolean.class)) {
            return val;
        }
        return eval(elvis.defaultExpression(), context);
    }

    private static Object evalNullCoalescing(NullCoalescingNode nc, ELContext context) {
        Object val = eval(nc.expression(), context);
        if (val != null) {
            return val;
        }
        return eval(nc.defaultExpression(), context);
    }

    private static String toConcatString(Object val, ELContext context) {
        if (val instanceof String s) return s;
        if (val == null) return "";
        return (String) context.convertToType(val, String.class);
    }

    private static Object evalConcat(ConcatNode concat, ELContext context) {
        Object leftVal = eval(concat.left(), context);
        Object rightVal = eval(concat.right(), context);
        if (leftVal instanceof Map leftMap && rightVal instanceof Map rightMap) {
            try {
                leftMap.putAll(rightMap);
                return leftMap;
            } catch (Exception e) {
                throw new ELException(e);
            }
        }
        if (leftVal instanceof Set leftSet && rightVal instanceof Collection rightColl) {
            try {
                leftSet.addAll(rightColl);
                return leftSet;
            } catch (Exception e) {
                throw new ELException(e);
            }
        }
        if (leftVal instanceof List leftList && rightVal instanceof Collection rightColl) {
            try {
                leftList.addAll(rightColl);
                return leftList;
            } catch (Exception e) {
                throw new ELException(e);
            }
        }
        return toConcatString(leftVal, context) + toConcatString(rightVal, context);
    }

    private static Object evalAssign(AssignNode assign, ELContext context) {
        Node left = assign.left();
        Node right = assign.right();
        if (left instanceof IdentifierNode identifierNode) {
            Object value = evalAssignValue(context, right);
            context.getELResolver().setValue(context, null, identifierNode.value(), value);
            return value;
        }
        if (left instanceof MemberNode memberNode) {
            ValueReference valueReference = memberNode.valueReference(context);
            Object value = evalAssignValue(context, right);
            context.getELResolver().setValue(context, valueReference.getBase(), valueReference.getProperty(), value);
            return value;
        }
        throw new PropertyNotWritableException("Illegal syntax for assign operation");
    }

    private static Object evalAssignValue(ELContext context, Object node) {
        if (node instanceof MemberNode memberNode)
            return evalAssignValue(context, memberNode.valueReference(context));
        if (node instanceof ValueReference reference)
            return context.getELResolver().getValue(context, reference.getBase(), reference.getProperty());
        if (node instanceof Node expressionNode)
            return eval(expressionNode, context);
        return null;
    }

    private static Object evalIdentifier(IdentifierNode id, ELContext context) {
        String value = id.value();
        if (context.isLambdaArgument(value))
            return context.getLambdaArgument(value);
        var variableMapper = context.getVariableMapper();
        if (variableMapper != null) {
            var valueExpression = variableMapper.resolveVariable(value);
            if (valueExpression != null) {
                return valueExpression.getValue(context);
            }
        }
        context.setPropertyResolved(false);
        Object result = context.getELResolver().getValue(context, null, value);
        if (context.isPropertyResolved()) {
            return result;
        }
        ImportHandler importHandler = context.getImportHandler();
        if (importHandler != null) {
            Class<?> aClass = importHandler.resolveClass(value);
            if (aClass != null) {
                return EL_CLASS_CACHE.get(aClass);
            }
            aClass = importHandler.resolveStatic(value);
            if (aClass != null) {
                return context.getELResolver().getValue(context, EL_CLASS_CACHE.get(aClass), value);
            }
        }

        throw new PropertyNotFoundException("Property %s not found".formatted(value));
    }

    private static Object evalMember(MemberNode member, ELContext context) {
        Object base = eval(member.object(), context);
        if (base == null) {
            return null;
        }
        Object prop = member.property() instanceof IdentifierNode node ? node.value() : eval(member.property(), context);
        context.setPropertyResolved(false);
        try {
            return context.getELResolver().getValue(context, base, prop);
        } catch (ELException rootCause) {
            if (member.object() instanceof IdentifierNode node) {
                ImportHandler importHandler = context.getImportHandler();
                if (importHandler != null) {
                    Class<?> aClass = importHandler.resolveClass(node.value());
                    if (aClass != null)
                        return context.getELResolver().getValue(context, EL_CLASS_CACHE.get(aClass), prop);
                }
            }
            throw new PropertyNotFoundException(member.prettyPrint(), rootCause);
        }
    }

    private static Object[] evaluateArguments(CallExpressionNode call, ELContext context) {
        var arguments = call.arguments();
        int size = arguments.size();
        if (size == 0) return EMPTY_ARGS;
        Object[] args = new Object[size];
        for (int i = 0; i < size; i++) {
            args[i] = eval(arguments.get(i), context);
        }
        return args;
    }

    private static Object evalCall(CallExpressionNode call, ELContext context) {
        Node callee = call.callee();
        if (callee instanceof CallExpressionNode callNode) {
            Object value = eval(callNode, context);
            if (value instanceof Node node) {
                return eval(node, context);
            }
            if (value instanceof LambdaExpression lambdaExpression) {
                return lambdaExpression.invoke(context, evaluateArguments(call, context));
            }
            return value;
        }
        if (callee instanceof LambdaNode lambdaNode) {
            return ((LambdaExpression) eval(lambdaNode, context)).invoke(context, evaluateArguments(call, context));
        }
        if (callee instanceof MemberNode memberNode) {
            Object base = memberNode.getBase(context);
            Object prop = memberNode.getProperty(context);
            var objects = evaluateArguments(call, context);
            return context.getELResolver().invoke(context, base, prop, null, objects);
        }
        if (callee instanceof IdentifierNode identifierNode) {
            String idValue = identifierNode.value();
            int colon = idValue.indexOf(':');
            String prefix = colon < 0 ? "" : idValue.substring(0, colon);
            String localName = colon < 0 ? idValue : idValue.substring(colon + 1);

            if (colon >= 0) {
                return resolveQualifiedFunction(call, context, identifierNode, prefix, localName);
            }
            if (context.getFunctionMapper() != null
                    && context.getFunctionMapper().resolveFunction("", idValue) != null) {
                return resolveQualifiedFunction(call, context, identifierNode, "", idValue);
            }
            try {
                if (eval(identifierNode, context) instanceof LambdaExpression lambdaExpression) {
                    return lambdaExpression.invoke(context, evaluateArguments(call, context));
                }
            } catch (PropertyNotFoundException ignored) {
            }
            return resolveQualifiedFunction(call, context, identifierNode, prefix, localName);
        }
        throw new UnsupportedOperationException();
    }

    private static Object resolveQualifiedFunction(CallExpressionNode call, ELContext context, IdentifierNode identifierNode, String prefix, String localName) {
        var functionMapper = context.getFunctionMapper();
        if (functionMapper == null) {
            throw new MethodNotFoundException("Function '%s' not found: no FunctionMapper available".formatted(identifierNode.value()));
        }

        var resolved = FunctionResolutionCache.resolve(functionMapper, prefix, localName);
        if (resolved == null) {
            throw new MethodNotFoundException("Function '%s' not found".formatted(identifierNode.value()));
        }

        return invokeResolvedMethod(call, context, resolved.method(), resolved.parameterTypes());
    }

    private static Object invokeResolvedMethod(CallExpressionNode call, ELContext context, Method method, Class<?>[] paramTypes) {
        var arguments = call.arguments();
        if (paramTypes.length != arguments.size()) {
            throw new MethodNotFoundException("Method " + method.getName() + " requires " + paramTypes.length + " parameters, but " + arguments.size() + " were supplied");
        }

        int size = arguments.size();
        Object[] objects = new Object[size];
        for (int i = 0; i < size; i++) {
            Object val = eval(arguments.get(i), context);
            Class<?> targetType = paramTypes[i];
            if (val != null && (targetType == Object.class || targetType.isInstance(val))) {
                objects[i] = val;
            } else {
                objects[i] = convertArgument(context, val, targetType);
            }
        }

        try {
            return method.invoke(null, objects);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof ELException elException) {
                throw elException;
            }
            throw new ELException(cause);
        } catch (IllegalAccessException exception) {
            throw new ELException(exception);
        }
    }

    private static Object convertArgument(ELContext context, Object value, Class<?> targetType) {
        if (value != null && (targetType == Object.class || targetType.isInstance(value))) {
            return value;
        }
        if (value instanceof LambdaExpression lambda && targetType.isInterface()) {
            Method abstractMethod = Arrays.stream(targetType.getMethods())
                    .filter(candidate -> !candidate.isDefault() && candidate.getDeclaringClass() != Object.class)
                    .findFirst()
                    .orElseThrow(() -> new MethodNotFoundException("No functional method in " + targetType));
            return Proxy.newProxyInstance(targetType.getClassLoader(), new Class<?>[]{targetType}, (proxy, method, args) -> {
                if (method.equals(abstractMethod)) {
                    Object result = lambda.invoke(context, args == null ? EMPTY_ARGS : args);
                    return method.getReturnType() == void.class ? null : context.convertToType(result, method.getReturnType());
                }
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(lambda, args);
                }
                return method.isDefault() ? method.invoke(proxy, args) : null;
            });
        }
        return context.convertToType(value, targetType);
    }

    private static Object evalList(ListNode list, ELContext context) {
        var values = list.values();
        int size = values.size();
        ArrayList<Object> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(eval(values.get(i), context));
        }
        return result;
    }

    private static Object evalMap(MapNode map, ELContext context) {
        Map<Object, Object> result = new LinkedHashMap<>();
        for (var entry : map.entries()) {
            result.put(eval(entry.getKey(), context), eval(entry.getValue(), context));
        }
        return result;
    }

    private static Object evalSet(SetNode set, ELContext context) {
        var values = set.values();
        int size = values.size();
        HashSet<Object> result = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            result.add(eval(values.get(i), context));
        }
        return result;
    }
}
