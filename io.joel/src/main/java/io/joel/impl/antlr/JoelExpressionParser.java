package io.joel.impl.antlr;

import io.joel.impl.node.ExpressionNode;
import jakarta.el.ELException;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class JoelExpressionParser {

    public static ExpressionNode parse(String expression) {
        var lexer = new ExpressionLanguageGrammarLexer(CharStreams.fromString(expression));
        var parser = new ExpressionLanguageGrammarParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new ELException(String.format("line %d:%d %s", line, charPositionInLine, msg));
            }
        });
        var prog = parser.prog();
        return new ExpressionVisitor().visit(prog);
    }
}
