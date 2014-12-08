package se.inera.webcert.notifications.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.ArbetsformagaType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.DiagnosType;
import se.inera.webcert.notifications.TestDataUtil;
import se.inera.webcert.notifications.process.EnrichWithIntygModelDataStrategy.NedsattningsPeriod;

import com.jayway.jsonpath.ReadContext;


public class EnrichWithIntygModelDataStrategyTest {

    private EnrichWithIntygModelDataStrategy enricher;
    
    private ReadContext intyg1JsonCtx;
    
    private ReadContext intyg2JsonCtx;
    
    @Before
    public void setup() {
        enricher = new EnrichWithIntygModelDataStrategy();
        String intyg1json = TestDataUtil.readRequestFromFile("utlatande/utlatande-intyg-1.json");
        intyg1JsonCtx = enricher.setupJsonContext(intyg1json);
        String intyg2json = TestDataUtil.readRequestFromFile("utlatande/utlatande-intyg-2.json");
        intyg2JsonCtx = enricher.setupJsonContext(intyg2json);
    }
    
    @Test
    public void testExtractArbetsformagor() {
        
        List<ArbetsformagaType> arbetsformagor = enricher.extractArbetsformagor(intyg1JsonCtx);
        assertEquals(2, arbetsformagor.size());
    }
    
    @Test
    public void testExtractArbetsformagaNotFound() {
      
        ArbetsformagaType res = enricher.extractToArbArbetsformagaType("100", EnrichWithIntygModelDataStrategy.NEDSATT_100_JSONP, intyg1JsonCtx);
        assertNull(res);
    }
    
    @Test
    public void testExtractArbetsformagaWith25() {
        
        ArbetsformagaType res = enricher.extractToArbArbetsformagaType("25", EnrichWithIntygModelDataStrategy.NEDSATT_25_JSONP, intyg1JsonCtx);
        assertNotNull(res);
        assertNotNull(res.getPeriod());
        assertNotNull(res.getPeriod().getFrom());
        assertNotNull(res.getPeriod().getTom());
        assertNotNull(res.getVarde());   
    }
    
    @Test
    public void testExtractNedsattning() {
        NedsattningsPeriod res = enricher.extractToNedsattningsPeriod(EnrichWithIntygModelDataStrategy.NEDSATT_25_JSONP, intyg1JsonCtx);
        assertNotNull(res);
    }
    
    @Test
    public void testExtractDiagnosKod() {        
        DiagnosType res = enricher.extractDiagnos(intyg1JsonCtx);
        assertNotNull(res);
        assertEquals("S47", res.getCode());
        assertNotNull(res.getDisplayName());
        assertTrue(res.getDisplayName().contains("Klämskada"));
    }
    
    @Test
    public void testExtractDiagnosKodNotFound() {        
        DiagnosType res = enricher.extractDiagnos(intyg2JsonCtx);
        assertNull(res);
    }
    
    
}
