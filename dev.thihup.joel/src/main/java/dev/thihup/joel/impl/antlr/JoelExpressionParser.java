package dev.thihup.joel.impl.antlr;

import dev.thihup.joel.impl.node.DeferredExpressionNode;
import dev.thihup.joel.impl.node.DynamicExpressionNode;
import dev.thihup.joel.impl.node.Node;
import jakarta.el.ELException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JoelExpressionParser {
    private static final int MAX_CACHE_SIZE = 5000;
    private static final Map<String, Node> EDEN = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    private static final Map<String, Node> LONG_TERM = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    private static final ELErrorListener ERROR_LISTENER = new ELErrorListener();

    private JoelExpressionParser() {
    }

    public static Node parse(String expression) {
        Node node = EDEN.get(expression);
        if (node != null) {
            return node;
        }
        node = LONG_TERM.get(expression);
        if (node != null) {
            EDEN.put(expression, node);
            return node;
        }
        node = parseInternal(expression);
        if (EDEN.size() >= MAX_CACHE_SIZE) {
            synchronized (JoelExpressionParser.class) {
                if (EDEN.size() >= MAX_CACHE_SIZE) {
                    LONG_TERM.clear();
                    LONG_TERM.putAll(EDEN);
                    EDEN.clear();
                }
            }
        }
        EDEN.put(expression, node);
        return node;
    }

    private static Node parseInternal(String expression) {
        var lexer = new ExpressionLanguageLexer(CharStreams.fromString(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(ERROR_LISTENER);
        var parser = new ExpressionLanguageParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(ERROR_LISTENER);
        var prog = parser.prog();
        Node visit = new ExpressionVisitor().visit(prog);
        if (visit instanceof DynamicExpressionNode dynamicExpression) {
            return dynamicExpression.node();
        }
        if (visit instanceof DeferredExpressionNode deferredExpression) {
            return deferredExpression.node();
        }
        return visit;
    }

    private static class ELErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new ELException("line %d:%d %s".formatted(line, charPositionInLine, msg));
        }
    }

}
