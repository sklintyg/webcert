package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.service.intyg.converter.IntygModuleFacadeException;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.monitoring.MonitoringLogService;
import se.inera.webcert.service.signatur.SignaturTicketTracker;
import se.inera.webcert.util.ReflectionUtils;

import javax.xml.ws.WebServiceException;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashSet;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceRevokeTest extends AbstractIntygServiceTest {


    private static final String REVOKE_MSG = "This is revoked";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";

    private static final String INTYG_ID = "123";
    private static final String PATIENT_ID = "19121212-1212";

    @Mock
    private FragaSvarRepository fragaSvarRepository;

    @Mock
    private FragaSvarService fragaSvarService;

    private Utkast signedUtkast;
    private Utkast revokedUtkast;

    @Before
    public void setup() {
        HoSPerson person = buildHosPerson();
        VardpersonReferens vardperson = buildVardpersonReferens(person);
        WebCertUser user = buildWebCertUser(person);

        signedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
        revokedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, json, vardperson);
        revokedUtkast.setAterkalladDatum(LocalDateTime.now());

        when(webCertUserService.getWebCertUser()).thenReturn(user);

        ReflectionUtils.setTypedField(intygSignatureService, new SignaturTicketTracker());
        ReflectionUtils.setTypedField(intygSignatureService, new CustomObjectMapper());
    }

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testRevokeIntygWithNoOpenQuestions() throws Exception {

        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.OK);

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();
        response.setResult(result);

        signedUtkast.setIntygsId(INTYG_ID);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        // when(getCertificateService.getCertificateForCare(anyString(),
        // any(GetCertificateForCareRequestType.class))).thenReturn(getCertResponse);
        when(revokeService.revokeMedicalCertificate((any(AttributedURIType.class)), any(RevokeMedicalCertificateRequestType.class))).thenReturn(
                response);
        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(false))).thenReturn(true);
        when(fragaSvarRepository.findByIntygsReferensIntygsId(INTYG_ID)).thenReturn(new ArrayList<FragaSvar>());
        when(fragaSvarService.closeAllNonClosedQuestions(INTYG_ID)).thenReturn(new FragaSvar[0]);

        // do the call
        IntygServiceResult res = intygService.revokeIntyg(INTYG_ID, REVOKE_MSG, REVOKE_MSG);

        // verify that services were called
        verify(fragaSvarService).closeAllNonClosedQuestions(INTYG_ID);
        verify(notificationService, times(1)).sendNotificationForIntygRevoked(INTYG_ID);
        verify(logService).logRevokeIntyg(any(LogRequest.class));
        verify(intygRepository).save(any(Utkast.class));

        assertEquals(IntygServiceResult.OK, res);
    }

    @Test
    public void testRevokeIntygWithOpenQuestions() throws Exception {

        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.OK);

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();
        response.setResult(result);

        FragaSvar fragaSvar1 = buildQuestion(12345L, "<text>", "FK", Status.PENDING_INTERNAL_ACTION, LocalDateTime.now());
        FragaSvar fragaSvar2 = buildQuestion(12345L, "<text>", "WC", Status.PENDING_EXTERNAL_ACTION, LocalDateTime.now());
        FragaSvar fragaSvar3 = buildQuestion(12345L, "<text>", "FK", Status.PENDING_INTERNAL_ACTION, LocalDateTime.now());

        when(intygRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        when(revokeService.revokeMedicalCertificate((any(AttributedURIType.class)), any(RevokeMedicalCertificateRequestType.class))).thenReturn(
                response);
        when(webCertUserService.isAuthorizedForUnit(anyString(), eq(false))).thenReturn(true);
        when(fragaSvarRepository.findByIntygsReferensIntygsId(INTYG_ID)).thenReturn(new ArrayList<FragaSvar>());
        when(fragaSvarService.closeAllNonClosedQuestions(INTYG_ID)).thenReturn(new FragaSvar[] { fragaSvar1, fragaSvar2, fragaSvar3 });

        // Do the call
        IntygServiceResult res = intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG);

        // verify that notification is called
        verify(notificationService).sendNotificationForIntygRevoked(INTYG_ID);

        // verify that questions is closed
        verify(fragaSvarService).closeAllNonClosedQuestions(INTYG_ID);

        // verify that one message is sent for each question
        verify(notificationService, times(1)).sendNotificationForAnswerHandled(fragaSvar2);
        verify(notificationService, times(2)).sendNotificationForQuestionHandled(any(FragaSvar.class));

        // Verify that revoke date was added to Utkast
        verify(intygRepository).save(any(Utkast.class));

        assertEquals(IntygServiceResult.OK, res);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeIntygWithApplicationErrorOnRevoke() throws Exception {

        ResultOfCall result = new ResultOfCall();
        result.setResultCode(ResultCodeEnum.ERROR);
        result.setErrorId(ErrorIdEnum.APPLICATION_ERROR);
        result.setErrorText("An application error occured");

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();
        response.setResult(result);

        // Setup mock behaviour
        when(revokeService.revokeMedicalCertificate((any(AttributedURIType.class)), any(RevokeMedicalCertificateRequestType.class))).thenReturn(
                response);

        intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG);

        verify(intygRepository, times(0)).save(any(Utkast.class));
    }

    @Test(expected = WebServiceException.class)
    public void testRevokeIntygWithIOExceptionOnRevoke() throws Exception {
        // Throw exception when revoke is invoked
        when(revokeService.revokeMedicalCertificate((any(AttributedURIType.class)), any(RevokeMedicalCertificateRequestType.class))).thenThrow(
                new WebServiceException("WS exception", new ConnectException("IO exception")));

        // Do the call
        try {
            intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG);
        } catch (Exception e) {
            verify(intygRepository, times(0)).save(any(Utkast.class));
            throw e;
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeIntygThatHasAlreadyBeenRevokedFails() throws IntygModuleFacadeException {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(revokedUtkast);
        when(moduleFacade.getCertificate(anyString(), anyString())).thenThrow(IntygModuleFacadeException.class);
        // Do the call
        try {
            intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG);
        } catch (Exception e) {
            verifyZeroInteractions(revokeService);
            verify(intygRepository, times(0)).save(any(Utkast.class));
            throw e;
        }
    }

    private HoSPerson buildHosPerson() {
        HoSPerson person = new HoSPerson();
        person.setHsaId("AAA");
        person.setNamn("Dr Dengroth");
        return person;
    }

    private Utkast buildUtkast(String intygId, String type, UtkastStatus status, String model, VardpersonReferens vardperson) {

        Utkast intyg = new Utkast();
        intyg.setIntygsId(intygId);
        intyg.setIntygsTyp(type);
        intyg.setStatus(status);
        intyg.setModel(model);
        intyg.setSkapadAv(vardperson);
        intyg.setSenastSparadAv(vardperson);

        return intyg;
    }

    private FragaSvar buildQuestion(Long id, String frageText, String frageStallare, Status status, LocalDateTime fragaSkickadDatum) {

        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId(INTYG_ID);
        intygsReferens.setIntygsTyp("fk7263");
        intygsReferens.setPatientId(PATIENT_ID);

        FragaSvar f = new FragaSvar();
        f.setFrageStallare(frageStallare);
        f.setStatus(status);
        f.setAmne(Amne.OVRIGT);
        f.setExternReferens("<fk-extern-referens>");
        f.setInternReferens(id);
        f.setFrageSkickadDatum(fragaSkickadDatum);
        f.setFrageText(frageText);

        f.setIntygsReferens(intygsReferens);
        f.setKompletteringar(new HashSet<Komplettering>());
        f.setVardperson(new Vardperson());
        f.getVardperson().setEnhetsId("<enhets-id>");
        return f;
    }

    private VardpersonReferens buildVardpersonReferens(HoSPerson person) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(person.getHsaId());
        vardperson.setNamn(person.getNamn());
        return vardperson;
    }

    private WebCertUser buildWebCertUser(HoSPerson person) {
        WebCertUser user = new WebCertUser();
        user.setNamn(person.getNamn());
        user.setHsaId(person.getHsaId());
        return user;
    }

}
