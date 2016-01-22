package se.inera.intyg.webcert.web.service.texts.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class IntygTextsTest {

    @Test
    public void testValidVersionNumber() {
        IntygTexts test1 = new IntygTexts("1.0", null, null, null, null, null);
        assertNotNull(test1);
        assertEquals("Version should be set to what is created", test1.getVersion(), "1.0");

        IntygTexts test2 = new IntygTexts("0", null, null, null, null, null);
        assertNotNull(test2);
        assertEquals("Version should be set to what is created", test2.getVersion(), "0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersionNumber() {
        new IntygTexts("1.x", null, null, null, null, null);
        fail();
    }
}
