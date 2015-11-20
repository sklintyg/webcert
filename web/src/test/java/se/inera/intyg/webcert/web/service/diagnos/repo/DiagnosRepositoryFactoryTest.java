package se.inera.intyg.webcert.web.service.diagnos.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/DiagnosService/DiagnosRepositoryFactoryTest-context.xml")
public class DiagnosRepositoryFactoryTest {

    private static final String LINE_1 = "A00-   Tyfoidfeber";
    private static final String LINE_1_KOD = "A00-";
    private static final String LINE_1_BESK = "Tyfoidfeber";
    private static final String LINE_2 = "A083W  Enterit orsakad av annat specificerat virus";
    private static final String LINE_2_KOD = "A083W";
    private static final String LINE_2_BESK = "Enterit orsakad av annat specificerat virus";

    private static final String REALLY_MESSY_LINE = "  A050   Matförgiftning orsakad av stafylokocker  ";

    private static final String FILE_1 = "classpath:/DiagnosService/KSH97_TESTKODER_1.ANS";
    private static final String FILE_2 = "classpath:/DiagnosService/KSH97_TESTKODER_2.ANS";
    private static final String FILE_3 = "classpath:/DiagnosService/KSH97P_SFAM_TESTKODER.ANS";

    @Autowired
    private DiagnosRepositoryFactory factory;

    @Before
    public void setup() {

    }

    @Test
    public void testCreateRepository() {
        List<String> fileList = Arrays.asList(FILE_1, FILE_2);
        DiagnosRepositoryImpl repository = (DiagnosRepositoryImpl) factory.createAndInitDiagnosRepository(fileList);
        assertNotNull(repository);
        assertEquals(150, repository.nbrOfDiagosis());
    }

    @Test
    public void testReadDiagnosFile() throws Exception {
        DiagnosRepositoryImpl diagnosRepository = new DiagnosRepositoryImpl();
        factory.populateRepoFromDiagnosisCodeFile(FILE_3, diagnosRepository);
        diagnosRepository.openLuceneIndexReader();
        assertEquals(980, diagnosRepository.nbrOfDiagosis());
        assertNotNull(diagnosRepository.getDiagnosesByCode("A00-"));
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
