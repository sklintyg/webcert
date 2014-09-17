package se.inera.webcert.service.diagnos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosRepositoryFactoryTest {

    private static final String LINE_1 = "010   Tyfoidfeber";
    private static final String LINE_1_KOD = "010";
    private static final String LINE_1_BESK = "Tyfoidfeber";
    private static final String LINE_2 = "A083W  Enterit orsakad av annat specificerat virus";
    private static final String LINE_2_KOD = "A083W";
    private static final String LINE_2_BESK = "Enterit orsakad av annat specificerat virus";
    
    private DiagnosRepositoryFactory factory = new DiagnosRepositoryFactory();
    
    @Test
    public void testCreateDiagnosFromString() {
        
        Diagnos res = factory.createDiagnosFromString(LINE_1);
        
        assertNotNull(res);
        assertEquals(LINE_1_KOD, res.getKod());
        assertEquals(LINE_1_BESK, res.getBeskrivning());
        
        res = factory.createDiagnosFromString(LINE_2);
        
        assertNotNull(res);
        assertEquals(LINE_2_KOD, res.getKod());
        assertEquals(LINE_2_BESK, res.getBeskrivning());
        
        res = factory.createDiagnosFromString(null);
        assertNull(res);
        
        res = factory.createDiagnosFromString("  ");
        assertNull(res);
        
        res = factory.createDiagnosFromString("  thrashyString   ");
        assertNull(res);
    }
    
    
}
