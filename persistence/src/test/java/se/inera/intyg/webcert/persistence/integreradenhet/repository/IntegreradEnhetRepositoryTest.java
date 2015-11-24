package se.inera.intyg.webcert.persistence.integreradenhet.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class IntegreradEnhetRepositoryTest {

    @Autowired
    private IntegreradEnhetRepository repository;

    @Test
    public void testSaveIntegreradEnhet() {

        IntegreradEnhet enhet = new IntegreradEnhet();
        enhet.setEnhetsId("SE1234567890-1A01");
        enhet.setEnhetsNamn("Enhet 1");
        enhet.setVardgivarId("SE1234567890-2B01");
        enhet.setVardgivarNamn("Vardgivare 1");

        IntegreradEnhet savedEnhet = repository.save(enhet);

        assertNotNull(savedEnhet);
        assertNotNull(savedEnhet.getSkapadDatum());
        assertNull(savedEnhet.getSenasteKontrollDatum());
    }

}
