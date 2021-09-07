package io.joel.impl.antlr;

import io.joel.impl.node.AddExpressionNode;
import io.joel.impl.node.AndNode;
import io.joel.impl.node.AssignNode;
import io.joel.impl.node.BooleanNode;
import io.joel.impl.node.CallExpressionNode;
import io.joel.impl.node.ConcatNode;
import io.joel.impl.node.DeferredExpressionNode;
import io.joel.impl.node.DivExpressionNode;
import io.joel.impl.node.DynamicExpressionNode;
import io.joel.impl.node.EqualNode;
import io.joel.impl.node.ExpressionNode;
import io.joel.impl.node.GreaterEqualNode;
import io.joel.impl.node.GreaterThanNode;
import io.joel.impl.node.IdentifierNode;
import io.joel.impl.node.LambdaNode;
import io.joel.impl.node.LessEqualNode;
import io.joel.impl.node.LessThanNode;
import io.joel.impl.node.ListNode;
import io.joel.impl.node.MemberNode;
import io.joel.impl.node.ModExpressionNode;
import io.joel.impl.node.MulExpressionNode;
import io.joel.impl.node.NotEqualNode;
import io.joel.impl.node.NullNode;
import io.joel.impl.node.NumberNode;
import io.joel.impl.node.OrNode;
import io.joel.impl.node.SemicolonNode;
import io.joel.impl.node.SetNode;
import io.joel.impl.node.StringNode;
import io.joel.impl.node.SubExpressionNode;
import io.joel.impl.node.TernaryNode;
import io.joel.impl.node.UnaryEmptyNode;
import io.joel.impl.node.UnaryMinusNode;
import io.joel.impl.node.UnaryNotNode;
import jakarta.el.ELException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.joel.impl.antlr.ExpressionLanguageParser.DeferredExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageParser.DynamicExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageParser.FloatingPointLiteralExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageParser.InfixExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageParser.IntegerLiteralExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageParser.LiteralExprContext;
import static io.joel.impl.antlr.ExpressionLanguageParser.NullLiteralExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageParser.RelationalExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageParser.StringLiteralExpressionContext;

public final class ExpressionVisitor extends ExpressionLanguageParserBaseVisitor<ExpressionNode> {

    public ExpressionVisitor() {
    }

    @Override
    public ExpressionNode visitParenExpression(ExpressionLanguageParser.ParenExpressionContext ctx) {
        return visit(ctx.getChild(1));
    }

    @Override
    public ExpressionNode visitLiteralExpression(ExpressionLanguageParser.LiteralExpressionContext ctx) {
        String text = ctx.getText();
        if (text.startsWith("\\#") || text.startsWith("\\$"))
            return new StringNode(text.substring(1));
        return new StringNode(text);
    }

    @Override
    public ExpressionNode visitCompositeExpression(ExpressionLanguageParser.CompositeExpressionContext ctx) {
        if (ctx.getChildCount() <= 1)
            return visit(ctx.getChild(0));
        if (!ctx.deferredExpression().isEmpty() && !ctx.dynamicExpression().isEmpty())
            throw new ELException("Cannot mix dynamic expressions with deferred expression");
        return ctx.children.stream()
                .map(this::visit)
                .reduce(new StringNode(""), ConcatNode::new);
    }

    @Override
    public ExpressionNode visitLogicalExpression(ExpressionLanguageParser.LogicalExpressionContext ctx) {
        var left = visit(ctx.getChild(0));
        var right = visit(ctx.getChild(2));
        return switch (ctx.bop.getText()) {
            case "&&", "and" -> new AndNode(left, right);
            case "||", "or" -> new OrNode(left, right);
            default -> throw new IllegalStateException("%s %s %s".formatted(left, ctx.bop.getText(), right));
        };
    }

    @Override
    public ExpressionNode visitStringLiteralExpression(StringLiteralExpressionContext ctx) {
        String text = ctx.getText().translateEscapes();
        return new StringNode(text.substring(1, text.length() - 1));
    }

    @Override
    public ExpressionNode visitIntegerLiteralExpression(IntegerLiteralExpressionContext ctx) {
        return new NumberNode(Long.valueOf(ctx.getText()));
    }

    @Override
    public ExpressionNode visitNullLiteralExpression(NullLiteralExpressionContext ctx) {
        return NullNode.INSTANCE;
    }

    @Override
    public ExpressionNode visitLambdaExpression(ExpressionLanguageParser.LambdaExpressionContext ctx) {
        return new LambdaNode(ctx.lambdaParameters().IDENTIFIER().stream().map(Objects::toString).toList(), visit(ctx.expression()));
    }

    @Override
    public ExpressionNode visitFloatingPointLiteralExpression(FloatingPointLiteralExpressionContext ctx) {
        return new NumberNode(Double.valueOf(ctx.getText()));
    }

    @Override
    public ExpressionNode visitUnaryExpression(ExpressionLanguageParser.UnaryExpressionContext ctx) {
        ExpressionNode node = visit(ctx.getChild(1));
        return switch (ctx.prefix.getText()) {
            case "-" -> new UnaryMinusNode(node);
            case "!", "not" -> new UnaryNotNode(node);
            case "empty" -> new UnaryEmptyNode(node);
            default -> throw new IllegalStateException("%s %s".formatted(ctx.prefix.getText(), node));
        };
    }

    @Override
    public ExpressionNode visitRelationalExpression(RelationalExpressionContext ctx) {
        var left = visit(ctx.getChild(0));
        var right = visit(ctx.getChild(2));
        return switch (ctx.bop.getText()) {
            case "==", "eq" -> new EqualNode(left, right);
            case "!=", "ne" -> new NotEqualNode(left, right);
            case ">", "gt" -> new GreaterThanNode(left, right);
            case ">=", "ge" -> new GreaterEqualNode(left, right);
            case "<", "lt" -> new LessThanNode(left, right);
            case "<=", "le" -> new LessEqualNode(left, right);
            default -> throw new IllegalStateException("%s %s %s".formatted(left, ctx.bop.getText(), right));
        };
    }

    @Override
    public ExpressionNode visitSemicolonExpression(ExpressionLanguageParser.SemicolonExpressionContext ctx) {
        return new SemicolonNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)));
    }

    @Override
    public ExpressionNode visitBooleanLiteralExpression(ExpressionLanguageParser.BooleanLiteralExpressionContext ctx) {
        return ctx.getText().equals("true") ? BooleanNode.TRUE : BooleanNode.FALSE;
    }

    @Override
    public ExpressionNode visitAssignExpression(ExpressionLanguageParser.AssignExpressionContext ctx) {
        var left = visit(ctx.getChild(0));
        var right = visit(ctx.getChild(2));
        return switch (ctx.bop.getText()) {
            case "+=" -> new ConcatNode(left, right);
            case "=" -> new AssignNode(left, right);
            default -> throw new IllegalStateException("%s %s %s".formatted(left, ctx.bop.getText(), right));
        };
    }

    @Override
    public ExpressionNode visitLiteralExpr(LiteralExprContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public ExpressionNode visitInfixExpression(InfixExpressionContext ctx) {
        var left = visit(ctx.getChild(0));
        var right = visit(ctx.getChild(2));
        return switch (ctx.bop.getText()) {
            case "+" -> new AddExpressionNode(left, right);
            case "-" -> new SubExpressionNode(left, right);
            case "*" -> new MulExpressionNode(left, right);
            case "/", "div" -> new DivExpressionNode(left, right);
            case "%", "mod" -> new ModExpressionNode(left, right);
            default -> throw new IllegalStateException("%s %s %s".formatted(left, ctx.bop.getText(), right));
        };
    }

    @Override
    public ExpressionNode visitDeferredExpression(DeferredExpressionContext ctx) {
        return new DeferredExpressionNode(visit(ctx.getChild(1)));
    }

    @Override
    public ExpressionNode visitDynamicExpression(DynamicExpressionContext ctx) {
        return new DynamicExpressionNode(visit(ctx.getChild(1)));
    }

    @Override
    public ExpressionNode visitMemberDotExpression(ExpressionLanguageParser.MemberDotExpressionContext ctx) {
        return new MemberNode(visit(ctx.getChild(0)), new IdentifierNode(ctx.getChild(2).getText()));
    }

    @Override
    public ExpressionNode visitIdentifierExpression(ExpressionLanguageParser.IdentifierExpressionContext ctx) {
        return new IdentifierNode(ctx.getText());
    }

    @Override
    public ExpressionNode visitMemberIndexExpression(ExpressionLanguageParser.MemberIndexExpressionContext ctx) {
        return new MemberNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)));
    }

    private List<ExpressionNode> getArguments(ExpressionLanguageParser.ExpressionListContext expressionList) {
        if (expressionList == null)
            return List.of();
        return expressionList
                .expression()
                .stream()
                .map(this::visit)
                .collect(Collectors.toList());
    }

    @Override
    public ExpressionNode visitCallExpression(ExpressionLanguageParser.CallExpressionContext ctx) {
        if (ctx.qualifiedFunction() != null) {
            return visit(ctx.qualifiedFunction());
        }
        var expressionList = ctx.arguments().expressionList();
        return new CallExpressionNode(visit(ctx.getChild(0)), getArguments(expressionList));
    }

    @Override
    public ExpressionNode visitListExpression(ExpressionLanguageParser.ListExpressionContext ctx) {
        var expressionListContext = ctx.expressionList();
        if (expressionListContext == null)
            return new ListNode(Collections.emptyList());
        return new ListNode(expressionListContext
                .expression()
                .stream()
                .map(this::visit)
                .collect(Collectors.toList()));
    }

    @Override
    public ExpressionNode visitSetExpression(ExpressionLanguageParser.SetExpressionContext ctx) {
        var expressionListContext = ctx.expressionList();
        if (expressionListContext == null)
            return new SetNode(Collections.emptyList());
        return new SetNode(ctx.expressionList()
                .expression()
                .stream()
                .map(this::visit)
                .collect(Collectors.toList()));
    }

    @Override
    public ExpressionNode visitQualifiedFunction(ExpressionLanguageParser.QualifiedFunctionContext ctx) {
        var expressionList = ctx.arguments().expressionList();
        return new CallExpressionNode(new IdentifierNode(ctx.getChild(0).getText()), getArguments(expressionList));
    }

    @Override
    public ExpressionNode visitTernaryExpression(ExpressionLanguageParser.TernaryExpressionContext ctx) {
        return new TernaryNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)), visit(ctx.getChild(4)));
    }

    @Override
    public ExpressionNode visitBadTernaryExpression(ExpressionLanguageParser.BadTernaryExpressionContext ctx) {
        throw new ELException();
    }
}
