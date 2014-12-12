package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

import javax.xml.ws.WebServiceException;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceStoreTest extends AbstractIntygServiceTest {

    @Test
    public void testStoreIntyg() throws IntygModuleFacadeException {

        UtlatandeType utlatandeType = new UtlatandeType();
        when(moduleFacade.convertFromInternalToTransport(INTYG_TYP_FK, INTYG_INTERNAL_JSON_MODEL)).thenReturn(utlatandeType);

        RegisterCertificateResponseType responseType = new RegisterCertificateResponseType();
        ResultType result = new ResultType();
        responseType.setResult(result);
        result.setResultCode(ResultCodeType.OK);

        when(registerService.registerCertificate(anyString(), any(RegisterCertificateType.class))).thenReturn(responseType);

        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP_FK);
        intyg.setModel(INTYG_INTERNAL_JSON_MODEL);

        IntygServiceResult res = intygService.storeIntyg(intyg);
        assertEquals(IntygServiceResult.OK, res);
        
        verify(omsandningRepository).save(any(Omsandning.class));
        
        // if all went well the resend should be deleted
        verify(omsandningRepository).delete(any(Omsandning.class));
    }

    @Test
    public void testStoreIntygFailingWithTechnicalError() throws IntygModuleFacadeException {

        UtlatandeType utlatandeType = new UtlatandeType();
        when(moduleFacade.convertFromInternalToTransport(INTYG_TYP_FK, INTYG_INTERNAL_JSON_MODEL)).thenReturn(utlatandeType);

        RegisterCertificateResponseType responseType = new RegisterCertificateResponseType();
        ResultType result = new ResultType();
        responseType.setResult(result);
        result.setResultCode(ResultCodeType.ERROR);
        result.setResultText("Error occured!");
        result.setErrorId(ErrorIdType.TECHNICAL_ERROR);
        when(registerService.registerCertificate(anyString(), any(RegisterCertificateType.class))).thenReturn(responseType);

        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP_FK);
        intyg.setModel(INTYG_INTERNAL_JSON_MODEL);

        IntygServiceResult res = intygService.storeIntyg(intyg);
        assertEquals(IntygServiceResult.RESCHEDULED, res);
        
        // this error should schedule a resend
        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    }

    @Test
    public void testStoreIntygWithInfoResponse() throws IntygModuleFacadeException {

        UtlatandeType utlatandeType = new UtlatandeType();
        when(moduleFacade.convertFromInternalToTransport(INTYG_TYP_FK, INTYG_INTERNAL_JSON_MODEL)).thenReturn(utlatandeType);

        RegisterCertificateResponseType responseType = new RegisterCertificateResponseType();
        ResultType result = new ResultType();
        responseType.setResult(result);
        result.setResultCode(ResultCodeType.INFO);
        result.setResultText("This is something important!");
        when(registerService.registerCertificate(anyString(), any(RegisterCertificateType.class))).thenReturn(responseType);

        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP_FK);
        intyg.setModel(INTYG_INTERNAL_JSON_MODEL);

        IntygServiceResult res = intygService.storeIntyg(intyg);
        assertEquals(IntygServiceResult.OK, res);

        // this should not schedule a resend
        verify(omsandningRepository).save(any(Omsandning.class));
        verify(omsandningRepository).delete(any(Omsandning.class));
    }

    @Test
    public void testStoreIntygFailingWithException() throws IntygModuleFacadeException {

        UtlatandeType utlatandeType = new UtlatandeType();
        when(moduleFacade.convertFromInternalToTransport(INTYG_TYP_FK, INTYG_INTERNAL_JSON_MODEL)).thenReturn(utlatandeType);

        RegisterCertificateResponseType responseType = new RegisterCertificateResponseType();
        ResultType result = new ResultType();
        responseType.setResult(result);
        result.setResultCode(ResultCodeType.ERROR);
        result.setResultText("Error occured!");
        result.setErrorId(ErrorIdType.TECHNICAL_ERROR);
        when(registerService.registerCertificate(anyString(), any(RegisterCertificateType.class))).thenThrow(new WebServiceException());

        Intyg intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYP_FK);
        intyg.setModel(INTYG_INTERNAL_JSON_MODEL);

        IntygServiceResult res = intygService.storeIntyg(intyg);
        assertEquals(IntygServiceResult.RESCHEDULED, res);

        // this error should schedule a resend
        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    }

}
