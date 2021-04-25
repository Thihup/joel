package io.joel.impl.antlr;

import io.joel.impl.node.ExpressionNode;
import jakarta.el.ELException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.HashMap;
import java.util.Map;

public class JoelExpressionParser {
    private static final Map<String, ExpressionNode> CACHED_EXPRESSION = new HashMap<>();
    private static final ELErrorListener ERROR_LISTENER = new ELErrorListener();

    private JoelExpressionParser() {
    }

    public static ExpressionNode parse(String expression) {
        return CACHED_EXPRESSION.computeIfAbsent(expression, key -> {
            var lexer = new ExpressionLanguageGrammarLexer(CharStreams.fromString(expression));
            var parser = new ExpressionLanguageGrammarParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(ERROR_LISTENER);
            var prog = parser.prog();
            ExpressionNode visit = new ExpressionVisitor().visit(prog);
            if (visit instanceof ExpressionNode.DynamicExpressionNode dynamicExpression) {
                return dynamicExpression.node();
            }
            if (visit instanceof ExpressionNode.DeferredExpressionNode deferredExpression) {
                return deferredExpression.node();
            }
            return visit;
        });
    }

    private static class ELErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new ELException(String.format("line %d:%d %s", line, charPositionInLine, msg));
        }
    }

}
