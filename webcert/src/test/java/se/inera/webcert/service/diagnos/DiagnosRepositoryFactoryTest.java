package se.inera.webcert.service.diagnos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosRepositoryFactoryTest {

    private static final String LINE_1 = "A010   Tyfoidfeber";
    private static final String LINE_1_KOD = "A010";
    private static final String LINE_1_BESK = "Tyfoidfeber";
    private static final String LINE_2 = "A083W  Enterit orsakad av annat specificerat virus";
    private static final String LINE_2_KOD = "A083W";
    private static final String LINE_2_BESK = "Enterit orsakad av annat specificerat virus";

    private static final String REALLY_MESSY_LINE = "  A050   Matförgiftning orsakad av stafylokocker  ";

    private static final String FILE_1 = "/DiagnosService/KSH97_TESTKODER_1.ANS";
    private static final String FILE_2 = "/DiagnosService/KSH97_TESTKODER_2.ANS";
    
    private DiagnosRepositoryFactory factory = new DiagnosRepositoryFactory();

    @Before
    public void setup() {
        factory = new DiagnosRepositoryFactory();
        List<String> diagnosKodFiler = Arrays.asList(FILE_1, FILE_2);
        factory.setDiagnosKodFiler(diagnosKodFiler);
    }

    @Test
    public void testCreateRepository() {
                
        DiagnosRepository repository = factory.createDiagnosRepository();
        assertNotNull(repository);
        assertEquals(150, repository.nbrOfDiagosis());
    }

    @Test
    public void testReadDiagnosFile() throws Exception {

        DiagnosRepository diagnosRepository = new DiagnosRepository();
        factory.readFile(FILE_1, diagnosRepository);
        assertEquals(100, diagnosRepository.nbrOfDiagosis());
    }

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

    }

    @Test
    public void testWithNullsAndEmpty() {

        Diagnos res = factory.createDiagnosFromString(null);
        assertNull(res);

        res = factory.createDiagnosFromString("");
        assertNull(res);

        res = factory.createDiagnosFromString("  ");
        assertNull(res);
    }

    @Test
    public void testWithMessyString() {

        Diagnos res = factory.createDiagnosFromString(REALLY_MESSY_LINE);
        assertNotNull(res);
        assertEquals("A050", res.getKod());
        assertEquals("Matförgiftning orsakad av stafylokocker", res.getBeskrivning());

    }

}
