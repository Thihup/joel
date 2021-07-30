package io.joel.impl.calculator;

import io.joel.impl.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;

enum ConversionType {
    ZERO,
    BIG_DECIMAL,
    BIG_INTEGER,
    DOUBLE,
    LONG;

    static ConversionType of(Object leftValue, Object rightValue) {
        if (leftValue == null && rightValue == null)
            return ConversionType.ZERO;
        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
            return ConversionType.BIG_DECIMAL;
        }

        if (leftValue instanceof String asString && Utils.isFloatingPointNumber(asString)) {
            if (rightValue instanceof BigInteger) {
                return ConversionType.BIG_DECIMAL;
            }
            return ConversionType.DOUBLE;
        }
        if (rightValue instanceof String asString && Utils.isFloatingPointNumber(asString)) {
            if (leftValue instanceof BigInteger) {
                return ConversionType.BIG_DECIMAL;
            }
            return ConversionType.DOUBLE;
        }
        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
                return ConversionType.BIG_DECIMAL;
            }
            return ConversionType.DOUBLE;
        }

        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
            return ConversionType.BIG_INTEGER;
        }

        return ConversionType.LONG;
    }
}
