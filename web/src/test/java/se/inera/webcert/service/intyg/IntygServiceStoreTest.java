package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.api.exception.ExternalServiceCallException;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceStoreTest extends AbstractIntygServiceTest {

    @Test
    public void testStoreIntyg() throws Exception {

        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP_FK);
        intyg.setModel(json);

        IntygServiceResult res = intygService.storeIntyg(intyg);
        assertEquals(IntygServiceResult.OK, res);
        
        verify(omsandningRepository).save(any(Omsandning.class));
        
        // if all went well the resend should be deleted
        verify(omsandningRepository).delete(any(Omsandning.class));
        verify(moduleFacade).registerCertificate(INTYG_TYP_FK, json);
    }

    @Test
    public void testStoreIntygFailingWithExternalServiceCallException() throws Exception {

        Mockito.doThrow(new ExternalServiceCallException("")).when(moduleFacade).registerCertificate(INTYG_TYP_FK, json);

        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP_FK);
        intyg.setModel(json);

        IntygServiceResult res = intygService.storeIntyg(intyg);
        assertEquals(IntygServiceResult.RESCHEDULED, res);
        
        // this error should schedule a resend
        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    }

    @Test
    public void testStoreIntygFailingWithException() throws Exception {

        Mockito.doThrow(new IntygModuleFacadeException("")).when(moduleFacade).registerCertificate(INTYG_TYP_FK, json);

        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP_FK);
        intyg.setModel(json);

        try {
            intygService.storeIntyg(intyg);
            Assert.fail("WebCertServiceException expected");
        } catch (WebCertServiceException expected) {
            // Expected
        }

        // this error should not schedule a resend
        verify(omsandningRepository, times(1)).save(any(Omsandning.class));
        verify(omsandningRepository, times(1)).delete(any(Omsandning.class));
    }

}
