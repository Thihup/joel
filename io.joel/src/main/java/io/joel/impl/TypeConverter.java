package io.joel.impl;

import jakarta.el.ELException;

import java.beans.PropertyEditorManager;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class TypeConverter {
    private static final String CANNOT_CONVERT_TO = "Cannot convert value %s to type %s";

    private TypeConverter() {
    }

    public static Object coerce(Object object, Class<?> targetType) {
        if (targetType == null)
            return object;
        if (!targetType.isPrimitive()) {
            return coerceImplementation(object, targetType);
        }
        try {
            return coercePrimitive(object, targetType);
        } catch (Throwable throwable) {
            return sneakyThrow(throwable);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, T> T sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    private static Object coerceImplementation(Object object, Class<?> targetType) {
        if (object == null)
            return null;
        return switch (targetType.getSimpleName()) {
            case "String" -> coerceToString(object);
            case "Boolean" -> coerceToBoolean(object);
            case "Enum" -> coerceToEnum(object, targetType);
            case "BigDecimal" -> coerceToBigDecimal(object);
            case "BigInteger" -> coerceToBigInteger(object);
            case "Void" -> throw new ELException("Cannot convert " + object + " to java.lang.Void");
            case "Character" -> coerceToCharacter(object);
            case "Long" -> coerceToLong(object);
            case "Integer" -> coerceToInteger(object);
            case "Short" -> coerceToShort(object);
            case "Byte" -> coerceToByte(object);
            case "Double" -> coerceToDouble(object);
            case "Float" -> coerceToFloat(object);
            default -> coerceToObject(object, targetType);
        };
    }

    private static Object coercePrimitive(Object object, Class<?> targetType) throws Throwable {
        if (object == null) {
            return MethodHandles.zero(targetType).invoke();
        }
        return switch (targetType.getSimpleName()) {
            case "boolean" -> coerceToBoolean(object);
            case "char" -> coerceToCharacter(object);
            case "long" -> coerceToLong(object);
            case "int" -> coerceToInteger(object);
            case "short" -> coerceToShort(object);
            case "byte" -> coerceToByte(object);
            case "double" -> coerceToDouble(object);
            case "float" -> coerceToFloat(object);
            case "void" -> throw new ELException("Cannot convert " + object + " to void");
            default -> null;
        };
    }

    private static Object coerceToObject(Object object, Class<?> targetType) {
        if (targetType.isInstance(object))
            return targetType.cast(object);
        if (object instanceof String newValue) {
            return coerceStringToObject(newValue, targetType);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(object, targetType));
    }

    private static Object coerceStringToObject(String value, Class<?> targetType) {
        var editor = PropertyEditorManager.findEditor(targetType);
        if (editor == null) {
            if (value.isEmpty()) {
                return null;
            }
            throw new ELException(CANNOT_CONVERT_TO.formatted(value, targetType));
        }
        try {
            editor.setAsText(value);
        } catch (IllegalArgumentException e) {
            if (value.isEmpty())
                return null;
            throw new ELException(CANNOT_CONVERT_TO.formatted(value, targetType));
        }
        return editor.getValue();
    }

    private static Object coerceToCharacter(Object value) {
        if (value instanceof String newValue) {
            if (newValue.isEmpty())
                return (char) 0;
            return newValue.charAt(0);
        }
        if (value instanceof Character newValue)
            return newValue;
        if (value instanceof Boolean) {
            throw new ELException("Cannot convert value %s from Boolean to Character".formatted(value));
        }
        if (value instanceof Number newValue)
            return (char) newValue.shortValue();
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Character"));
    }

    private static String coerceToString(Object value) {
        if (value == null)
            return "";
        if (value instanceof String newValue) {
            return newValue;
        }
        if (value instanceof Enum<?> newValue) {
            return newValue.name();
        }
        return value.toString();
    }

    private static Object convertToNumber(Object value) {
        if (value instanceof String newValue && newValue.isEmpty())
            return 0L;
        Object valueToConvert = (value instanceof Character newValue) ? Short.valueOf((short) newValue.charValue()) : value;
        if (valueToConvert instanceof Boolean) {
            throw new ELException("Cannot convert value %s from Boolean to Number".formatted(value));
        }
        return valueToConvert;
    }

    private static Long coerceToLong(Object value) {
        value = convertToNumber(value);
        if (value instanceof Long newValue) {
            return newValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String newValue) {
            return Long.valueOf(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Long"));
    }

    private static BigDecimal coerceToBigDecimal(Object value) {
        value = convertToNumber(value);
        if (value instanceof BigDecimal newValue) {
            return newValue;
        }
        if (value instanceof BigInteger bigInteger) {
            return new BigDecimal(bigInteger);
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.doubleValue());
        }
        if (value instanceof String newValue) {
            return new BigDecimal(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "BigDecimal"));
    }

    private static BigInteger coerceToBigInteger(Object value) {
        value = convertToNumber(value);
        if (value instanceof BigInteger newValue) {
            return newValue;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.toBigInteger();
        }
        if (value instanceof Number number) {
            return BigInteger.valueOf(number.longValue());
        }
        if (value instanceof String newValue) {
            return new BigInteger(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "BigInteger"));
    }

    private static Byte coerceToByte(Object value) {
        value = convertToNumber(value);
        if (value instanceof Byte newValue) {
            return newValue;
        }
        if (value instanceof Number number) {
            return number.byteValue();
        }
        if (value instanceof String newValue) {
            return Byte.valueOf(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Byte"));
    }

    private static Short coerceToShort(Object value) {
        value = convertToNumber(value);
        if (value instanceof Short newValue) {
            return newValue;
        }
        if (value instanceof Number number) {
            return number.shortValue();
        }
        if (value instanceof String newValue) {
            return Short.valueOf(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Short"));
    }

    private static Integer coerceToInteger(Object value) {
        value = convertToNumber(value);
        if (value instanceof Integer newValue) {
            return newValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String newValue) {
            return Integer.valueOf(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Integer"));
    }

    private static Float coerceToFloat(Object value) {
        value = convertToNumber(value);
        if (value instanceof Float newValue) {
            return newValue;
        }
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String newValue) {
            return Float.valueOf(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Float"));
    }

    private static Double coerceToDouble(Object value) {
        value = convertToNumber(value);
        if (value instanceof Double newValue) {
            return newValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String newValue) {
            return Double.valueOf(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Double"));
    }

    private static Boolean coerceToBoolean(Object value) {
        if (value instanceof Boolean newValue)
            return newValue;
        if (value instanceof String newValue) {
            return !newValue.isEmpty() && Boolean.parseBoolean(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Boolean"));
    }

    private static Enum<?> coerceToEnum(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return (Enum<?>) targetType.cast(value);
        }
        if (value instanceof String newValue && newValue.isEmpty()) {
            return null;
        }
        return Enum.valueOf(targetType.asSubclass(Enum.class), coerceToString(value));
    }

}
