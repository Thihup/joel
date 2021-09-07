package io.joel.impl;

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
import io.joel.impl.node.ObjectNode;
import io.joel.impl.node.OrNode;
import io.joel.impl.node.SemicolonNode;
import io.joel.impl.node.SetNode;
import io.joel.impl.node.StringNode;
import io.joel.impl.node.SubExpressionNode;
import io.joel.impl.node.TernaryNode;
import io.joel.impl.node.UnaryEmptyNode;
import io.joel.impl.node.UnaryMinusNode;
import io.joel.impl.node.UnaryNotNode;

import java.util.stream.Collectors;

public final class Utils {
    private Utils() {
    }

    public static boolean isFloatingPointNumber(String value) {
        return value.indexOf('.') >= 0 || value.indexOf('e') >= 0 || value.indexOf('E') >= 0;
    }

    public static String prettyPrint(ExpressionNode node) {
        return switch (node) {
            case ObjectNode n -> n.value().toString();
            case BooleanNode n -> switch (n) {
                case TRUE -> "true";
                case FALSE -> "false";
            };
            case NullNode ignored -> "null";
            case IdentifierNode n -> n.value();
            case StringNode n -> "'%s'".formatted(n.value());
            case NumberNode n -> n.value().toString();
            case SetNode n -> n.values().stream().map(Utils::prettyPrint).collect(Collectors.joining(",", "{", "}"));
            case ListNode n -> n.values().stream().map(Utils::prettyPrint).collect(Collectors.joining(",", "[", "]"));
            case CallExpressionNode n -> "%s(%s)".formatted(prettyPrint(n.callee()), n.arguments().stream().map(Utils::prettyPrint).collect(Collectors.joining(",")));
            case DeferredExpressionNode n -> "#{%s}".formatted(prettyPrint(n));
            case AddExpressionNode n -> "%s + %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case AssignNode n -> "%s = %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case ConcatNode n -> "%s += %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case DivExpressionNode n -> "%s / %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case ModExpressionNode n -> "%s %% %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case MulExpressionNode n -> "%s * %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case SubExpressionNode n -> "%s - %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case DynamicExpressionNode n -> "${%s}".formatted(prettyPrint(n));
            case LambdaNode n -> "(%s) -> %s".formatted(String.join(",", n.parameters()), prettyPrint(n.expression()));
            case MemberNode n -> "%s.%s".formatted(prettyPrint(n.object()), prettyPrint(n.property()));
            case SemicolonNode n -> "%s ; %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case TernaryNode n -> "%s ? %s : %s".formatted(prettyPrint(n.condition()), prettyPrint(n.trueExpression()), prettyPrint(n.falseExpression()));
            case UnaryEmptyNode n -> "empty %s".formatted(prettyPrint(n.node()));
            case UnaryMinusNode n -> "- %s".formatted(prettyPrint(n.node()));
            case UnaryNotNode n -> "! %s".formatted(prettyPrint(n.node()));
            case AndNode n -> "%s && %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case EqualNode n -> "%s == %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case GreaterEqualNode n -> "%s >= %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case GreaterThanNode n -> "%s > %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case LessEqualNode n -> "%s <= %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case LessThanNode n -> "%s < %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case NotEqualNode n -> "%s != %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
            case OrNode n -> "%s || %s".formatted(prettyPrint(n.left()), prettyPrint(n.right()));
        };
    }
}
