package se.inera.intyg.webcert.web.service.dto;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class LakareTest {

    @Test
    public void testMerge() {
        Lakare lakare1 = new Lakare("1", "1");
        Lakare lakare2 = new Lakare("2", "2");
        Lakare lakare3 = new Lakare("3", "3");
        Lakare lakare4 = new Lakare("4", "4");
        List<Lakare> a = Arrays.asList(lakare1, lakare2, lakare4);
        List<Lakare> b = Arrays.asList(lakare2, lakare3, lakare4);
        List<Lakare> merged = Lakare.merge(a, b);
        assertNotNull(merged);
        assertFalse(merged.isEmpty());
        assertEquals(Arrays.asList(lakare1, lakare2, lakare3, lakare4), merged);
    }
}
