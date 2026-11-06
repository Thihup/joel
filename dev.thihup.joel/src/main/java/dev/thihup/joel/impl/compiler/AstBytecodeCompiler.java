package dev.thihup.joel.impl.compiler;

import dev.thihup.joel.impl.node.*;
import jakarta.el.ELContext;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.constant.ConstantDescs.*;

@SuppressWarnings("exports")
public final class AstBytecodeCompiler {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final AtomicLong COUNTER = new AtomicLong();

    private static final ClassDesc CD_CompiledExpression = ClassDesc.of("dev.thihup.joel.impl.compiler.CompiledExpression");
    private static final ClassDesc CD_AstCompilerHelpers = ClassDesc.of("dev.thihup.joel.impl.compiler.AstCompilerHelpers");
    private static final ClassDesc CD_ELContext = ClassDesc.of("jakarta.el.ELContext");
    private static final ClassDesc CD_BigDecimal = ClassDesc.of("java.math.BigDecimal");
    private static final ClassDesc CD_BigInteger = ClassDesc.of("java.math.BigInteger");

    private static final ClassDesc CD_CallSite = ClassDesc.of("java.lang.invoke.CallSite");
    private static final ClassDesc CD_Lookup = ClassDesc.of("java.lang.invoke.MethodHandles$Lookup");
    private static final ClassDesc CD_MethodType = ClassDesc.of("java.lang.invoke.MethodType");

    private static final DirectMethodHandleDesc BSM_MEMBER = MethodHandleDesc.ofMethod(
            DirectMethodHandleDesc.Kind.STATIC,
            CD_AstCompilerHelpers,
            "bootstrapMember",
            MethodTypeDesc.of(CD_CallSite, CD_Lookup, CD_String, CD_MethodType)
    );

    private static final MethodTypeDesc MTD_indyMember = MethodTypeDesc.of(CD_Object, CD_Object, CD_ELContext);
    private static final MethodTypeDesc MTD_evaluate = MethodTypeDesc.of(CD_Object, CD_ELContext);
    private static final MethodTypeDesc MTD_resolveId = MethodTypeDesc.of(CD_Object, CD_ELContext, CD_String);
    private static final MethodTypeDesc MTD_evalMemberDyn = MethodTypeDesc.of(CD_Object, CD_ELContext, CD_Object, CD_Object);
    private static final MethodTypeDesc MTD_binaryOp = MethodTypeDesc.of(CD_Object, CD_Object, CD_Object, CD_ELContext);
    private static final MethodTypeDesc MTD_compareOp = MethodTypeDesc.of(CD_int, CD_Object, CD_Object, CD_ELContext);
    private static final MethodTypeDesc MTD_toBoolean = MethodTypeDesc.of(CD_boolean, CD_Object, CD_ELContext);
    private static final MethodTypeDesc MTD_valueOfBool = MethodTypeDesc.of(CD_Boolean, CD_boolean);
    private static final MethodTypeDesc MTD_valueOfInt = MethodTypeDesc.of(CD_Integer, CD_int);
    private static final MethodTypeDesc MTD_valueOfLong = MethodTypeDesc.of(CD_Long, CD_long);
    private static final MethodTypeDesc MTD_valueOfDouble = MethodTypeDesc.of(CD_Double, CD_double);
    private static final MethodTypeDesc MTD_negate = MethodTypeDesc.of(CD_Object, CD_Object, CD_ELContext);
    private static final MethodTypeDesc MTD_empty = MethodTypeDesc.of(CD_Object, CD_Object);

    private static final class CompilationContext {
        final ClassDesc classDesc;
        final List<BigDecimal> bigDecimals = new ArrayList<>();
        final List<BigInteger> bigIntegers = new ArrayList<>();
        final Map<BigDecimal, Integer> bdIndex = new HashMap<>();
        final Map<BigInteger, Integer> biIndex = new HashMap<>();

        CompilationContext(ClassDesc classDesc) {
            this.classDesc = classDesc;
        }

        int registerBigDecimal(BigDecimal bd) {
            return bdIndex.computeIfAbsent(bd, k -> {
                int idx = bigDecimals.size();
                bigDecimals.add(k);
                return idx;
            });
        }

        int registerBigInteger(BigInteger bi) {
            return biIndex.computeIfAbsent(bi, k -> {
                int idx = bigIntegers.size();
                bigIntegers.add(k);
                return idx;
            });
        }
    }

    private AstBytecodeCompiler() {
    }

    public static CompiledExpression compile(String expression) {
        Node node = dev.thihup.joel.impl.antlr.JoelExpressionParser.parse(expression);
        return compileToBytecode(node);
    }

    public static CompiledExpression compileToBytecode(Node node) {
        String className = "dev.thihup.joel.impl.compiler.CompiledExpr$" + COUNTER.incrementAndGet();
        ClassDesc classDesc = ClassDesc.of(className);
        CompilationContext ctx = new CompilationContext(classDesc);

        // Pré-coleta constantes
        collectConstants(node, ctx);

        byte[] bytes = ClassFile.of().build(classDesc, cb -> {
            cb.withInterfaceSymbols(CD_CompiledExpression);

            // Campos de constantes estáticas
            for (int i = 0; i < ctx.bigDecimals.size(); i++) {
                cb.withField("BD_" + i, CD_BigDecimal, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            }
            for (int i = 0; i < ctx.bigIntegers.size(); i++) {
                cb.withField("BI_" + i, CD_BigInteger, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            }

            // <clinit> se houver constantes
            if (!ctx.bigDecimals.isEmpty() || !ctx.bigIntegers.isEmpty()) {
                cb.withMethodBody(CLASS_INIT_NAME, MTD_void, Modifier.STATIC, clinitCode -> {
                    for (int i = 0; i < ctx.bigDecimals.size(); i++) {
                        clinitCode.new_(CD_BigDecimal);
                        clinitCode.dup();
                        clinitCode.ldc(ctx.bigDecimals.get(i).toString());
                        clinitCode.invokespecial(CD_BigDecimal, INIT_NAME, MethodTypeDesc.of(CD_void, CD_String));
                        clinitCode.putstatic(classDesc, "BD_" + i, CD_BigDecimal);
                    }
                    for (int i = 0; i < ctx.bigIntegers.size(); i++) {
                        clinitCode.new_(CD_BigInteger);
                        clinitCode.dup();
                        clinitCode.ldc(ctx.bigIntegers.get(i).toString());
                        clinitCode.invokespecial(CD_BigInteger, INIT_NAME, MethodTypeDesc.of(CD_void, CD_String));
                        clinitCode.putstatic(classDesc, "BI_" + i, CD_BigInteger);
                    }
                    clinitCode.return_();
                });
            }

            // Default constructor
            cb.withMethodBody(INIT_NAME, MTD_void, Modifier.PUBLIC, code -> {
                code.aload(0);
                code.invokespecial(CD_Object, INIT_NAME, MTD_void);
                code.return_();
            });

            // evaluate(ELContext context)
            cb.withMethodBody("evaluate", MTD_evaluate, Modifier.PUBLIC, code -> {
                compileNode(node, code, ctx);
                code.areturn();
            });
        });

        try {
            var hiddenLookup = LOOKUP.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
            var mh = hiddenLookup.findConstructor(hiddenLookup.lookupClass(), MethodType.methodType(void.class));
            return (CompiledExpression) mh.invoke();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate compiled bytecode expression", t);
        }
    }

    private static void collectConstants(Node node, CompilationContext ctx) {
        if (node instanceof NumberNode num) {
            if (num.value() instanceof BigDecimal bd) {
                ctx.registerBigDecimal(bd);
            } else if (num.value() instanceof BigInteger bi) {
                ctx.registerBigInteger(bi);
            }
        } else if (node instanceof InfixExpressionNode infix) {
            collectConstants(infix.left(), ctx);
            collectConstants(infix.right(), ctx);
        } else if (node instanceof RelationalNode rel) {
            collectConstants(rel.left(), ctx);
            collectConstants(rel.right(), ctx);
        } else if (node instanceof UnaryNotNode not) {
            collectConstants(not.node(), ctx);
        } else if (node instanceof UnaryMinusNode minus) {
            collectConstants(minus.node(), ctx);
        } else if (node instanceof UnaryEmptyNode empty) {
            collectConstants(empty.node(), ctx);
        } else if (node instanceof MemberNode member) {
            collectConstants(member.object(), ctx);
            collectConstants(member.property(), ctx);
        } else if (node instanceof TernaryNode ternary) {
            collectConstants(ternary.condition(), ctx);
            collectConstants(ternary.trueExpression(), ctx);
            collectConstants(ternary.falseExpression(), ctx);
        } else if (node instanceof ElvisNode elvis) {
            collectConstants(elvis.expression(), ctx);
            collectConstants(elvis.defaultExpression(), ctx);
        } else if (node instanceof NullCoalescingNode nc) {
            collectConstants(nc.expression(), ctx);
            collectConstants(nc.defaultExpression(), ctx);
        } else if (node instanceof DeferredExpressionNode def) {
            collectConstants(def.node(), ctx);
        } else if (node instanceof DynamicExpressionNode dyn) {
            collectConstants(dyn.node(), ctx);
        } else if (node instanceof LambdaNode lambda && lambda.parameters().isEmpty()) {
            collectConstants(lambda.expression(), ctx);
        }
    }

    private static void compileNode(Node node, CodeBuilder code, CompilationContext ctx) {
        // Literals
        if (node instanceof NullNode) {
            code.aconst_null();
            return;
        }
        if (node instanceof BooleanNode bool) {
            code.getstatic(CD_Boolean, bool == BooleanNode.TRUE ? "TRUE" : "FALSE", CD_Boolean);
            return;
        }
        if (node instanceof NumberNode num) {
            Number val = num.value();
            if (val instanceof Integer i) {
                code.ldc(i);
                code.invokestatic(CD_Integer, "valueOf", MTD_valueOfInt);
                return;
            }
            if (val instanceof Long l) {
                code.ldc(l);
                code.invokestatic(CD_Long, "valueOf", MTD_valueOfLong);
                return;
            }
            if (val instanceof Double d) {
                code.ldc(d);
                code.invokestatic(CD_Double, "valueOf", MTD_valueOfDouble);
                return;
            }
            if (val instanceof BigDecimal bd) {
                int idx = ctx.registerBigDecimal(bd);
                code.getstatic(ctx.classDesc, "BD_" + idx, CD_BigDecimal);
                return;
            }
            if (val instanceof BigInteger bi) {
                int idx = ctx.registerBigInteger(bi);
                code.getstatic(ctx.classDesc, "BI_" + idx, CD_BigInteger);
                return;
            }
        }
        if (node instanceof StringNode str) {
            code.ldc(str.value());
            return;
        }

        // Structure
        if (node instanceof IdentifierNode id) {
            code.aload(1);
            code.ldc(id.value());
            code.invokestatic(CD_AstCompilerHelpers, "resolveIdentifier", MTD_resolveId);
            return;
        }
        if (node instanceof MemberNode member) {
            if (member.property() instanceof IdentifierNode idProp) {
                compileNode(member.object(), code, ctx);
                code.aload(1); // context
                code.invokedynamic(DynamicCallSiteDesc.of(BSM_MEMBER, idProp.value(), MTD_indyMember));
                return;
            }
            code.aload(1);
            compileNode(member.object(), code, ctx);
            compileNode(member.property(), code, ctx);
            code.invokestatic(CD_AstCompilerHelpers, "resolveMemberDynamic", MTD_evalMemberDyn);
            return;
        }

        // Infix Aritmética
        if (node instanceof AddExpressionNode add) {
            compileNode(add.left(), code, ctx);
            compileNode(add.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalAddDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof SubExpressionNode sub) {
            compileNode(sub.left(), code, ctx);
            compileNode(sub.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalSubDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof MulExpressionNode mul) {
            compileNode(mul.left(), code, ctx);
            compileNode(mul.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalMulDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof DivExpressionNode div) {
            compileNode(div.left(), code, ctx);
            compileNode(div.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalDivDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof ModExpressionNode mod) {
            compileNode(mod.left(), code, ctx);
            compileNode(mod.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalModDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof ConcatNode concat) {
            compileNode(concat.left(), code, ctx);
            compileNode(concat.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalConcatDirect", MTD_binaryOp);
            return;
        }

        // Relational
        if (node instanceof EqualNode eq) {
            compileNode(eq.left(), code, ctx);
            compileNode(eq.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalEqualDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof NotEqualNode neq) {
            compileNode(neq.left(), code, ctx);
            compileNode(neq.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalNotEqualDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof LessThanNode lt) {
            compileRelationalCompare(lt.left(), lt.right(), code, ctx, 1);
            return;
        }
        if (node instanceof LessEqualNode le) {
            compileRelationalCompare(le.left(), le.right(), code, ctx, 2);
            return;
        }
        if (node instanceof GreaterThanNode gt) {
            compileRelationalCompare(gt.left(), gt.right(), code, ctx, 3);
            return;
        }
        if (node instanceof GreaterEqualNode ge) {
            compileRelationalCompare(ge.left(), ge.right(), code, ctx, 4);
            return;
        }

        // Logical com short-circuit
        if (node instanceof AndNode and) {
            var falseLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(and.left(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifeq(falseLabel);
            compileNode(and.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifeq(falseLabel);
            code.getstatic(CD_Boolean, "TRUE", CD_Boolean);
            code.goto_(endLabel);
            code.labelBinding(falseLabel);
            code.getstatic(CD_Boolean, "FALSE", CD_Boolean);
            code.labelBinding(endLabel);
            return;
        }
        if (node instanceof OrNode or) {
            var trueLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(or.left(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifne(trueLabel);
            compileNode(or.right(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifne(trueLabel);
            code.getstatic(CD_Boolean, "FALSE", CD_Boolean);
            code.goto_(endLabel);
            code.labelBinding(trueLabel);
            code.getstatic(CD_Boolean, "TRUE", CD_Boolean);
            code.labelBinding(endLabel);
            return;
        }

        // Unary
        if (node instanceof UnaryNotNode not) {
            compileNode(not.node(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.iconst_1();
            code.ixor();
            code.invokestatic(CD_Boolean, "valueOf", MTD_valueOfBool);
            return;
        }
        if (node instanceof UnaryMinusNode minus) {
            compileNode(minus.node(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "negateValue", MTD_negate);
            return;
        }
        if (node instanceof UnaryEmptyNode empty) {
            compileNode(empty.node(), code, ctx);
            code.invokestatic(CD_AstCompilerHelpers, "emptyValue", MTD_empty);
            return;
        }

        // Conditional
        if (node instanceof TernaryNode ternary) {
            var falseLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(ternary.condition(), code, ctx);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifeq(falseLabel);
            compileNode(ternary.trueExpression(), code, ctx);
            code.goto_(endLabel);
            code.labelBinding(falseLabel);
            compileNode(ternary.falseExpression(), code, ctx);
            code.labelBinding(endLabel);
            return;
        }
        if (node instanceof ElvisNode elvis) {
            var defaultLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(elvis.expression(), code, ctx);
            code.dup();
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifeq(defaultLabel);
            code.goto_(endLabel);
            code.labelBinding(defaultLabel);
            code.pop();
            compileNode(elvis.defaultExpression(), code, ctx);
            code.labelBinding(endLabel);
            return;
        }
        if (node instanceof NullCoalescingNode nc) {
            var defaultLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(nc.expression(), code, ctx);
            code.dup();
            code.ifnull(defaultLabel);
            code.goto_(endLabel);
            code.labelBinding(defaultLabel);
            code.pop();
            compileNode(nc.defaultExpression(), code, ctx);
            code.labelBinding(endLabel);
            return;
        }

        // Wrappers
        if (node instanceof DeferredExpressionNode def) {
            compileNode(def.node(), code, ctx);
            return;
        }
        if (node instanceof DynamicExpressionNode dyn) {
            compileNode(dyn.node(), code, ctx);
            return;
        }
        if (node instanceof LambdaNode lambda && lambda.parameters().isEmpty()) {
            compileNode(lambda.expression(), code, ctx);
            return;
        }

        throw new UnsupportedOperationException("Unsupported node for bytecode compilation: " + node.getClass().getName());
    }

    private static void compileRelationalCompare(Node left, Node right, CodeBuilder code, CompilationContext ctx, int op) {
        compileNode(left, code, ctx);
        compileNode(right, code, ctx);
        code.aload(1);
        code.invokestatic(CD_AstCompilerHelpers, "evalCompareDirect", MTD_compareOp);
        var trueLabel = code.newLabel();
        var endLabel = code.newLabel();
        switch (op) {
            case 1 -> code.iflt(trueLabel); // <
            case 2 -> code.ifle(trueLabel); // <=
            case 3 -> code.ifgt(trueLabel); // >
            case 4 -> code.ifge(trueLabel); // >=
        }
        code.getstatic(CD_Boolean, "FALSE", CD_Boolean);
        code.goto_(endLabel);
        code.labelBinding(trueLabel);
        code.getstatic(CD_Boolean, "TRUE", CD_Boolean);
        code.labelBinding(endLabel);
    }
}
