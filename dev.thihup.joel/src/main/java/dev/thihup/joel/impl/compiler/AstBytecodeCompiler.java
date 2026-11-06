package dev.thihup.joel.impl.compiler;

import dev.thihup.joel.impl.node.*;
import jakarta.el.ELContext;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.constant.ConstantDescs.*;

@SuppressWarnings("exports")
public final class AstBytecodeCompiler {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final AtomicLong COUNTER = new AtomicLong();

    private static final ClassDesc CD_CompiledExpression = ClassDesc.of("dev.thihup.joel.impl.compiler.CompiledExpression");
    private static final ClassDesc CD_AstCompilerHelpers = ClassDesc.of("dev.thihup.joel.impl.compiler.AstCompilerHelpers");
    private static final ClassDesc CD_NodeEvaluator = ClassDesc.of("dev.thihup.joel.impl.node.NodeEvaluator");
    private static final ClassDesc CD_ELContext = ClassDesc.of("jakarta.el.ELContext");
    private static final ClassDesc CD_BigDecimal = ClassDesc.of("java.math.BigDecimal");
    private static final ClassDesc CD_BigInteger = ClassDesc.of("java.math.BigInteger");

    private static final MethodTypeDesc MTD_evaluate = MethodTypeDesc.of(CD_Object, CD_ELContext);
    private static final MethodTypeDesc MTD_resolveId = MethodTypeDesc.of(CD_Object, CD_ELContext, CD_String);
    private static final MethodTypeDesc MTD_evalMemberProp = MethodTypeDesc.of(CD_Object, CD_ELContext, CD_Object, CD_String);
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

    private AstBytecodeCompiler() {
    }

    public static CompiledExpression compile(String expression) {
        Node node = dev.thihup.joel.impl.antlr.JoelExpressionParser.parse(expression);
        return compileToBytecode(node);
    }

    public static CompiledExpression compileToBytecode(Node node) {
        String className = "dev.thihup.joel.impl.compiler.CompiledExpr$" + COUNTER.incrementAndGet();
        ClassDesc classDesc = ClassDesc.of(className);

        byte[] bytes = ClassFile.of().build(classDesc, cb -> {
            cb.withInterfaceSymbols(CD_CompiledExpression);

            // Default constructor
            cb.withMethodBody(INIT_NAME, MTD_void, Modifier.PUBLIC, code -> {
                code.aload(0);
                code.invokespecial(CD_Object, INIT_NAME, MTD_void);
                code.return_();
            });

            // evaluate(ELContext context)
            cb.withMethodBody("evaluate", MTD_evaluate, Modifier.PUBLIC, code -> {
                compileNode(node, code);
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

    private static void compileNode(Node node, CodeBuilder code) {
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
                code.new_(CD_BigDecimal);
                code.dup();
                code.ldc(bd.toString());
                code.invokespecial(CD_BigDecimal, INIT_NAME, MethodTypeDesc.of(CD_void, CD_String));
                return;
            }
            if (val instanceof BigInteger bi) {
                code.new_(CD_BigInteger);
                code.dup();
                code.ldc(bi.toString());
                code.invokespecial(CD_BigInteger, INIT_NAME, MethodTypeDesc.of(CD_void, CD_String));
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
            code.aload(1);
            compileNode(member.object(), code);
            if (member.property() instanceof IdentifierNode idProp) {
                code.ldc(idProp.value());
                code.invokestatic(CD_AstCompilerHelpers, "resolveMemberProperty", MTD_evalMemberProp);
                return;
            }
            compileNode(member.property(), code);
            code.invokestatic(CD_AstCompilerHelpers, "resolveMemberDynamic", MTD_evalMemberDyn);
            return;
        }

        // Infix Aritmética
        if (node instanceof AddExpressionNode add) {
            compileNode(add.left(), code);
            compileNode(add.right(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalAddDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof SubExpressionNode sub) {
            compileNode(sub.left(), code);
            compileNode(sub.right(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalSubDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof MulExpressionNode mul) {
            compileNode(mul.left(), code);
            compileNode(mul.right(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalMulDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof DivExpressionNode div) {
            compileNode(div.left(), code);
            compileNode(div.right(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalDivDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof ModExpressionNode mod) {
            compileNode(mod.left(), code);
            compileNode(mod.right(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalModDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof ConcatNode concat) {
            compileNode(concat.left(), code);
            compileNode(concat.right(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalConcatDirect", MTD_binaryOp);
            return;
        }

        // Relational
        if (node instanceof EqualNode eq) {
            compileNode(eq.left(), code);
            compileNode(eq.right(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalEqualDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof NotEqualNode neq) {
            compileNode(neq.left(), code);
            compileNode(neq.right(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "evalNotEqualDirect", MTD_binaryOp);
            return;
        }
        if (node instanceof LessThanNode lt) {
            compileRelationalCompare(lt.left(), lt.right(), code, 1);
            return;
        }
        if (node instanceof LessEqualNode le) {
            compileRelationalCompare(le.left(), le.right(), code, 2);
            return;
        }
        if (node instanceof GreaterThanNode gt) {
            compileRelationalCompare(gt.left(), gt.right(), code, 3);
            return;
        }
        if (node instanceof GreaterEqualNode ge) {
            compileRelationalCompare(ge.left(), ge.right(), code, 4);
            return;
        }

        // Logical com short-circuit
        if (node instanceof AndNode and) {
            var falseLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(and.left(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifeq(falseLabel);
            compileNode(and.right(), code);
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
            compileNode(or.left(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifne(trueLabel);
            compileNode(or.right(), code);
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
            compileNode(not.node(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.iconst_1();
            code.ixor();
            code.invokestatic(CD_Boolean, "valueOf", MTD_valueOfBool);
            return;
        }
        if (node instanceof UnaryMinusNode minus) {
            compileNode(minus.node(), code);
            code.aload(1);
            code.invokestatic(CD_NodeEvaluator, "negateValue", MTD_negate);
            return;
        }
        if (node instanceof UnaryEmptyNode empty) {
            compileNode(empty.node(), code);
            code.invokestatic(CD_NodeEvaluator, "emptyValue", MTD_empty);
            return;
        }

        // Conditional
        if (node instanceof TernaryNode ternary) {
            var falseLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(ternary.condition(), code);
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifeq(falseLabel);
            compileNode(ternary.trueExpression(), code);
            code.goto_(endLabel);
            code.labelBinding(falseLabel);
            compileNode(ternary.falseExpression(), code);
            code.labelBinding(endLabel);
            return;
        }
        if (node instanceof ElvisNode elvis) {
            var defaultLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(elvis.expression(), code);
            code.dup();
            code.aload(1);
            code.invokestatic(CD_AstCompilerHelpers, "toBoolean", MTD_toBoolean);
            code.ifeq(defaultLabel);
            code.goto_(endLabel);
            code.labelBinding(defaultLabel);
            code.pop();
            compileNode(elvis.defaultExpression(), code);
            code.labelBinding(endLabel);
            return;
        }
        if (node instanceof NullCoalescingNode nc) {
            var defaultLabel = code.newLabel();
            var endLabel = code.newLabel();
            compileNode(nc.expression(), code);
            code.dup();
            code.ifnull(defaultLabel);
            code.goto_(endLabel);
            code.labelBinding(defaultLabel);
            code.pop();
            compileNode(nc.defaultExpression(), code);
            code.labelBinding(endLabel);
            return;
        }

        // Wrappers
        if (node instanceof DeferredExpressionNode def) {
            compileNode(def.node(), code);
            return;
        }
        if (node instanceof DynamicExpressionNode dyn) {
            compileNode(dyn.node(), code);
            return;
        }
        if (node instanceof LambdaNode lambda && lambda.parameters().isEmpty()) {
            compileNode(lambda.expression(), code);
            return;
        }

        throw new UnsupportedOperationException("Unsupported node for bytecode compilation: " + node.getClass().getName());
    }

    private static void compileRelationalCompare(Node left, Node right, CodeBuilder code, int op) {
        compileNode(left, code);
        compileNode(right, code);
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
