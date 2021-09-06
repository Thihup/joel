package io.joel.impl.antlr;

import io.joel.impl.node.DeferredExpressionNode;
import io.joel.impl.node.DynamicExpressionNode;
import io.joel.impl.node.ExpressionNode;
import jakarta.el.ELException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.HashMap;
import java.util.Map;

public final class JoelExpressionParser {
    private static final Map<String, ExpressionNode> CACHED_EXPRESSION = new HashMap<>();
    private static final ELErrorListener ERROR_LISTENER = new ELErrorListener();

    private JoelExpressionParser() {
    }

    public static ExpressionNode parse(String expression) {
        return CACHED_EXPRESSION.computeIfAbsent(expression, key -> {
            var lexer = new ExpressionLanguageLexer(CharStreams.fromString(expression));
            lexer.removeErrorListeners();
            lexer.addErrorListener(ERROR_LISTENER);
            var parser = new ExpressionLanguageParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(ERROR_LISTENER);
            var prog = parser.prog();
            ExpressionNode visit = new ExpressionVisitor().visit(prog);
            if (visit instanceof DynamicExpressionNode dynamicExpression) {
                return dynamicExpression.node();
            }
            if (visit instanceof DeferredExpressionNode deferredExpression) {
                return deferredExpression.node();
            }
            return visit;
        });
    }

    private static class ELErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new ELException("line %d:%d %s".formatted(line, charPositionInLine, msg));
        }
    }

}
