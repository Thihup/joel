package dev.thihup.joel.impl.node;

import dev.thihup.joel.impl.Utils;
import jakarta.el.ELContext;
import jakarta.el.ELException;

import java.math.BigDecimal;
import java.math.BigInteger;

public record UnaryMinusNode(Node node) implements Node {
    @Override
    public Class<?> getType(ELContext context) {
        return node.getType(context);
    }

    @Override
    public Object getValue(ELContext context) {
        Object value = node.getValue(context);
        if (value == null)
            return 0L;
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.negate();
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.negate();
        }
        if (value instanceof String asString) {
            if (Utils.isFloatingPointNumber(asString)) {
                return -(Double) context.convertToType(asString, Double.class);
            }
            return -(Long) context.convertToType(asString, Long.class);
        }
        if (value instanceof Long asLong) {
            return -asLong;
        }
        if (value instanceof Integer asInt) {
            return -asInt;
        }
        if (value instanceof Double asDouble) {
            return -asDouble;
        }
        if (value instanceof Float asFloat) {
            return -asFloat;
        }
        if (value instanceof Short asShort) {
            return (short) -asShort;
        }
        if (value instanceof Byte asByte) {
            return (byte) -asByte;
        }
        throw new ELException("Cannot apply the '-' operator to %s".formatted(value));
    }

    @Override
    public String prettyPrint() {
        return "-%s".formatted(node.prettyPrint());
    }
}
