package se.inera.intyg.webcert.web.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceStoreTest extends AbstractIntygServiceTest {

    @Test
    public void testStoreIntyg() throws Exception {

        Utkast utkast = createUtkast();

        IntygServiceResult res = intygService.storeIntyg(utkast);
        assertEquals(IntygServiceResult.OK, res);

        verify(certificateSenderService, times(1)).storeCertificate(INTYG_ID, INTYG_TYP_FK, json);
    }

    // TODO This one is not really applicable in this context, needs to be moved into Processor code of
    // certificate-sender
    // @Test
    public void testStoreIntygFailingWithExternalServiceCallException() throws Exception {

        Mockito.doThrow(new ExternalServiceCallException("")).when(moduleFacade).registerCertificate(INTYG_TYP_FK, json);

        Utkast utkast = createUtkast();

        IntygServiceResult res = intygService.storeIntyg(utkast);
        assertEquals(IntygServiceResult.RESCHEDULED, res);
    }

    // TODO This test needs to be moved to the new certificate-sender module.
    // @Test
    // public void testStoreIntygFailingWithException() throws Exception {
    //
    // Mockito.doThrow(new IntygModuleFacadeException("")).when(moduleFacade).registerCertificate(INTYG_TYP_FK, json);
    //
    // Utkast utkast = createUtkast();
    //
    // try {
    // intygService.storeIntyg(utkast);
    // Assert.fail("WebCertServiceException expected");
    // } catch (WebCertServiceException expected) {
    // // Expected
    // }
    //
    // // this error should not schedule a resend
    // verify(omsandningRepository, times(1)).save(any(Omsandning.class));
    // verify(omsandningRepository, times(1)).delete(any(Omsandning.class));
    // }

    // TODO Check if we should verify that Utkast exists before store.
    // @Test
    public void testStoreIntygFailingWithStatusWhenNoUtkastFound() {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(null);
        IntygServiceResult intygServiceResult = intygService.storeIntyg(createUtkast());
        assertEquals(IntygServiceResult.FAILED, intygServiceResult);

        verifyZeroInteractions(certificateSenderService);
    }

    private Utkast createUtkast() {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setIntygsTyp(INTYG_TYP_FK);
        utkast.setStatus(UtkastStatus.SIGNED);
        utkast.setModel(json);
        return utkast;
    }
}
