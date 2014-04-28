package se.inera.webcert.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtils {

	public static void setStaticAttribute(Class clazz, String attribute, Object newValue) throws Exception {
		Field field = clazz.getDeclaredField(attribute);
	    field.setAccessible(true);
	    field.set(null, newValue);
	}

	public static void setStaticFinalAttribute(Class clazz, String attribute, Object newValue) throws Exception {
		Field field = clazz.getDeclaredField(attribute);
	    field.setAccessible(true);
	    removeFinalModifier(field);
	    field.set(null, newValue);
	}

	private static void removeFinalModifier(Field field)
			throws NoSuchFieldException, IllegalAccessException {
	    Field modifiersField = Field.class.getDeclaredField("modifiers");
	    modifiersField.setAccessible(true);
	    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
	}

}
