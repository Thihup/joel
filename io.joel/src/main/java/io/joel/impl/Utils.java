package io.joel.impl;

import io.joel.impl.node.*;

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
