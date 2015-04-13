package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.api.exception.ExternalServiceCallException;
import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceStoreTest extends AbstractIntygServiceTest {

    @Test
    public void testStoreIntyg() throws Exception {

        Utkast utkast = createUtkast();

        IntygServiceResult res = intygService.storeIntyg(utkast);
        assertEquals(IntygServiceResult.OK, res);

        verify(certificateSenderService, times(1)).storeCertificate(INTYG_ID, INTYG_TYP_FK, json);
    }

    // TODO This one is not really applicable in this context, needs to be moved into Processor code of certificate-sender
    //@Test
    public void testStoreIntygFailingWithExternalServiceCallException() throws Exception {

        Mockito.doThrow(new ExternalServiceCallException("")).when(moduleFacade).registerCertificate(INTYG_TYP_FK, json);

        Utkast utkast = createUtkast();

        IntygServiceResult res = intygService.storeIntyg(utkast);
        assertEquals(IntygServiceResult.RESCHEDULED, res);
        
        // this error should schedule a resend
        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    }

    // TODO This test needs to be moved to the new certificate-sender module.
//    @Test
//    public void testStoreIntygFailingWithException() throws Exception {
//
//        Mockito.doThrow(new IntygModuleFacadeException("")).when(moduleFacade).registerCertificate(INTYG_TYP_FK, json);
//
//        Utkast utkast = createUtkast();
//
//        try {
//            intygService.storeIntyg(utkast);
//            Assert.fail("WebCertServiceException expected");
//        } catch (WebCertServiceException expected) {
//            // Expected
//        }
//
//        // this error should not schedule a resend
//        verify(omsandningRepository, times(1)).save(any(Omsandning.class));
//        verify(omsandningRepository, times(1)).delete(any(Omsandning.class));
//    }

    // TODO Check if we should verify that Utkast exists before store.
    // @Test
    public void testStoreIntygFailingWithStatusWhenNoUtkastFound() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(null);
        IntygServiceResult intygServiceResult = intygService.storeIntyg(createUtkast());
        assertEquals(IntygServiceResult.FAILED, intygServiceResult);
        verify(omsandningRepository, times(0)).delete(any(Omsandning.class));
    }

    private Utkast createUtkast() {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setIntygsTyp(INTYG_TYP_FK);
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setModel(json);
        return utkast;
    }

    private Omsandning createOmsandning() {
        Omsandning omsandning = new Omsandning();
        omsandning.setIntygId(INTYG_ID);
        omsandning.setIntygTyp(INTYG_TYP_FK);
        return omsandning;
    }

}
