package dev.thihup.joel.impl.calculator;

import dev.thihup.joel.impl.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;

enum ConversionType {
    ZERO,
    BIG_DECIMAL,
    BIG_INTEGER,
    DOUBLE,
    LONG;


    static ConversionType of(Object leftValue, Object rightValue) {

        record Values(Object a, Object b) {}


        return switch (new Values(leftValue, rightValue)) {
            case Values(var a, var b) when a ==null || b == null -> ConversionType.ZERO;
            case Values(BigDecimal __, var ___) foo -> ConversionType.BIG_DECIMAL;
            case Values(var __, BigDecimal ___) foo -> ConversionType.BIG_DECIMAL;

            case Values(String asString, BigInteger __) when Utils.isFloatingPointNumber(asString) -> ConversionType.BIG_DECIMAL;
            case Values(String asString, var __) when Utils.isFloatingPointNumber(asString) -> ConversionType.DOUBLE;

            case Values(BigInteger __, String asString) when Utils.isFloatingPointNumber(asString) -> ConversionType.BIG_DECIMAL;
            case Values(var __, String asString) when Utils.isFloatingPointNumber(asString) -> ConversionType.DOUBLE;

            case Values(Float __, BigInteger ___) -> ConversionType.BIG_DECIMAL;
            case Values(Double __, BigInteger ___) -> ConversionType.BIG_DECIMAL;

            case Values(Float __, var ___) -> ConversionType.DOUBLE;
            case Values(Double __, var ___) -> ConversionType.DOUBLE;

            case Values(BigInteger ___, Float __) -> ConversionType.BIG_DECIMAL;
            case Values(BigInteger ___, Double __) -> ConversionType.BIG_DECIMAL;

            case Values(var ___, Float __) -> ConversionType.DOUBLE;
            case Values(var ___, Double __) -> ConversionType.DOUBLE;

            case Values(BigInteger __, var ___) -> ConversionType.BIG_INTEGER;
            case Values(var ___, BigInteger __) -> ConversionType.BIG_INTEGER;

            case default -> LONG;
        };

//        if (leftValue == null && rightValue == null)
//            return ConversionType.ZERO;
//        if (leftValue instanceof BigDecimal || rightValue instanceof BigDecimal) {
//            return ConversionType.BIG_DECIMAL;
//        }
//
//        if (leftValue instanceof String asString && Utils.isFloatingPointNumber(asString)) {
//            if (rightValue instanceof BigInteger) {
//                return ConversionType.BIG_DECIMAL;
//            }
//            return ConversionType.DOUBLE;
//        }
//        if (rightValue instanceof String asString && Utils.isFloatingPointNumber(asString)) {
//            if (leftValue instanceof BigInteger) {
//                return ConversionType.BIG_DECIMAL;
//            }
//            return ConversionType.DOUBLE;
//        }
//        if (leftValue instanceof Float || leftValue instanceof Double || rightValue instanceof Float || rightValue instanceof Double) {
//            if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
//                return ConversionType.BIG_DECIMAL;
//            }
//            return ConversionType.DOUBLE;
//        }
//
//        if (leftValue instanceof BigInteger || rightValue instanceof BigInteger) {
//            return ConversionType.BIG_INTEGER;
//        }
//
//        return ConversionType.LONG;
    }
}
