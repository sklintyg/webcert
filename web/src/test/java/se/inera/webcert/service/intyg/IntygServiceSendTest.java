package se.inera.webcert.service.intyg;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.model.OmsandningOperation;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.log.dto.LogRequest;

import javax.xml.ws.WebServiceException;
import java.net.ConnectException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceSendTest extends AbstractIntygServiceTest {

    @Test
    public void testSendIntyg() throws Exception {

        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);

        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(INTYG_EXTERNAL_JSON_MODEL, utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP_FK), any(UtlatandeType.class))).thenReturn(unmarshallResponse);

        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.OK);
        response.setResult(result);
        when(sendService.sendMedicalCertificate(any(AttributedURIType.class), any(SendMedicalCertificateRequestType.class))).thenReturn(response);

        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(false))).thenReturn(true);

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        verify(omsandningRepository).save(any(Omsandning.class));
        verify(omsandningRepository).delete(any(Omsandning.class));
        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
    }

    @Test
    public void testSendIntygFailingWithErrorResponse() throws Exception {

        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);

        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(INTYG_EXTERNAL_JSON_MODEL, utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP_FK), any(UtlatandeType.class))).thenReturn(unmarshallResponse);

        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.ERROR);
        result.setErrorId(ErrorIdEnum.TECHNICAL_ERROR);
        result.setErrorText("A technical error occured");
        response.setResult(result);
        when(sendService.sendMedicalCertificate(any(AttributedURIType.class), any(SendMedicalCertificateRequestType.class))).thenReturn(response);

        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID);
        omsandning.setConfiguration(CONFIG_AS_JSON);

        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(false))).thenReturn(true);

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, "FK", true);
        assertEquals(IntygServiceResult.RESCHEDULED, res);

        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    }

    @Test
    public void testSendIntygWithIOExceptionWhenSending() throws Exception {

        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);

        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(INTYG_EXTERNAL_JSON_MODEL, utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP_FK), any(UtlatandeType.class))).thenReturn(unmarshallResponse);

        WebServiceException wse = new WebServiceException(new ConnectException("Could not connect..."));
        when(sendService.sendMedicalCertificate(any(AttributedURIType.class), any(SendMedicalCertificateRequestType.class))).thenThrow(wse);

        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(false))).thenReturn(true);

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, "FK", true);
        assertEquals(IntygServiceResult.RESCHEDULED, res);

        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    }
}
