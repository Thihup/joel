package dev.thihup.joel.impl;

import jakarta.el.ELException;

import java.beans.PropertyEditorManager;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class TypeConverter {
    private static final String CANNOT_CONVERT_TO = "Cannot convert value %s to type %s";

    private TypeConverter() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T coerce(Object object, Class<T> targetType) {
        try {
            return (T) switch(targetType) {
                case null -> null;
                case Class<?> type when object == null -> null;
                case Class<?> type when type.isPrimitive() -> coercePrimitive(object, type);
                case Class<?> type -> coerceImplementation(object, type);
            };
        } catch (Throwable throwable) {
            return sneakyThrow(throwable);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable, T> T sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    private static Object coerceImplementation(Object object, Class<?> targetType) {
        return switch (targetType) {
            case Class<?> foo when object == null -> null;
            case Class<?> foo when foo == Object.class  -> coerceToObject(object, Object.class);
            case Class<?> foo when foo == String.class  -> coerceToString(object);
            case Class<?> foo when foo == Boolean.class  -> coerceToBoolean(object);
            case Class<?> foo when foo == Enum.class  -> coerceToEnum(object, targetType);
            case Class<?> foo when foo == BigDecimal.class  -> coerceToBigDecimal(object);
            case Class<?> foo when foo == BigInteger.class  -> coerceToBigInteger(object);
            case Class<?> foo when foo == Void.class  -> throw new ELException("Cannot convert " + object + " to java.lang.Void");
            case Class<?> foo when foo == Character.class  -> coerceToCharacter(object);
            case Class<?> foo when foo == Long.class  -> coerceToLong(object);
            case Class<?> foo when foo == Integer.class  -> coerceToInteger(object);
            case Class<?> foo when foo == Short.class  -> coerceToShort(object);
            case Class<?> foo when foo == Byte.class  -> coerceToByte(object);
            case Class<?> foo when foo == Double.class  -> coerceToDouble(object);
            case Class<?> foo when foo == Float.class  -> coerceToFloat(object);
            default -> coerceToObject(object, targetType);
        };
    }

    private static Object coercePrimitive(Object object, Class<?> targetType) throws Throwable {
        return switch (targetType) {
            case Class<?> type when type == Boolean.TYPE -> coerceToBoolean(object);
            case Class<?> type when type == Character.TYPE -> coerceToCharacter(object);
            case Class<?> type when type == Long.TYPE -> coerceToLong(object);
            case Class<?> type when type == Integer.TYPE -> coerceToInteger(object);
            case Class<?> type when type == Short.TYPE -> coerceToShort(object);
            case Class<?> type when type == Byte.TYPE -> coerceToByte(object);
            case Class<?> type when type == Double.TYPE -> coerceToDouble(object);
            case Class<?> type when type == Float.TYPE -> coerceToFloat(object);
            case Class<?> type when type == Void.TYPE -> throw new ELException("Cannot convert " + object + " to void");
            case null -> MethodHandles.zero(targetType).invokeExact();
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
        return switch (value) {
            case String newValue -> newValue;
            case Enum<?> newValue -> newValue.name();
            case null -> "";
            default -> value.toString();
        };
    }

    private static Object convertToNumber(Object value) {
        return switch (value) {
            case String newValue when newValue.isEmpty() -> 0L;
            case Character newValue -> Short.valueOf((short) newValue.charValue());
            case Boolean newValue -> throw new ELException("Cannot convert value %s from Boolean to Number".formatted(newValue));
            case default -> value;
        };
    }

    private static Long coerceToLong(Object value) {
        value = convertToNumber(value);
        return switch (value) {
            case Long newValue -> newValue;
            case Number newValue -> newValue.longValue();
            case String newValue -> Long.valueOf(newValue);
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Long"));
        };
    }

    private static BigDecimal coerceToBigDecimal(Object value) {
        value = convertToNumber(value);
        return switch (value) {
            case BigDecimal newValue -> newValue;
            case BigInteger newValue -> new BigDecimal(newValue);
            case Number newValue -> new BigDecimal(newValue.doubleValue());
            case String newValue -> new BigDecimal(newValue);
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "BigDecimal"));
        };
    }

    private static BigInteger coerceToBigInteger(Object value) {
        value = convertToNumber(value);
        return switch (value) {
            case BigInteger newValue -> newValue;
            case BigDecimal newValue -> newValue.toBigInteger();
            case Number newValue -> BigInteger.valueOf(newValue.longValue());
            case String newValue -> new BigInteger(newValue);
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "BigInteger"));
        };
    }

    private static Byte coerceToByte(Object value) {
        value = convertToNumber(value);
        return switch (value) {
            case Byte newValue -> newValue;
            case Number newValue -> newValue.byteValue();
            case String newValue -> Byte.valueOf(newValue);
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Byte"));
        };
    }

    private static Short coerceToShort(Object value) {
        value = convertToNumber(value);
        return switch (value) {
            case Short newValue -> newValue;
            case Number newValue -> newValue.shortValue();
            case String newValue -> Short.valueOf(newValue);
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Short"));
        };
    }

    private static Integer coerceToInteger(Object value) {
        value = convertToNumber(value);
        return switch (value) {
            case Integer newValue -> newValue;
            case Number newValue -> newValue.intValue();
            case String newValue -> Integer.valueOf(newValue);
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Integer"));
        };
    }

    private static Float coerceToFloat(Object value) {
        value = convertToNumber(value);
        return switch (value) {
            case Float newValue -> newValue;
            case Number newValue -> newValue.floatValue();
            case String newValue -> Float.valueOf(newValue);
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Float"));
        };
    }

    private static Double coerceToDouble(Object value) {
        value = convertToNumber(value);
        return switch (value) {
            case Double newValue -> newValue;
            case Number newValue -> newValue.doubleValue();
            case String newValue -> Double.valueOf(newValue);
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Double"));
        };
    }

    private static Boolean coerceToBoolean(Object value) {
        return switch (value) {
            case Boolean newValue -> newValue;
            case String newValue when !newValue.isEmpty() -> Boolean.parseBoolean(newValue);
            case String newValue when newValue.isEmpty() -> false;
            default -> throw new ELException(CANNOT_CONVERT_TO.formatted(value, "Boolean"));
        };
    }

    @SuppressWarnings("unchecked")
    private static Enum<?> coerceToEnum(Object value, Class<?> targetType) {
        return switch (value) {
            case Object obj when targetType.isInstance(value) -> (Enum<?>) targetType.cast(obj);
            case String newValue when newValue.isEmpty() -> null;
            case default -> Enum.valueOf(targetType.asSubclass(Enum.class), coerceToString(value));
            case null -> null;
        };
    }

}
