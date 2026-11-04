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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class JoelExpressionParser {
    private static final int MAX_CACHE_SIZE = 5000;
    private static volatile ConcurrentHashMap<String, Node> EDEN = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    private static volatile ConcurrentHashMap<String, Node> LONG_TERM = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
    private static final AtomicBoolean PROMOTING = new AtomicBoolean(false);
    private static final ELErrorListener ERROR_LISTENER = new ELErrorListener();

    private JoelExpressionParser() {
    }

    public static Node parse(String expression) {
        ConcurrentHashMap<String, Node> eden = EDEN;
        Node node = eden.get(expression);
        if (node != null) {
            return node;
        }
        ConcurrentHashMap<String, Node> longTerm = LONG_TERM;
        node = longTerm.get(expression);
        if (node != null) {
            eden.put(expression, node);
            return node;
        }
        node = parseInternal(expression);
        if (eden.mappingCount() >= MAX_CACHE_SIZE && PROMOTING.compareAndSet(false, true)) {
            try {
                LONG_TERM = eden;
                EDEN = new ConcurrentHashMap<>(MAX_CACHE_SIZE);
                eden = EDEN;
            } finally {
                PROMOTING.set(false);
            }
        }
        eden.put(expression, node);
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
