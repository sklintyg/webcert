package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.modules.support.api.exception.ExternalServiceCallException;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.model.OmsandningOperation;
import se.inera.webcert.service.draft.TicketTracker;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.util.ReflectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceSendTest extends AbstractIntygServiceTest {

    private static final String INTYG_ID = "abc123";

    @Before
    public void setup() {
        WebCertUser user = new WebCertUser();
        user.setNamn("Dr Dengroth");
        user.setHsaId("AAA");

        when(webCertUserService.getWebCertUser()).thenReturn(user);

        ReflectionUtils.setTypedField(intygSignatureService, new TicketTracker());
        ReflectionUtils.setTypedField(intygSignatureService, new CustomObjectMapper());
    }

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testSendIntyg() throws Exception {

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        verify(omsandningRepository).save(any(Omsandning.class));
        verify(omsandningRepository).delete(any(Omsandning.class));
        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(moduleFacade).sendCertificate(INTYG_TYP_FK, json, "FK");
    }

    @Test
    public void testSendIntygFailingWithExternalServiceCallException() throws Exception {
                
        Mockito.doThrow(new ExternalServiceCallException("")).when(moduleFacade).sendCertificate(INTYG_TYP_FK, json, "FK");
                
        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
        omsandning.setConfiguration(CONFIG_AS_JSON);
        
        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.RESCHEDULED, res);

        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    }

    @Test
    public void testSendIntygFailingWithModuleFacadeException() throws Exception {
                
        Mockito.doThrow(new IntygModuleFacadeException("")).when(moduleFacade).sendCertificate(INTYG_TYP_FK, json, "FK");
                
        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
        omsandning.setConfiguration(CONFIG_AS_JSON);
        
        try {
            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
            Assert.fail("WebCertServiceException expected");
        } catch (WebCertServiceException e) {
            // Expected
        }
        
        verify(omsandningRepository, times(1)).save(any(Omsandning.class));
        verify(omsandningRepository, times(1)).delete(any(Omsandning.class));
    }
    
}
