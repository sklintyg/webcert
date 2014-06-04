package se.inera.certificate.mc2wc.converter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FragaSvarUtilsTest {

    @Test(expected=RuntimeException.class)
    public void testWithBlankString() {
        String in = "";
        FragaSvarUtils.detectIfSamordningsNummer(in);
    }
    
    @Test(expected=RuntimeException.class)
    public void testWithTooShortInput() {
        String in = "197001";
        FragaSvarUtils.detectIfSamordningsNummer(in);
    }
    
    @Test(expected=RuntimeException.class)
    public void testWithGarbageInput() {
        String in = "BlaHongaBla";
        FragaSvarUtils.detectIfSamordningsNummer(in);
    }
    
    @Test
    public void testWithPersonnummerHigh() {
        String in = "19701231-1234";
        assertFalse(FragaSvarUtils.detectIfSamordningsNummer(in));
    }
    
    @Test
    public void testWithPersonnummerLow() {
        String in = "19700101-1234";
        assertFalse(FragaSvarUtils.detectIfSamordningsNummer(in));
    }
    
    @Test
    public void testWithSamordningsnummerHigh() {
        String in = "19701291-1234";
        assertTrue(FragaSvarUtils.detectIfSamordningsNummer(in));
    }
    
    @Test
    public void testWithSamordningsnummerLow() {
        String in = "19701261-1234";
        assertTrue(FragaSvarUtils.detectIfSamordningsNummer(in));
    }
}   


