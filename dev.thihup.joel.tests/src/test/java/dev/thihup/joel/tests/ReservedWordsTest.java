package dev.thihup.joel.tests;

import jakarta.el.ELException;
import jakarta.el.ELProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validates Section 1.17 (Reserved Words) of the Jakarta Expression Language Specification.
 * The following words are reserved for the language and must not be used as identifiers:
 * and, eq, gt, true, instanceof, or, ne, le, false, empty, not, lt, ge, null, div, mod
 */
class ReservedWordsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "and", "eq", "gt", "true", "instanceof",
            "or", "ne", "le", "false", "empty",
            "not", "lt", "ge", "null", "div", "mod"
    })
    void reservedWordsCannotBeUsedAsIdentifiers(String reservedWord) {
        ELProcessor elProcessor = new ELProcessor();
        // None of the reserved words can be used as an identifier in an assignment
        assertThrows(ELException.class, () -> elProcessor.eval(reservedWord + " = 1"));
    }

    @Test
    void instanceofIsNotAnOperator() {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("nums", List.of(1, 2, 3));
        // 'instanceof' is a reserved word and not a binary operator in Jakarta EL
        assertThrows(ELException.class, () -> elProcessor.eval("nums instanceof java.util.List"));
    }
}

