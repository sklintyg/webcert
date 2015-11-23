package se.inera.intyg.webcert.web.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class ReflectionUtils {

    // Util class should not be instantiated
    private ReflectionUtils() {
    }

    public static void setStaticAttribute(Class<?> clazz, String attribute, Object newValue) throws Exception {
        Field field = clazz.getDeclaredField(attribute);
        field.setAccessible(true);
        field.set(null, newValue);
    }

    public static void setStaticFinalAttribute(Class<?> clazz, String attribute, Object newValue) throws Exception {
        Field field = clazz.getDeclaredField(attribute);
        field.setAccessible(true);
        removeFinalModifier(field);
        field.set(null, newValue);
    }

    private static void removeFinalModifier(Field field) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    public static void setTypedField(Object object, Object value) {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.getType().isAssignableFrom(value.getClass())) {
                try {
                    boolean wasPrivate = !field.isAccessible();
                    if (wasPrivate) {
                        field.setAccessible(true);
                    }
                    field.set(object, value);
                    if (wasPrivate) {
                        field.setAccessible(false);
                    }
                    return;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new IllegalArgumentException("Found no fileds for setting " + value + " on " + object);
    }
}
