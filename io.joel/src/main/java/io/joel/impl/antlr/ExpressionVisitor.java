package io.joel.impl.antlr;

import io.joel.impl.node.ExpressionNode;
import io.joel.impl.node.ExpressionNode.BooleanNode;
import io.joel.impl.node.ExpressionNode.IdentifierNode;
import io.joel.impl.node.ExpressionNode.MemberNode;
import io.joel.impl.node.InfixExpressionNode.AssignNode;
import io.joel.impl.node.InfixExpressionNode.ConcatNode;
import io.joel.impl.node.RelationalNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.DeferredExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.DynamicExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.FloatingPointLiteralExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.InfixExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.IntegerLiteralExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.LiteralExprContext;
import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.NullLiteralExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.RelationalExpressionContext;
import static io.joel.impl.antlr.ExpressionLanguageGrammarParser.StringLiteralExpressionContext;
import static io.joel.impl.node.ExpressionNode.CallExpressionNode;
import static io.joel.impl.node.ExpressionNode.DeferredExpressionNode;
import static io.joel.impl.node.ExpressionNode.DynamicExpressionNode;
import static io.joel.impl.node.ExpressionNode.NullNode;
import static io.joel.impl.node.ExpressionNode.NumberNode;
import static io.joel.impl.node.ExpressionNode.SemicolonNode;
import static io.joel.impl.node.ExpressionNode.StringNode;
import static io.joel.impl.node.ExpressionNode.TernaryNode;
import static io.joel.impl.node.ExpressionNode.UnaryEmptyNode;
import static io.joel.impl.node.ExpressionNode.UnaryMinusNode;
import static io.joel.impl.node.ExpressionNode.UnaryNotNode;
import static io.joel.impl.node.InfixExpressionNode.AddExpressionNode;
import static io.joel.impl.node.InfixExpressionNode.DivExpressionNode;
import static io.joel.impl.node.InfixExpressionNode.ModExpressionNode;
import static io.joel.impl.node.InfixExpressionNode.MulExpressionNode;
import static io.joel.impl.node.InfixExpressionNode.SubExpressionNode;
import static io.joel.impl.node.RelationalNode.EqualNode;
import static io.joel.impl.node.RelationalNode.GreaterEqualNode;
import static io.joel.impl.node.RelationalNode.GreaterThanNode;
import static io.joel.impl.node.RelationalNode.LessEqualNode;
import static io.joel.impl.node.RelationalNode.LessThanNode;
import static io.joel.impl.node.RelationalNode.NotEqualNode;

public class ExpressionVisitor extends ExpressionLanguageGrammarBaseVisitor<ExpressionNode> {

    public ExpressionVisitor() {
    }

    @Override
    public ExpressionNode visitParenExpression(ExpressionLanguageGrammarParser.ParenExpressionContext ctx) {
        return visit(ctx.getChild(1));
    }

    @Override
    public ExpressionNode visitLogicalExpression(ExpressionLanguageGrammarParser.LogicalExpressionContext ctx) {
        var left = visit(ctx.getChild(0));
        var right = visit(ctx.getChild(2));
        return switch (ctx.bop.getText()) {
            case "&&", "and" -> new RelationalNode.AndNode(left, right);
            case "||", "or" -> new RelationalNode.OrNode(left, right);
            default -> throw new IllegalStateException("%s %s %s".formatted(left, ctx.bop.getText(), right));
        };
    }

    @Override
    public ExpressionNode visitStringLiteralExpression(StringLiteralExpressionContext ctx) {
        return new StringNode(ctx.getText().substring(1, ctx.getText().length() - 1));
    }

    @Override
    public ExpressionNode visitIntegerLiteralExpression(IntegerLiteralExpressionContext ctx) {
        try {
            return new NumberNode(Long.valueOf(ctx.getText()));
        } catch (NumberFormatException ignored) {
            return new NumberNode(new BigInteger(ctx.getText()));
        }
    }

    @Override
    public ExpressionNode visitNullLiteralExpression(NullLiteralExpressionContext ctx) {
        return NullNode.INSTANCE;
    }

    @Override
    public ExpressionNode visitFloatingPointLiteralExpression(FloatingPointLiteralExpressionContext ctx) {
        try {
            return new NumberNode(Double.valueOf(ctx.getText()));
        } catch (NumberFormatException ignored) {
            return new NumberNode(new BigDecimal(ctx.getText()));
        }
    }

    @Override
    public ExpressionNode visitUnaryExpression(ExpressionLanguageGrammarParser.UnaryExpressionContext ctx) {
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
    public ExpressionNode visitSemicolonExpression(ExpressionLanguageGrammarParser.SemicolonExpressionContext ctx) {
        return new SemicolonNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)));
    }

    @Override
    public ExpressionNode visitBooleanLiteralExpression(ExpressionLanguageGrammarParser.BooleanLiteralExpressionContext ctx) {
        return ctx.getText().equals("true") ? BooleanNode.TRUE : BooleanNode.FALSE;
    }

    @Override
    public ExpressionNode visitAssignExpression(ExpressionLanguageGrammarParser.AssignExpressionContext ctx) {
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
    public ExpressionNode visitMemberDotExpression(ExpressionLanguageGrammarParser.MemberDotExpressionContext ctx) {
        return new MemberNode(visit(ctx.getChild(0)), new IdentifierNode(ctx.getChild(2).getText()));
    }

    @Override
    public ExpressionNode visitIdentifierExpression(ExpressionLanguageGrammarParser.IdentifierExpressionContext ctx) {
        return new IdentifierNode(ctx.getText());
    }

    @Override
    public ExpressionNode visitMemberIndexExpression(ExpressionLanguageGrammarParser.MemberIndexExpressionContext ctx) {
        return new MemberNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)));
    }

    @Override
    public ExpressionNode visitCallExpression(ExpressionLanguageGrammarParser.CallExpressionContext ctx) {
        var firstChild = ctx.getChild(0).getText();
        if (":".equals(ctx.getChild(1).getText())) {
            return new CallExpressionNode(new IdentifierNode(firstChild + ":" + ctx.getChild(2).getText()), List.of());
        }
        return null;
    }

    @Override
    public ExpressionNode visitListExpression(ExpressionLanguageGrammarParser.ListExpressionContext ctx) {
        var expressionListContext = ctx.expressionList();
        if (expressionListContext == null)
            return new ExpressionNode.ListNode(Collections.emptyList());
        return new ExpressionNode.ListNode(expressionListContext
                .expression()
                .stream()
                .map(this::visit)
                .collect(Collectors.toList()));
    }

    @Override
    public ExpressionNode visitSetExpression(ExpressionLanguageGrammarParser.SetExpressionContext ctx) {
        var expressionListContext = ctx.expressionList();
        if (expressionListContext == null)
            return new ExpressionNode.SetNode(Collections.emptyList());
        return new ExpressionNode.SetNode(ctx.expressionList()
                .expression()
                .stream()
                .map(this::visit)
                .collect(Collectors.toList()));
    }

    @Override
    public ExpressionNode visitTernaryExpression(ExpressionLanguageGrammarParser.TernaryExpressionContext ctx) {
        return new TernaryNode(visit(ctx.getChild(0)), visit(ctx.getChild(2)), visit(ctx.getChild(4)));
    }
}
