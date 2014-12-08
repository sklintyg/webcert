package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;

import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.service.draft.TicketTracker;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.util.ReflectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceRevokeTest extends AbstractIntygServiceTest {

    private static final String REVOKE_MSG = "This is revoked";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";

    private static final String INTYG_ID = "abc123";

    private Intyg intygSigned;
    private HoSPerson hoSPerson;

    @Before
    public void setup() {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getHsaId());
        vardperson.setNamn(hoSPerson.getNamn());

        intygSigned = createIntyg(INTYG_ID, INTYG_TYPE, IntygsStatus.SIGNED, INTYG_JSON, vardperson);

        WebCertUser user = new WebCertUser();
        user.setNamn(hoSPerson.getNamn());
        user.setHsaId(hoSPerson.getHsaId());

        when(webCertUserService.getWebCertUser()).thenReturn(user);

        ReflectionUtils.setTypedField(intygSignatureService, new TicketTracker());
        ReflectionUtils.setTypedField(intygSignatureService, new CustomObjectMapper());
    }

    private Intyg createIntyg(String intygId, String type, IntygsStatus status, String model, VardpersonReferens vardperson) {
        Intyg intyg = new Intyg();
        intyg.setIntygsId(intygId);
        intyg.setIntygsTyp(type);
        intyg.setStatus(status);
        intyg.setModel(model);
        intyg.setSkapadAv(vardperson);
        intyg.setSenastSparadAv(vardperson);
        return intyg;
    }

    @Test
    public void testRevokeIntyg() throws Exception {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygSigned);

        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);

        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(INTYG_EXTERNAL_JSON_MODEL, utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP_FK), any(UtlatandeType.class))).thenReturn(unmarshallResponse);

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.OK);
        response.setResult(result);

        when(revokeService.revokeMedicalCertificate((any(AttributedURIType.class)), any(RevokeMedicalCertificateRequestType.class))).thenReturn(response);
        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(false))).thenReturn(true);

        // setup notification
        ArgumentCaptor<NotificationRequestType> notificationRequestTypeArgumentCaptor = ArgumentCaptor.forClass(NotificationRequestType.class);
        IntygServiceResult res = intygService.revokeIntyg(INTYG_ID, REVOKE_MSG);

        verify(notificationService).notify(notificationRequestTypeArgumentCaptor.capture());


        assertEquals(IntygServiceResult.OK, res);

        // Assert notification message
        NotificationRequestType notificationRequestType = notificationRequestTypeArgumentCaptor.getValue();
        assertEquals(INTYG_ID, notificationRequestType.getIntygsId());
        assertEquals(HandelseType.INTYG_MAKULERAT, notificationRequestType.getHandelse());

    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeIntygWithApplicationErrorOnRevoke() throws Exception {

        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);

        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(INTYG_EXTERNAL_JSON_MODEL, utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP_FK), any(UtlatandeType.class))).thenReturn(unmarshallResponse);

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();
        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.ERROR);
        result.setErrorId(ErrorIdEnum.APPLICATION_ERROR);
        result.setErrorText("An application error occured");
        response.setResult(result);

        when(revokeService.revokeMedicalCertificate((any(AttributedURIType.class)), any(RevokeMedicalCertificateRequestType.class))).thenReturn(response);

        intygService.revokeIntyg(INTYG_ID, REVOKE_MSG);

    }

    @Test(expected = WebServiceException.class)
    public void testRevokeIntygWithIOExceptionOnRevoke() throws Exception {

        // simulate response from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = makeIntygstjanstResponse();
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);

        // setup module API behaviour
        Utlatande utlatande = makeUtlatande();
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(INTYG_EXTERNAL_JSON_MODEL, utlatande);
        when(moduleFacade.convertFromTransportToExternal(eq(INTYG_TYP_FK), any(UtlatandeType.class))).thenReturn(unmarshallResponse);

        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(false))).thenReturn(true);

        // throw exception when revoke is invoked
        when(revokeService.revokeMedicalCertificate((any(AttributedURIType.class)), any(RevokeMedicalCertificateRequestType.class))).thenThrow(
                new WebServiceException("WS exception", new ConnectException("IO exception")));

        intygService.revokeIntyg(INTYG_ID, REVOKE_MSG);

    }

    @Test(expected = WebServiceException.class)
    public void testRevokeIntygWithIOExceptionOnFetch() throws Exception {

        // simulate an exception from Intygstjanst
        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenThrow(
                new WebServiceException("WS exception", new ConnectException("IO exception")));

        intygService.revokeIntyg(INTYG_ID, REVOKE_MSG);

    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeIntygWithRevokedIntyg() throws Exception {

        // simulate an exception from Intygstjanst
        GetCertificateForCareResponseType getCertResponse = new GetCertificateForCareResponseType();
        ResultType badResult = new ResultType();
        badResult.setResultCode(ResultCodeType.ERROR);
        badResult.setErrorId(ErrorIdType.REVOKED);
        badResult.setResultText("Cert is revoked");
        getCertResponse.setResult(badResult);

        when(getCertificateService.getCertificateForCare(anyString(), any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);

        intygService.revokeIntyg(INTYG_ID, REVOKE_MSG);
    }

}
