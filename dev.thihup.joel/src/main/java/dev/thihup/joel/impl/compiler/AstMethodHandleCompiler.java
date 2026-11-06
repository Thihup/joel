package dev.thihup.joel.impl.compiler;

import dev.thihup.joel.impl.node.*;
import jakarta.el.ELContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("exports")
public final class AstMethodHandleCompiler {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodType EXPR_TYPE = MethodType.methodType(Object.class, ELContext.class);

    private static final MethodHandle MH_RESOLVE_ID;
    private static final MethodHandle MH_MEMBER_PROP;
    private static final MethodHandle MH_MEMBER_DYN;
    private static final MethodHandle MH_ADD;
    private static final MethodHandle MH_SUB;
    private static final MethodHandle MH_MUL;
    private static final MethodHandle MH_DIV;
    private static final MethodHandle MH_MOD;
    private static final MethodHandle MH_CONCAT;
    private static final MethodHandle MH_EQ;
    private static final MethodHandle MH_NEQ;
    private static final MethodHandle MH_LT;
    private static final MethodHandle MH_LE;
    private static final MethodHandle MH_GT;
    private static final MethodHandle MH_GE;
    private static final MethodHandle MH_AND;
    private static final MethodHandle MH_OR;
    private static final MethodHandle MH_NOT;
    private static final MethodHandle MH_MINUS;
    private static final MethodHandle MH_EMPTY;
    private static final MethodHandle MH_TERNARY;
    private static final MethodHandle MH_ELVIS;
    private static final MethodHandle MH_NULL_COALESCING;
    private static final MethodHandle MH_FALLBACK;

    private static final byte[] TEMPLATE_BYTES;

    static {
        try {
            Class<?> h = AstCompilerHelpers.class;
            MH_RESOLVE_ID = LOOKUP.findStatic(h, "resolveIdentifier", MethodType.methodType(Object.class, ELContext.class, String.class));
            MH_MEMBER_PROP = LOOKUP.findStatic(h, "evalMemberProp", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, String.class));
            MH_MEMBER_DYN = LOOKUP.findStatic(h, "evalMemberDynamic", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_ADD = LOOKUP.findStatic(h, "evalAdd", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_SUB = LOOKUP.findStatic(h, "evalSub", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_MUL = LOOKUP.findStatic(h, "evalMul", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_DIV = LOOKUP.findStatic(h, "evalDiv", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_MOD = LOOKUP.findStatic(h, "evalMod", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_CONCAT = LOOKUP.findStatic(h, "evalConcat", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_EQ = LOOKUP.findStatic(h, "evalEqual", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_NEQ = LOOKUP.findStatic(h, "evalNotEqual", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_LT = LOOKUP.findStatic(h, "evalLessThan", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_LE = LOOKUP.findStatic(h, "evalLessEqual", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_GT = LOOKUP.findStatic(h, "evalGreaterThan", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_GE = LOOKUP.findStatic(h, "evalGreaterEqual", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_AND = LOOKUP.findStatic(h, "evalAnd", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_OR = LOOKUP.findStatic(h, "evalOr", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_NOT = LOOKUP.findStatic(h, "evalNot", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class));
            MH_MINUS = LOOKUP.findStatic(h, "evalMinus", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class));
            MH_EMPTY = LOOKUP.findStatic(h, "evalEmpty", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class));
            MH_TERNARY = LOOKUP.findStatic(h, "evalTernary", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class, MethodHandle.class));
            MH_ELVIS = LOOKUP.findStatic(h, "evalElvis", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_NULL_COALESCING = LOOKUP.findStatic(h, "evalNullCoalescing", MethodType.methodType(Object.class, ELContext.class, MethodHandle.class, MethodHandle.class));
            MH_FALLBACK = LOOKUP.findStatic(h, "evalFallback", MethodType.methodType(Object.class, ELContext.class, Node.class));

            try (var in = AstMethodHandleCompiler.class.getClassLoader()
                    .getResourceAsStream("dev/thihup/joel/impl/compiler/CompiledExpressionTemplate.class")) {
                if (in == null) {
                    throw new IllegalStateException("CompiledExpressionTemplate.class not found in classpath");
                }
                TEMPLATE_BYTES = in.readAllBytes();
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AstMethodHandleCompiler() {
    }

    public static CompiledExpression compile(String expression) {
        Node node = dev.thihup.joel.impl.antlr.JoelExpressionParser.parse(expression);
        return compileToHiddenClass(node);
    }

    public static MethodHandle compile(Node node) {
        if (node instanceof LiteralNode literal) {
            return compileLiteral(literal);
        }
        if (node instanceof IdentifierNode id) {
            return MethodHandles.insertArguments(MH_RESOLVE_ID, 1, id.value());
        }
        if (node instanceof MemberNode member) {
            MethodHandle objMh = compile(member.object());
            if (member.property() instanceof IdentifierNode idProp) {
                return MethodHandles.insertArguments(MH_MEMBER_PROP, 1, objMh, idProp.value());
            }
            MethodHandle propMh = compile(member.property());
            return MethodHandles.insertArguments(MH_MEMBER_DYN, 1, objMh, propMh);
        }
        if (node instanceof AddExpressionNode add) {
            return MethodHandles.insertArguments(MH_ADD, 1, compile(add.left()), compile(add.right()));
        }
        if (node instanceof SubExpressionNode sub) {
            return MethodHandles.insertArguments(MH_SUB, 1, compile(sub.left()), compile(sub.right()));
        }
        if (node instanceof MulExpressionNode mul) {
            return MethodHandles.insertArguments(MH_MUL, 1, compile(mul.left()), compile(mul.right()));
        }
        if (node instanceof DivExpressionNode div) {
            return MethodHandles.insertArguments(MH_DIV, 1, compile(div.left()), compile(div.right()));
        }
        if (node instanceof ModExpressionNode mod) {
            return MethodHandles.insertArguments(MH_MOD, 1, compile(mod.left()), compile(mod.right()));
        }
        if (node instanceof ConcatNode concat) {
            return MethodHandles.insertArguments(MH_CONCAT, 1, compile(concat.left()), compile(concat.right()));
        }
        if (node instanceof EqualNode eq) {
            return MethodHandles.insertArguments(MH_EQ, 1, compile(eq.left()), compile(eq.right()));
        }
        if (node instanceof NotEqualNode neq) {
            return MethodHandles.insertArguments(MH_NEQ, 1, compile(neq.left()), compile(neq.right()));
        }
        if (node instanceof LessThanNode lt) {
            return MethodHandles.insertArguments(MH_LT, 1, compile(lt.left()), compile(lt.right()));
        }
        if (node instanceof LessEqualNode le) {
            return MethodHandles.insertArguments(MH_LE, 1, compile(le.left()), compile(le.right()));
        }
        if (node instanceof GreaterThanNode gt) {
            return MethodHandles.insertArguments(MH_GT, 1, compile(gt.left()), compile(gt.right()));
        }
        if (node instanceof GreaterEqualNode ge) {
            return MethodHandles.insertArguments(MH_GE, 1, compile(ge.left()), compile(ge.right()));
        }
        if (node instanceof AndNode and) {
            return MethodHandles.insertArguments(MH_AND, 1, compile(and.left()), compile(and.right()));
        }
        if (node instanceof OrNode or) {
            return MethodHandles.insertArguments(MH_OR, 1, compile(or.left()), compile(or.right()));
        }
        if (node instanceof UnaryNotNode not) {
            return MethodHandles.insertArguments(MH_NOT, 1, compile(not.node()));
        }
        if (node instanceof UnaryMinusNode minus) {
            return MethodHandles.insertArguments(MH_MINUS, 1, compile(minus.node()));
        }
        if (node instanceof UnaryEmptyNode empty) {
            return MethodHandles.insertArguments(MH_EMPTY, 1, compile(empty.node()));
        }
        if (node instanceof LambdaNode lambda && lambda.parameters().isEmpty()) {
            return compile(lambda.expression());
        }
        if (node instanceof TernaryNode ternary) {
            return MethodHandles.insertArguments(MH_TERNARY, 1, compile(ternary.condition()), compile(ternary.trueExpression()), compile(ternary.falseExpression()));
        }
        if (node instanceof ElvisNode elvis) {
            return MethodHandles.insertArguments(MH_ELVIS, 1, compile(elvis.expression()), compile(elvis.defaultExpression()));
        }
        if (node instanceof NullCoalescingNode nc) {
            return MethodHandles.insertArguments(MH_NULL_COALESCING, 1, compile(nc.expression()), compile(nc.defaultExpression()));
        }
        if (node instanceof DeferredExpressionNode def) {
            return compile(def.node());
        }
        if (node instanceof DynamicExpressionNode dyn) {
            return compile(dyn.node());
        }

        // Fallback para qualquer outro nó
        return MethodHandles.insertArguments(MH_FALLBACK, 1, node);
    }

    private static MethodHandle compileLiteral(LiteralNode literal) {
        Object val = switch (literal) {
            case NumberNode n -> n.value();
            case StringNode s -> s.value();
            case BooleanNode b -> b == BooleanNode.TRUE ? Boolean.TRUE : Boolean.FALSE;
            case NullNode _ -> null;
            case ObjectNode o -> o.value();
        };
        return MethodHandles.dropArguments(MethodHandles.constant(Object.class, val), 0, ELContext.class);
    }

    public static CompiledExpression compileToHiddenClass(Node node) {
        MethodHandle mh = compile(node).asType(EXPR_TYPE);
        try {
            MethodHandles.Lookup hidden = LOOKUP.defineHiddenClassWithClassData(TEMPLATE_BYTES, mh, true);
            return (CompiledExpression) hidden.findConstructor(hidden.lookupClass(), MethodType.methodType(void.class)).invoke();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to compile AST to hidden class", t);
        }
    }
}
