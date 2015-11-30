package se.inera.certificate.spec.util

import fitnesse.slim.Converter;

public class StringConverter implements Converter<String> {

    private static final String NULL = "<null>";
    private static final String EMPTY = "<empty>";
    private static final String SPACE = "<space>";
    
    @Override
    public String fromString(String inputString) {
        if (inputString == null || inputString.isEmpty() || EMPTY.equals(inputString)) {
            return "";
        }
        if (SPACE.equals(inputString)) {
            return " ";
        }
        if (NULL.equals(inputString)) {
            return null;
        }
        return inputString.trim();
    }

    @Override
    public String toString(String obj) {
        if (obj == null) {
            return NULL;
        }
        if (" ".equals(obj)) {
            return SPACE;
        }
        if ("".equals(obj)) {
            return EMPTY;
        }
        return obj.trim();
    }
}