package se.inera.intyg.webcert.persistence.legacy.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.legacy.model.MigreratMedcertIntyg;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class MigratedMedcertIntygRepositoryTest {

    @Autowired
    private MigreratMedcertIntygRepository medcertIntygRepository;

    @Test
    public void testSaveMigreratIntyg() {

        MigreratMedcertIntyg intyg1 = new MigreratMedcertIntyg();

        intyg1.setIntygsId("intyg1");
        intyg1.setEnhetsId("enhet1");
        intyg1.setIntygsTyp("fk7263");
        intyg1.setMigreradFran("landtinget");
        intyg1.setPatientNamn("Test Testsson");
        intyg1.setPatientPersonnummer(new Personnummer("19121212-1212"));
        intyg1.setSkapad(new LocalDateTime("2013-03-01T11:11:11"));
        intyg1.setSkickad(new LocalDateTime("2013-03-01T12:34:56"));
        intyg1.setUrsprung("APPLICATION");
        intyg1.setIntygsData("VGhpcyBpcyBhIGxlZ2FjeSBjZXJ0aWZpY2F0ZQ==".getBytes());

        medcertIntygRepository.save(intyg1);

        MigreratMedcertIntyg intyg2 = medcertIntygRepository.findOne("intyg1");
        assertNotNull(intyg2);
        assertEquals("intyg1", intyg2.getIntygsId());
        assertEquals("Test Testsson", intyg2.getPatientNamn());
        assertEquals(new LocalDateTime("2013-03-01T12:34:56"), intyg2.getSkickad());
        assertTrue(intyg2.getIntygsData() != null);
    }
}
