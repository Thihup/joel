package io.joel.impl;

import jakarta.el.ELException;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class TypeConverter {
    private static final String CANNOT_CONVERT_TO = "Cannot convert value %s to type %s";
    public static final Map<Class<?>, Class<?>> primitiveToWrapper = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class);
    private static final Map<Class<?>, Class<?>> wrapperToPrimitive = Map.of(
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class);

    private TypeConverter() {
    }

    public static Object coerce(Object object, Class<?> targetType) {
        Class<?> boxedClass = primitiveToWrapper.get(targetType);
        if (boxedClass != null) {
            var boxedValue = coerceImplementation(object, boxedClass, true);
            if (boxedValue == null) {
                throw new ELException(CANNOT_CONVERT_TO.formatted(object, targetType));
            }
            return boxedValue;
        }
        return coerceImplementation(object, targetType, false);
    }

    private static Object coerceImplementation(Object object, Class<?> targetType, boolean isPrimitive) {
        if (targetType == String.class) {
            return coerceToString(object);
        }
        if (targetType == Boolean.class) {
            return coerceToBoolean(object, isPrimitive);
        }
        if (targetType == Enum.class) {
            return coerceToEnum(object, targetType);
        }
        if (targetType == Character.class)
            return coerceToCharacter(object);

        if (object == null)
            return null;

        if (targetType == Long.class)
            return coerceToLong(object);
        if (targetType == Integer.class)
            return coerceToInteger(object);
        if (targetType == BigDecimal.class)
            return coerceToBigDecimal(object);
        if (targetType == BigInteger.class)
            return coerceToBigInteger(object);
        if (targetType == Short.class)
            return coerceToShort(object);
        if (targetType == Byte.class)
            return coerceToByte(object);
        if (targetType == Double.class)
            return coerceToDouble(object);
        if (targetType == Float.class)
            return coerceToFloat(object);

        if (targetType == void.class || targetType == Void.class)
            return null;

        return coerceToObject(object, targetType);
    }

    private static Object coerceToObject(Object object, Class<?> targetType) {
        if (targetType.isInstance(object))
            return targetType.cast(object);
        if (object instanceof String newValue) {
            return coerceStringToObject(newValue, targetType);
        }
        return null;
    }

    private static Object coerceStringToObject(String value, Class<?> targetType) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
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

    private static Boolean coerceToBoolean(Object value, boolean isPrimitive) {
        if (value == null) {
            return isPrimitive ? false : null;
        }
        if (value instanceof Boolean newValue)
            return newValue;
        if (value instanceof String newValue) {
            return newValue.isEmpty() ? false : Boolean.valueOf(newValue);
        }
        throw new ELException(CANNOT_CONVERT_TO.formatted(value, isPrimitive ? "boolean" : "Boolean"));
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
