package se.inera.certificate.spec.util;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import fitnesse.testsystems.slim.CustomComparator;

public class JSONAssertComparator implements CustomComparator {
    @Override
    public boolean matches(String actual, String expected) {
        try {
            JSONAssert.assertEquals(expected, actual, false);
            return true;
        } catch (JSONException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
