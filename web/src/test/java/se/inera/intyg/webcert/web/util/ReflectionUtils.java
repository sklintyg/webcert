/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
