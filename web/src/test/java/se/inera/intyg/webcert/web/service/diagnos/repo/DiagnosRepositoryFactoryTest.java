/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.diagnos.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.charset.StandardCharsets;
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
    private static final String LINE_3 = "A17\t†\tTuberkulos i nervsystemet";
    private static final String LINE_3_KOD = "A17";
    private static final String LINE_3_BESK = "Tuberkulos i nervsystemet";
    private static final String LINE_4 = "A17\t*\tTuberkulos i nervsystemet";
    private static final String LINE_4_KOD = "A17";
    private static final String LINE_4_BESK = "Tuberkulos i nervsystemet";

    private static final String REALLY_MESSY_LINE = "  A050   Matförgiftning orsakad av stafylokocker  ";
    private static final String LINE_WITH_BOM = "\uFEFFA050   Matförgiftning orsakad av stafylokocker  ";

    private static final String FILE_1 = "classpath:/DiagnosService/icd10se/digit3.txt";
    private static final String FILE_2 = "classpath:/DiagnosService/icd10se/digit4.txt";
    private static final String FILE_3 = "classpath:/DiagnosService/KSH97P_SFAM_TESTKODER.ANS";

    @Autowired
    private DiagnosRepositoryFactory factory;

    @Before
    public void setup() {

    }

    @Test
    public void testCreateRepository() {
        List<String> fileList = Arrays.asList(FILE_1, FILE_2);
        DiagnosRepositoryImpl repository = (DiagnosRepositoryImpl) factory.createAndInitDiagnosRepository(fileList,
            StandardCharsets.UTF_8);
        assertNotNull(repository);
        assertEquals(11699, repository.nbrOfDiagosis());
    }

    @Test
    public void testReadDiagnosFile() throws Exception {
        DiagnosRepositoryImpl diagnosRepository = new DiagnosRepositoryImpl();
        factory.populateRepoFromDiagnosisCodeFile(FILE_3, diagnosRepository, StandardCharsets.ISO_8859_1);
        diagnosRepository.openLuceneIndexReader();
        assertEquals(980, diagnosRepository.nbrOfDiagosis());
        assertNotNull(diagnosRepository.getDiagnosesByCode("A00-"));
    }

    @Test
    public void testCreateDiagnosFromString() {

        Diagnos res = factory.createDiagnosFromString(LINE_1, false);

        assertNotNull(res);
        assertEquals(LINE_1_KOD, res.getKod());
        assertEquals(LINE_1_BESK, res.getBeskrivning());

        res = factory.createDiagnosFromString(LINE_2, false);

        assertNotNull(res);
        assertEquals(LINE_2_KOD, res.getKod());
        assertEquals(LINE_2_BESK, res.getBeskrivning());

        res = factory.createDiagnosFromString(LINE_3, false);

        assertNotNull(res);
        assertEquals(LINE_3_KOD, res.getKod());
        assertEquals(LINE_3_BESK, res.getBeskrivning());

        res = factory.createDiagnosFromString(LINE_4, false);

        assertNotNull(res);
        assertEquals(LINE_4_KOD, res.getKod());
        assertEquals(LINE_4_BESK, res.getBeskrivning());

    }

    @Test
    public void testWithNullsAndEmpty() {

        Diagnos res = factory.createDiagnosFromString(null, false);
        assertNull(res);

        res = factory.createDiagnosFromString("", false);
        assertNull(res);

        res = factory.createDiagnosFromString("  ", false);
        assertNull(res);
    }

    @Test
    public void testWithMessyString() {

        Diagnos res = factory.createDiagnosFromString(REALLY_MESSY_LINE, false);
        assertNotNull(res);
        assertEquals("A050", res.getKod());
        assertEquals("Matförgiftning orsakad av stafylokocker", res.getBeskrivning());

    }

    @Test
    public void testRemoveBOMFromString() {

        Diagnos res = factory.createDiagnosFromString(LINE_WITH_BOM, true);
        assertNotNull(res);
        assertEquals("A050", res.getKod());
        assertEquals("Matförgiftning orsakad av stafylokocker", res.getBeskrivning());

    }

}
