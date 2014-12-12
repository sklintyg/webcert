package se.inera.webcert.persistence.intyg.repository;

import static org.junit.Assert.*;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.model.OmsandningOperation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({"dev","unit-testing"})
@Transactional
public class OmsandningRepositoryTest {

    private static final String CONFIGURATION = "{do-important-stuff: true}";

    private static final String INTYG_ID = "intyg-1";
    private static final String INTYG_TYP = "typ-1";
    
    @Autowired
    private OmsandningRepositoryCustom repository;
    
    @Test
    public void testOmsandingCRUD() {
        
        Long omsId = saveOmsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, true);
        assertNotNull(omsId);
        
        Omsandning oms2 = repository.findOne(omsId);
        assertEquals(INTYG_ID, oms2.getIntygId());
        assertEquals(CONFIGURATION, oms2.getConfiguration());
        
        repository.delete(oms2);
        
        assertFalse(repository.exists(omsId));
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Long saveOmsandning(OmsandningOperation op, String intygId, boolean addConfig) {
        Omsandning oms1 = new Omsandning(op, intygId, INTYG_TYP);
        oms1.setGallringsdatum(LocalDateTime.now().plusHours(1));
        oms1.setNastaForsok(LocalDateTime.now().plusMinutes(10));
        if (addConfig) {
            oms1.setConfiguration(CONFIGURATION);
        }
        return repository.save(oms1).getId();
    }
}
