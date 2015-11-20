package se.inera.intyg.webcert.web.service.fragasvar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.modules.support.feature.ModuleFeature;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarFilter;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.FrageStallare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.util.ReflectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class FragaSvarServiceImplTest {

    private static final Personnummer PATIENT_ID = new Personnummer("19121212-1212");

    private static final LocalDateTime JANUARY = new LocalDateTime("2013-01-12T11:22:11");
    private static final LocalDateTime MAY = new LocalDateTime("2013-05-01T11:11:11");
    private static final LocalDateTime AUGUST = new LocalDateTime("2013-08-02T11:11:11");
    private static final LocalDateTime DECEMBER_YEAR_9999 = new LocalDateTime("9999-12-11T10:22:00");

    @Mock
    private FragaSvarRepository fragasvarRepositoryMock;

    @Mock
    private SendMedicalCertificateAnswerResponderInterface sendAnswerToFKClientMock;

    @Mock
    private SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClientMock;

    @Mock
    private IntygService intygServiceMock;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private WebcertFeatureService webcertFeatureServiceMock;

    @Mock
    private NotificationService notificationServiceMock;

    @Mock
    private Logger loggerMock;

    @Mock
    private MonitoringLogService monitoringServiceMock;

    @Spy
    private ObjectMapper objectMapper = new CustomObjectMapper();

    @InjectMocks
    private FragaSvarServiceImpl service;

    @Before
    public void setUpLoggerFactory() throws Exception {
        ReflectionUtils.setStaticFinalAttribute(FragaSvarServiceImpl.class, "LOGGER", loggerMock);
    }

    @Before
    public void setupCommonBehaviour() {
        when(webcertFeatureServiceMock.isModuleFeatureActive(eq(ModuleFeature.HANTERA_FRAGOR), anyString())).thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByEnhetsIdSorting() {
        List<FragaSvar> unsortedList = new ArrayList<>();
        unsortedList.add(buildFragaSvar(1L, MAY, null));
        unsortedList.add(buildFragaSvar(2L, DECEMBER_YEAR_9999, null));
        unsortedList.add(buildFragaSvar(3L, null, JANUARY));
        unsortedList.add(buildFragaSvar(4L, null, AUGUST));
        when(fragasvarRepositoryMock.findByEnhetsId(any(List.class))).thenReturn(unsortedList);

        List<FragaSvar> result = service.getFragaSvar(Collections.singletonList("123"));

        assertEquals(4, result.size());

        assertEquals(2, (long) result.get(0).getInternReferens());
        assertEquals(4, (long) result.get(1).getInternReferens());
        assertEquals(1, (long) result.get(2).getInternReferens());
        assertEquals(3, (long) result.get(3).getInternReferens());

    }

    @Test
    public void testFindByIntygSorting() {
        List<FragaSvar> unsortedList = new ArrayList<>();
        unsortedList.add(buildFragaSvar(1L, MAY, null));
        unsortedList.add(buildFragaSvar(2L, DECEMBER_YEAR_9999, null));
        unsortedList.add(buildFragaSvar(3L, null, JANUARY));
        unsortedList.add(buildFragaSvar(4L, null, AUGUST));
        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId((any(String.class)))).thenReturn(unsortedList);
        when(webCertUserService.getUser()).thenReturn(createUser());

        List<FragaSvar> result = service.getFragaSvar("intygsid");

        assertEquals(4, result.size());

        assertEquals(2, (long) result.get(0).getInternReferens());
        assertEquals(4, (long) result.get(1).getInternReferens());
        assertEquals(1, (long) result.get(2).getInternReferens());
        assertEquals(3, (long) result.get(3).getInternReferens());

    }

    private FragaSvar buildFragaSvar(Long id, LocalDateTime fragaSkickadDatum, LocalDateTime svarSkickadDatum) {
        FragaSvar f = new FragaSvar();
        f.setStatus(Status.PENDING_INTERNAL_ACTION);
        f.setAmne(Amne.OVRIGT);
        f.setExternReferens("<fk-extern-referens>");
        f.setInternReferens(id);
        f.setFrageSkickadDatum(fragaSkickadDatum);
        f.setFrageText("frageText");
        f.setSvarSkickadDatum(svarSkickadDatum);

        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId("<intygsId>");
        intygsReferens.setIntygsTyp("fk7263");
        intygsReferens.setPatientId(PATIENT_ID);
        f.setIntygsReferens(intygsReferens);
        f.setKompletteringar(new HashSet<Komplettering>());
        f.setVardperson(new Vardperson());
        f.getVardperson().setEnhetsId("enhet");
        return f;
    }

    private FragaSvar buildFraga(Long id, String frageText, Amne amne, LocalDateTime fragaSkickadDatum) {
        FragaSvar f = new FragaSvar();
        f.setStatus(Status.PENDING_INTERNAL_ACTION);
        f.setAmne(amne);
        f.setExternReferens("<fk-extern-referens>");
        f.setInternReferens(id);
        f.setFrageSkickadDatum(fragaSkickadDatum);
        f.setFrageText(frageText);

        IntygsReferens intygsReferens = new IntygsReferens();
        intygsReferens.setIntygsId("<intygsId>");
        intygsReferens.setIntygsTyp("fk7263");
        intygsReferens.setPatientId(PATIENT_ID);
        f.setIntygsReferens(intygsReferens);
        f.setKompletteringar(new HashSet<Komplettering>());
        f.setVardperson(new Vardperson());
        f.getVardperson().setEnhetsId("enhet");
        return f;
    }

    @Test
    public void testGetFragaSvarForIntyg() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvar(1L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        fragaSvarList.add(buildFragaSvar(2L, new LocalDateTime(), new LocalDateTime()));
        fragaSvarList.add(buildFragaSvar(3L, JANUARY, JANUARY));

        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getUser()).thenReturn(createUser());

        List<FragaSvar> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getUser();

        assertEquals(3, result.size());
        assertEquals(fragaSvarList, result);
    }

    @Test
    public void testGetFragaSvarFilteringForIntyg() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvar(1L, DECEMBER_YEAR_9999, DECEMBER_YEAR_9999));
        fragaSvarList.add(buildFragaSvar(2L, new LocalDateTime(), new LocalDateTime()));
        fragaSvarList.add(buildFragaSvar(3L, JANUARY, JANUARY));

        // the second question/answer pair was sent to a unit out of the current user's range -> has to be filtered
        fragaSvarList.get(1).getVardperson().setEnhetsId("another unit without my range");

        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getUser()).thenReturn(createUser());

        List<FragaSvar> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getUser();

        assertEquals(2, result.size());
        assertEquals(fragaSvarList.get(0), result.get(0));
        assertEquals(fragaSvarList.get(2), result.get(1));
    }

    @Test
    public void testSaveFragaOK() throws IOException {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fraga);

        // mock ws ok response
        SendMedicalCertificateQuestionResponseType wsResponse = new SendMedicalCertificateQuestionResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(sendQuestionToFKClientMock.sendMedicalCertificateQuestion(
                any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class))).thenReturn(wsResponse);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());

        verify(webCertUserService).getUser();
        verify(notificationServiceMock).sendNotificationForQuestionSent(any(FragaSvar.class));
        verify(webCertUserService).isAuthorizedForUnit(anyString(), eq(false));
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        verify(sendQuestionToFKClientMock).sendMedicalCertificateQuestion(any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class));
        verify(monitoringServiceMock).logQuestionSent(anyString(), any(Long.class), anyString(), anyString(), anyString());

        assertEquals(Status.PENDING_EXTERNAL_ACTION, capture.getValue().getStatus());
        assertEquals(getIntygContentHolder().getUtlatande().getGrundData().getSkapadAv().getVardenhet().getEnhetsid(), capture.getValue()
                .getVardperson()
                .getEnhetsId());

    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotSentToFK() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNoFrageText() throws IOException {
        FragaSvar fraga = buildFraga(1L, null, Amne.OVRIGT, new LocalDateTime());
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNoAmne() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", null, new LocalDateTime());
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotAuthorizedForUnit() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());

        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(false);
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);

        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fraga);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());

    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaOnRevokedCertificate() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp())).thenReturn(
                getRevokedIntygContentHolder());

        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());

    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaWsHTMLError() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        when(intygServiceMock.fetchIntygData(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());

        // mock error with content type html
        SOAPFault soapFault = null;
        try {
            soapFault = SOAPFactory.newInstance().createFault();
            soapFault.setFaultString("Response was of unexpected text/html ContentType.");
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        when(sendQuestionToFKClientMock.sendMedicalCertificateQuestion(
                any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class))).thenThrow(new SOAPFaultException(soapFault));

        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                fraga.getFrageText());
    }

    @Test
    public void testSetVidareBefordradOK() {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());
        // set it to false initially
        fraga.setVidarebefordrad(false);

        when(fragasvarRepositoryMock.findOne(any(Long.class))).thenReturn(fraga);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fraga);

        // test call
        service.setDispatchState(fraga.getInternReferens(), true);

        verify(fragasvarRepositoryMock).findOne(any(Long.class));
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));

        assertEquals(true, capture.getValue().getVidarebefordrad());
    }

    @Test
    public void testSaveSvarOK() throws IOException {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());

        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());

        // ArgumentCaptor<NotificationRequestType> notCapture = ArgumentCaptor.forClass(NotificationRequestType.class);

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);
        // mock ws ok response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(
                sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                        any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        FragaSvar result = service.saveSvar(1L, "svarsText");

        verify(fragasvarRepositoryMock).findOne(1L);
        verify(webCertUserService).getUser();
        verify(webCertUserService).isAuthorizedForUnit(anyString(), eq(false));
        verify(fragasvarRepositoryMock).save(fragaSvar);
        verify(sendAnswerToFKClientMock).sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class));
        verify(notificationServiceMock).sendNotificationForQuestionHandled(any(FragaSvar.class));
        verify(monitoringServiceMock).logAnswerSent(anyString(), any(Long.class), anyString(), anyString(), anyString());

        assertEquals("svarsText", result.getSvarsText());
        assertEquals(Status.CLOSED, result.getStatus());
        assertNotNull(result.getSvarSkickadDatum());
    }

    @Test(expected = WebCertServiceException.class)
    public void testExceptionThrownWhenIntygIsUnsentToFK() throws IOException {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());
        String intygsId = fraga.getIntygsReferens().getIntygsId();
        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(intygsId, fraga.getIntygsReferens().getIntygsTyp())).thenReturn(getUnsentIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } catch (Exception e) {
            verifyZeroInteractions(sendQuestionToFKClientMock);
            verifyZeroInteractions(notificationServiceMock);
            verify(fragasvarRepositoryMock, times(0)).save(any(FragaSvar.class));
            throw e;
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testExceptionThrownWhenIntygIsRevoked() throws IOException {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());
        String intygsId = fraga.getIntygsReferens().getIntygsId();
        // Setup when - given -then

        when(intygServiceMock.fetchIntygData(intygsId, fraga.getIntygsReferens().getIntygsTyp())).thenReturn(getRevokedIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        // test call
        try {
            service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getIntygsReferens().getIntygsTyp(), fraga.getAmne(),
                    fraga.getFrageText());
        } catch (Exception e) {
            verifyZeroInteractions(sendQuestionToFKClientMock);
            verifyZeroInteractions(notificationServiceMock);
            verify(fragasvarRepositoryMock, times(0)).save(any(FragaSvar.class));
            throw e;
        }
    }

    private IntygContentHolder getIntygContentHolder() {
        List<se.inera.certificate.model.Status> status = new ArrayList<>();
        status.add(new se.inera.certificate.model.Status(CertificateState.RECEIVED, "MI", LocalDateTime.now()));
        status.add(new se.inera.certificate.model.Status(CertificateState.SENT, "FK", LocalDateTime.now()));
        return new IntygContentHolder("<external-json/>", getUtlatande(), status, false);
    }

    private IntygContentHolder getUnsentIntygContentHolder() {
        List<se.inera.certificate.model.Status> status = new ArrayList<>();
        status.add(new se.inera.certificate.model.Status(CertificateState.RECEIVED, "MI", LocalDateTime.now()));
        return new IntygContentHolder("<external-json/>", getUtlatande(), status, false);
    }

    private IntygContentHolder getRevokedIntygContentHolder() {
        List<se.inera.certificate.model.Status> status = new ArrayList<>();
        status.add(new se.inera.certificate.model.Status(CertificateState.RECEIVED, "MI", LocalDateTime.now()));
        status.add(new se.inera.certificate.model.Status(CertificateState.SENT, "FK", LocalDateTime.now()));
        status.add(new se.inera.certificate.model.Status(CertificateState.CANCELLED, "MI", LocalDateTime.now()));
        return new IntygContentHolder("<external-json/>", getUtlatande(), status, true);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWsError() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);

        // mock ws error response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.failResult("some error"));
        when(
                sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                        any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWsHTMLError() throws IOException {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());

        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());

        when(webCertUserService.getUser()).thenReturn(createUser());
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getUser()).thenReturn(createUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);

        // mock error with content type html
        SOAPFault soapFault = null;
        try {
            soapFault = SOAPFactory.newInstance().createFault();
            soapFault.setFaultString("Response was of unexpected text/html ContentType.");
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        when(sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class))).thenThrow(new SOAPFaultException(soapFault));

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWrongStateForAnswering() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setStatus(Status.ANSWERED);

        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        service.saveSvar(1L, "svarsText");
    }

    private Utlatande getUtlatande() {
        // create mocked Utlatande from intygstjansten
        try {
            return new CustomObjectMapper().readValue(new ClassPathResource(
                    "FragaSvarServiceImplTest/utlatande.json").getFile(), Utlatande.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarForKompletteringAndNotDoctor() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());
        WebCertUser nonDoctor = createUser();
        when(webCertUserService.getUser()).thenReturn(nonDoctor);

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarForPaminnelse() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setAmne(Amne.PAMINNELSE);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.getUser()).thenReturn(createUser());

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarNotAuthorizedForunit() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchIntygData(fragaSvar.getIntygsReferens().getIntygsId(), fragaSvar.getIntygsReferens().getIntygsTyp())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(false);

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveMissingSvarsText() {
        service.saveSvar(1L, null);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveMissingIntygsId() {
        service.saveSvar(null, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarIntygNotFound() {

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(null);
        service.saveSvar(1L, "svarsText");

    }

    @Test
    public void testCloseQuestionToFKAsHandledOK() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga till FK");
        fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);

        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);

        service.closeQuestionAsHandled(1L);

        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.CLOSED, fsCapture.getValue().getStatus());
    }

    @Test
    public void testCloseQuestionFromFKAsHandledOK() {
        // This is a question from FK that we have no intention to answer so we close it
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga från FK till WC");
        fragaSvar.setStatus(Status.PENDING_INTERNAL_ACTION);

        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);

        service.closeQuestionAsHandled(1L);

        verify(notificationServiceMock).sendNotificationForQuestionHandled(any(FragaSvar.class));
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));

        assertEquals(Status.CLOSED, fsCapture.getValue().getStatus());
        // assertEquals(HandelseType.FRAGA_FRAN_FK_HANTERAD, notCapture.getValue().getHandelse());
    }

    @Test
    public void testCloseAnsweredQuestionToFKAsHandledOK() {
        // This is a question that we sent to FK that has been answered so we now have to close it
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga från WC till FK");
        fragaSvar.setStatus(Status.ANSWERED);
        fragaSvar.setSvarsText("Med ett svar från FK");

        ArgumentCaptor<FragaSvar> fsCapture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(fsCapture.capture())).thenReturn(fragaSvar);

        service.closeQuestionAsHandled(1L);

        verify(notificationServiceMock).sendNotificationForAnswerHandled(any(FragaSvar.class));
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));

        assertEquals(Status.CLOSED, fsCapture.getValue().getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testCloseAshandledNotFound() {
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(null);
        service.closeQuestionAsHandled(1L);
    }

    @Test
    public void testOpenAsUnhandledFromFK() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga till WC från FK");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);

        verify(notificationServiceMock).sendNotificationForQuestionReceived(any(FragaSvar.class));
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.PENDING_INTERNAL_ACTION, capture.getValue().getStatus());
    }

    @Test
    public void testOpenAsUnhandledToFKNoAnsw() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga till FK från WC");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);

        verifyZeroInteractions(notificationServiceMock);
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.PENDING_EXTERNAL_ACTION, capture.getValue().getStatus());
    }

    @Test
    public void testOpenAsUnhandledFromWCWithAnswer() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare(FrageStallare.WEBCERT.getKod());
        fragaSvar.setFrageText("Fråga till FK från WC");
        fragaSvar.setSvarsText("Med ett svar från FK");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);

        verify(notificationServiceMock).sendNotificationForAnswerRecieved(any(FragaSvar.class));
        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.ANSWERED, capture.getValue().getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledNotFound() {
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(null);
        service.openQuestionAsUnhandled(1L);
    }

    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledInvalidState() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare(FrageStallare.FORSAKRINGSKASSAN.getKod());
        fragaSvar.setFrageText("Fråga från FK");
        fragaSvar.setSvarsText("Svar till FK från WC");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);
    }

    @Test
    public void testVerifyEnhetsAuthOK() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), any(Boolean.class))).thenReturn(true);
        service.verifyEnhetsAuth("enhet");

        verify(webCertUserService).isAuthorizedForUnit(anyString(), any(Boolean.class));
    }

    @Test(expected = WebCertServiceException.class)
    public void testVerifyEnhetsAuthFail() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), any(Boolean.class))).thenReturn(false);
        service.verifyEnhetsAuth("<doesnt-exist>");
    }

    @Test(expected = WebCertServiceException.class)
    public void testFilterFragaSvarWithAuthFail() {
        WebCertUser webCertUser = createUser();
        when(webCertUserService.getUser()).thenReturn(webCertUser);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId("no-auth");

        service.filterFragaSvar(params);
    }

    @Test
    public void testFilterFragaSvarWithEnhetsIdAsParam() {

        WebCertUser webCertUser = createUser();
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(true))).thenReturn(true);

        List<FragaSvar> queryResults = new ArrayList<>();
        queryResults.add(buildFragaSvar(1L, MAY, null));
        queryResults.add(buildFragaSvar(2L, MAY, null));

        when(fragasvarRepositoryMock.filterFragaSvar(any(FragaSvarFilter.class))).thenReturn(queryResults);
        when(fragasvarRepositoryMock.filterCountFragaSvar(any(FragaSvarFilter.class))).thenReturn(queryResults.size());

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId(webCertUser.getValdVardenhet().getId());

        QueryFragaSvarResponse response = service.filterFragaSvar(params);

        verify(webCertUserService).isAuthorizedForUnit(anyString(), eq(true));

        verify(fragasvarRepositoryMock).filterFragaSvar(any(FragaSvarFilter.class));
        verify(fragasvarRepositoryMock).filterCountFragaSvar(any(FragaSvarFilter.class));

        assertNotNull(response);
        assertEquals(2, response.getResults().size());
    }

    @Test
    public void testFilterFragaSvarWithNoEnhetsIdAsParam() {

        when(webCertUserService.getUser()).thenReturn(createUser());

        List<FragaSvar> queryResults = new ArrayList<>();
        queryResults.add(buildFragaSvar(1L, MAY, null));
        queryResults.add(buildFragaSvar(2L, MAY, null));

        when(fragasvarRepositoryMock.filterFragaSvar(any(FragaSvarFilter.class))).thenReturn(queryResults);
        when(fragasvarRepositoryMock.filterCountFragaSvar(any(FragaSvarFilter.class))).thenReturn(queryResults.size());

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        QueryFragaSvarResponse response = service.filterFragaSvar(params);

        verify(webCertUserService).getUser();

        verify(fragasvarRepositoryMock).filterFragaSvar(any(FragaSvarFilter.class));
        verify(fragasvarRepositoryMock).filterCountFragaSvar(any(FragaSvarFilter.class));

        assertNotNull(response);
        assertEquals(2, response.getResults().size());
    }

    @Test
    public void testGetMDByEnhetsIdOK() {
        String enhetsId = "enhet";
        when(webCertUserService.isAuthorizedForUnit(any(String.class), eq(false))).thenReturn(true);

        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[] { "HSA-1_ID", "NAMN1" });
        queryResult.add(new Object[] { "HSA-2_ID", "NAMN2" });
        queryResult.add(new Object[] { "HSA-3_ID", "NAMN3" });
        queryResult.add(new Object[] { "HSA-4_ID", "NAMN4" });

        when(fragasvarRepositoryMock.findDistinctFragaSvarHsaIdByEnhet(Matchers.anyListOf(String.class))).thenReturn(queryResult);
        List<Lakare> result = service.getFragaSvarHsaIdByEnhet(enhetsId);
        ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
        verify(webCertUserService).isAuthorizedForUnit(capture.capture(), eq(false));

        verify(fragasvarRepositoryMock).findDistinctFragaSvarHsaIdByEnhet(Matchers.anyListOf(String.class));
        assertEquals(enhetsId, capture.getValue());
        assertEquals(4, result.size());
    }

    @Test
    public void testGetNbrOfUnhandledFragaSvarForCareUnits() {

        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(new Object[] { "HSA1", 2L });
        queryResult.add(new Object[] { "HSA2", 4L });

        when(fragasvarRepositoryMock.countUnhandledGroupedByEnhetIds(Mockito.anyListOf(String.class))).thenReturn(queryResult);

        Map<String, Long> res = service.getNbrOfUnhandledFragaSvarForCareUnits(Arrays.asList("HSA1", "HSA2"));

        verify(fragasvarRepositoryMock).countUnhandledGroupedByEnhetIds(Mockito.anyListOf(String.class));

        assertNotNull(res);
        assertEquals(2, res.size());
    }

    @Test(expected = WebCertServiceException.class)
    public void intygWithoutFragaSvarDoesNotAcceptFraga() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.getIntygsReferens().setIntygsTyp("ts-bas");

        when(webcertFeatureServiceMock.isModuleFeatureActive(ModuleFeature.HANTERA_FRAGOR, "ts-bas")).thenReturn(false);

        service.processIncomingQuestion(fragaSvar);
        fail("Processing should have thrown an exception");
    }

    private WebCertUser createUser() {
        WebCertUser user = new WebCertUser();
        user.setRoles(getGrantedRole());
        user.setAuthorities(getGrantedPrivileges());
        user.setHsaId("testuser");
        user.setNamn("test userman");

        Vardenhet vardenhet = new Vardenhet("enhet", "Enhet");

        Vardgivare vardgivare = new Vardgivare("vardgivare", "Vardgivaren");
        vardgivare.getVardenheter().add(vardenhet);

        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }

    private Map<String, UserRole> getGrantedRole() {
        Map<String, UserRole> map = new HashMap<>();
        map.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        return map;
    }

    private Map<String, UserPrivilege> getGrantedPrivileges() {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());

        // convert list to map
        Map<String, UserPrivilege> privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
            @Override
            public String apply(UserPrivilege userPrivilege) {
                return userPrivilege.name();
            }
        });

        return privilegeMap;
    }

}
