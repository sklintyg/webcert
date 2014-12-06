package se.inera.webcert.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.common.MinimalUtlatande;
import se.inera.certificate.modules.support.feature.ModuleFeature;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.ifv.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.fragasvar.model.*;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarFilter;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.feature.WebcertFeatureService;
import se.inera.webcert.service.fragasvar.FragaSvarServiceImpl;
import se.inera.webcert.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.webcert.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygMetadata;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.service.intyg.dto.StatusType;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.util.ReflectionUtils;
import se.inera.webcert.web.service.WebCertUserService;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FragaSvarServiceImplTest {

    private static final Id PATIENT_ID = new Id("patiend-id-root", "19121212-1212");

    @Mock
    private FragaSvarRepository fragasvarRepositoryMock;

    @Mock
    private SendMedicalCertificateAnswerResponderInterface sendAnswerToFKClientMock;

    @Mock
    private SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClientMock;

    @Mock
    private IntygService intygServiceMock;

    @Mock
    private IntygMetadata intygMetadataMock;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private WebcertFeatureService webcertFeatureServiceMock;

    @Mock
    private NotificationService notificationServiceMock;

    @Mock
    private Logger loggerMock;

    @InjectMocks
    private FragaSvarServiceImpl service;

    private LocalDateTime JANUARY = new LocalDateTime("2013-01-12T11:22:11");
    private LocalDateTime MAY = new LocalDateTime("2013-05-01T11:11:11");
    private LocalDateTime AUGUST = new LocalDateTime("2013-08-02T11:11:11");
    private LocalDateTime DECEMBER_YEAR_9999 = new LocalDateTime("9999-12-11T10:22:00");

    @Before
    public void setUpLoggerFactory() throws Exception {
        ReflectionUtils.setStaticFinalAttribute(FragaSvarServiceImpl.class, "LOG", loggerMock);
    }

    @Before
    public void setupCommonBehaviour() {
        when(webcertFeatureServiceMock.isModuleFeatureActive(eq(ModuleFeature.HANTERA_FRAGOR), anyString())).thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByEnhetsIdSorting() {
        List<FragaSvar> unsortedList = new ArrayList<FragaSvar>();
        unsortedList.add(buildFragaSvar(1L, MAY, null));
        unsortedList.add(buildFragaSvar(2L, DECEMBER_YEAR_9999, null));
        unsortedList.add(buildFragaSvar(3L, null, JANUARY));
        unsortedList.add(buildFragaSvar(4L, null, AUGUST));
        when(fragasvarRepositoryMock.findByEnhetsId(Mockito.any(List.class))).thenReturn(unsortedList);

        List<FragaSvar> result = service.getFragaSvar(Arrays.asList("123"));

        assertEquals(4, result.size());

        assertEquals(2, (long) result.get(0).getInternReferens());
        assertEquals(4, (long) result.get(1).getInternReferens());
        assertEquals(1, (long) result.get(2).getInternReferens());
        assertEquals(3, (long) result.get(3).getInternReferens());

    }

    @Test
    public void testFindByIntygSorting() {
        List<FragaSvar> unsortedList = new ArrayList<FragaSvar>();
        unsortedList.add(buildFragaSvar(1L, MAY, null));
        unsortedList.add(buildFragaSvar(2L, DECEMBER_YEAR_9999, null));
        unsortedList.add(buildFragaSvar(3L, null, JANUARY));
        unsortedList.add(buildFragaSvar(4L, null, AUGUST));
        when(fragasvarRepositoryMock.findByIntygsReferensIntygsId((Mockito.any(String.class)))).thenReturn(unsortedList);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

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
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getWebCertUser();

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
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepositoryMock).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getWebCertUser();

        assertEquals(2, result.size());
        assertEquals(fragaSvarList.get(0), result.get(0));
        assertEquals(fragaSvarList.get(2), result.get(1));
    }

    @Test
    public void testSaveFragaOK() throws IOException {

        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // Setup when - given -then

        IntygContentHolder utlatandeCommonModelHolder = getIntygContentHolder();
        when(intygServiceMock.fetchExternalIntygData(fraga.getIntygsReferens().getIntygsId())).thenReturn(utlatandeCommonModelHolder);

        List<IntygStatus> list = Arrays.asList(new IntygStatus(StatusType.SENT, "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);

        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fraga);

        // mock ws ok response
        SendMedicalCertificateQuestionResponseType wsResponse = new SendMedicalCertificateQuestionResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(sendQuestionToFKClientMock.sendMedicalCertificateQuestion(
                any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class))).thenReturn(wsResponse);

        ArgumentCaptor<NotificationRequestType> notificationRequestTypeArgumentCaptor = ArgumentCaptor.forClass(NotificationRequestType.class);

        // Do test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());

        // Verify execution

        verify(intygServiceMock).fetchExternalIntygData(any(String.class));
        verify(webCertUserService).getWebCertUser();
        verify(webCertUserService).isAuthorizedForUnit(anyString());
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        verify(sendQuestionToFKClientMock).sendMedicalCertificateQuestion(any(AttributedURIType.class), any(SendMedicalCertificateQuestionType.class));
        verify(notificationServiceMock).notify(notificationRequestTypeArgumentCaptor.capture());

        assertEquals(Status.PENDING_EXTERNAL_ACTION, capture.getValue().getStatus());
        assertEquals(utlatandeCommonModelHolder.getExternalModel().getSkapadAv().getVardenhet().getId().getExtension(), capture.getValue().getVardperson()
                .getEnhetsId());

        // Assert notification message
        NotificationRequestType notificationRequestType = notificationRequestTypeArgumentCaptor.getValue();
        assertEquals(fraga.getIntygsReferens().getIntygsId(), notificationRequestType.getIntygsId());
        assertEquals(HandelseType.FRAGA_TILL_FK, notificationRequestType.getHandelse());

    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotSentToFK() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchExternalIntygData(fraga.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());

        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNoFrageText() throws IOException {
        FragaSvar fraga = buildFraga(1L, null, Amne.OVRIGT, new LocalDateTime());
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNoAmne() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", null, new LocalDateTime());
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotAuthorizedForUnit() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchExternalIntygData(fraga.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());

        List<IntygStatus> list = Arrays.asList(new IntygStatus(StatusType.SENT, "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(false);
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);

        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fraga);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());

    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaOnRevokedCertificate() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        when(intygServiceMock.fetchExternalIntygData(fraga.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());


        List<IntygStatus> list = Arrays.asList(new IntygStatus(StatusType.SENT, "FK", null), new IntygStatus(StatusType.CANCELLED, "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);

        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());

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
    public void testSaveSvarOK() throws JsonParseException, JsonMappingException, IOException {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());

        when(intygServiceMock.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());

        List<IntygStatus> list = Arrays.asList(new IntygStatus(StatusType.SENT, "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);

        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
        when(fragasvarRepositoryMock.save(fragaSvar)).thenReturn(fragaSvar);

        // mock ws ok response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(
                sendAnswerToFKClientMock.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                        any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        FragaSvar result = service.saveSvar(1L, "svarsText");

        verify(fragasvarRepositoryMock).findOne(1L);
        verify(webCertUserService).getWebCertUser();
        verify(webCertUserService).isAuthorizedForUnit(anyString());
        verify(fragasvarRepositoryMock).save(fragaSvar);
        verify(sendAnswerToFKClientMock).sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class));

        assertEquals("svarsText", result.getSvarsText());
        assertEquals(Status.CLOSED, result.getStatus());
        assertNotNull(result.getSvarSkickadDatum());
    }

    private IntygContentHolder getIntygContentHolder() {
        return new IntygContentHolder("<external-json/>", getUtlatande(), intygMetadataMock);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWsError() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
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
    public void testSaveSvarWrongStateForAnswering() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setStatus(Status.ANSWERED);


        when(intygServiceMock.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());

        List<IntygStatus> list = Arrays.asList(new IntygStatus(StatusType.SENT, "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);

        service.saveSvar(1L, "svarsText");
    }

    private Utlatande getUtlatande()  {
     // create mocked Utlatande from intygstjansten
        try {
            return new CustomObjectMapper().readValue(new ClassPathResource(
                    "FragaSvarServiceTest/utlatande.json").getFile(), MinimalUtlatande.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarForKompletteringAndNotDoctor() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);

        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());
        WebCertUser nonDoctor = webCertUser();
        nonDoctor.setLakare(false);
        when(webCertUserService.getWebCertUser()).thenReturn(nonDoctor);

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarForPaminnelse() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setAmne(Amne.PAMINNELSE);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarNotAuthorizedForunit() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(intygServiceMock.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(false);

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
    public void testCloseAshandledOk() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare("WC");
        fragaSvar.setFrageText("Fråga till FK");
        fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.closeQuestionAsHandled(1L);

        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.CLOSED,capture.getValue().getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void testCloseAshandledNotFound() {
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(null);
        service.closeQuestionAsHandled(1L);
    }

    @Test
    public void testOpenAsUnhandledOk() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare("WC");
        fragaSvar.setFrageText("Fråga till FK");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);

        verify(fragasvarRepositoryMock).findOne(1L);
        verify(fragasvarRepositoryMock).save(any(FragaSvar.class));
        assertEquals(Status.PENDING_EXTERNAL_ACTION,capture.getValue().getStatus());
    }


    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledNotFound() {
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(null);
        service.openQuestionAsUnhandled(1L);
    }

    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledInvalidState() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare("FK");
        fragaSvar.setFrageText("Fråga från FK");
        fragaSvar.setSvarsText("Svar till FK från WC");
        fragaSvar.setStatus(Status.CLOSED);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepositoryMock.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepositoryMock.save(capture.capture())).thenReturn(fragaSvar);

        service.openQuestionAsUnhandled(1L);
    }

/*    @Test
    public void testVerifyEnhetsAuthOK() {
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
        service.verifyEnhetsAuth("enhet");

        verify(webCertUserService).isAuthorizedForUnit(anyString());
    }

    @Test(expected = WebCertServiceException.class)
    public void testVerifyEnhetsAuthFail() {
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(false);
        service.verifyEnhetsAuth("<doesnt-exist>");

    }*/

    @Test(expected = WebCertServiceException.class)
    public void testFilterFragaSvarWithAuthFail() {
        WebCertUser webCertUser = webCertUser();
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser);

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId("no-auth");

        service.filterFragaSvar(params);
    }

    @Test
    public void testFilterFragaSvarWithEnhetsIdAsParam() {

        WebCertUser webCertUser = webCertUser();
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);

        List<FragaSvar> queryResults = new ArrayList<FragaSvar>();
        queryResults.add(buildFragaSvar(1L, MAY, null));
        queryResults.add(buildFragaSvar(2L, MAY, null));

        when(fragasvarRepositoryMock.filterFragaSvar(any(FragaSvarFilter.class))).thenReturn(queryResults);
        when(fragasvarRepositoryMock.filterCountFragaSvar(any(FragaSvarFilter.class))).thenReturn(queryResults.size());

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();
        params.setEnhetId(webCertUser.getValdVardenhet().getId());

        QueryFragaSvarResponse response = service.filterFragaSvar(params);

        verify(webCertUserService).isAuthorizedForUnit(anyString());

        verify(fragasvarRepositoryMock).filterFragaSvar(any(FragaSvarFilter.class));
        verify(fragasvarRepositoryMock).filterCountFragaSvar(any(FragaSvarFilter.class));

        assertNotNull(response);
        assertEquals(2, response.getResults().size());
    }

    @Test
    public void testFilterFragaSvarWithNoEnhetsIdAsParam() {

        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> queryResults = new ArrayList<FragaSvar>();
        queryResults.add(buildFragaSvar(1L, MAY, null));
        queryResults.add(buildFragaSvar(2L, MAY, null));

        when(fragasvarRepositoryMock.filterFragaSvar(any(FragaSvarFilter.class))).thenReturn(queryResults);
        when(fragasvarRepositoryMock.filterCountFragaSvar(any(FragaSvarFilter.class))).thenReturn(queryResults.size());

        QueryFragaSvarParameter params = new QueryFragaSvarParameter();

        QueryFragaSvarResponse response = service.filterFragaSvar(params);

        verify(webCertUserService).getWebCertUser();

        verify(fragasvarRepositoryMock).filterFragaSvar(any(FragaSvarFilter.class));
        verify(fragasvarRepositoryMock).filterCountFragaSvar(any(FragaSvarFilter.class));

        assertNotNull(response);
        assertEquals(2, response.getResults().size());
    }

    @Test
    public void testGetMDByEnhetsIdOK() {
        String enhetsId = "enhet";
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);

        List<Object[]> queryResult = new ArrayList<Object[]>();
        queryResult.add(new Object[] { "HSA-1_ID", "NAMN1" });
        queryResult.add(new Object[] { "HSA-2_ID", "NAMN2" });
        queryResult.add(new Object[] { "HSA-3_ID", "NAMN3" });
        queryResult.add(new Object[] { "HSA-4_ID", "NAMN4" });

        when(fragasvarRepositoryMock.findDistinctFragaSvarHsaIdByEnhet(Matchers.anyListOf(String.class))).thenReturn(queryResult);
        List<Lakare> result = service.getFragaSvarHsaIdByEnhet(enhetsId);
        ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
        verify(webCertUserService).isAuthorizedForUnit(capture.capture());

        verify(fragasvarRepositoryMock).findDistinctFragaSvarHsaIdByEnhet(Matchers.anyListOf(String.class));
        assertEquals(enhetsId, capture.getValue());
        assertEquals(4, result.size());
    }

    @Test
    public void testGetNbrOfUnhandledFragaSvarForCareUnits() {

        List<Object[]> queryResult = new ArrayList<Object[]>();
        queryResult.add(new Object[] { "HSA1", 2L });
        queryResult.add(new Object[] { "HSA2", 4L });

        when(fragasvarRepositoryMock.countUnhandledGroupedByEnhetIds(Mockito.anyListOf(String.class))).thenReturn(queryResult);

        Map<String, Long> res = service.getNbrOfUnhandledFragaSvarForCareUnits(Arrays.asList("HSA1","HSA2"));

        verify(fragasvarRepositoryMock).countUnhandledGroupedByEnhetIds(Mockito.anyListOf(String.class));

        assertNotNull(res);
        assertEquals(2, res.size());
    }

    private WebCertUser webCertUser() {
        WebCertUser user = new WebCertUser();
        user.setHsaId("testuser");
        user.setNamn("test userman");

        Vardenhet vardenhet = new Vardenhet("enhet", "Enhet");

        Vardgivare vardgivare = new Vardgivare("vardgivare", "Vardgivaren");
        vardgivare.getVardenheter().add(vardenhet);

        user.setVardgivare(Arrays.asList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }

    @Test(expected = WebCertServiceException.class)
    public void intygWithoutFragaSvarDoesNotAcceptFraga() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.getIntygsReferens().setIntygsTyp("ts-bas");

        when(webcertFeatureServiceMock.isModuleFeatureActive(ModuleFeature.HANTERA_FRAGOR, "ts-bas")).thenReturn(false);

        service.processIncomingQuestion(fragaSvar);
        fail("Processing should have thrown an exception");
    }
}
