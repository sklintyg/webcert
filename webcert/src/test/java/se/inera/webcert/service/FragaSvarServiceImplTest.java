package se.inera.webcert.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.integration.rest.dto.CertificateContentMeta;
import se.inera.certificate.integration.rest.dto.CertificateStatus;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.common.MinimalUtlatande;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Id;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarFilter;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.persistence.fragasvar.repository.LakarIdNamn;
import se.inera.webcert.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.webcert.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;
import se.inera.webcert.service.dto.IntygContentHolder;
import se.inera.webcert.service.dto.IntygMetadata;
import se.inera.webcert.service.dto.IntygStatus;
import se.inera.webcert.service.dto.UtlatandeCommonModelHolder;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class FragaSvarServiceImplTest {

    private static final Id PATIENT_ID = new Id("patiend-id-root", "19121212-1212");

    @Mock
    private FragaSvarRepository fragasvarRepository;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    SendMedicalCertificateAnswerResponderInterface sendAnswerToFKClient;

    @Mock
    SendMedicalCertificateQuestionResponderInterface sendQuestionToFKClient;

    @Mock
    IntygService intygService;

    @Mock
    IntygMetadata intygMetadataMock;

    @InjectMocks
    private FragaSvarServiceImpl service;

    private LocalDateTime JANUARY = new LocalDateTime("2013-01-12T11:22:11");
    private LocalDateTime MAY = new LocalDateTime("2013-05-01T11:11:11");
    private LocalDateTime AUGUST = new LocalDateTime("2013-08-02T11:11:11");
    private LocalDateTime DECEMBER_YEAR_9999 = new LocalDateTime("9999-12-11T10:22:00");

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByEnhetsIdSorting() {
        List<FragaSvar> unsortedList = new ArrayList<FragaSvar>();
        unsortedList.add(buildFragaSvar(1L, MAY, null));
        unsortedList.add(buildFragaSvar(2L, DECEMBER_YEAR_9999, null));
        unsortedList.add(buildFragaSvar(3L, null, JANUARY));
        unsortedList.add(buildFragaSvar(4L, null, AUGUST));
        when(fragasvarRepository.findByEnhetsId(Mockito.any(List.class))).thenReturn(unsortedList);

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
        when(fragasvarRepository.findByIntygsReferensIntygsId((Mockito.any(String.class)))).thenReturn(unsortedList);
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

        when(fragasvarRepository.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepository).findByIntygsReferensIntygsId("intyg-1");
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

        when(fragasvarRepository.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepository).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getWebCertUser();

        assertEquals(2, result.size());
        assertEquals(fragaSvarList.get(0), result.get(0));
        assertEquals(fragaSvarList.get(2), result.get(1));
    }

    @Test
    public void testSaveFragaOK() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());
        
        IntygContentHolder utlatandeCommonModelHolder = getIntygContentHolder();
        
        when(intygService.fetchExternalIntygData(fraga.getIntygsReferens().getIntygsId())).thenReturn(
                utlatandeCommonModelHolder);
        List<IntygStatus> list = Arrays.asList(new IntygStatus("SENT", "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);
        
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
        
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepository.save(capture.capture())).thenReturn(fraga);

        // mock ws ok response
        SendMedicalCertificateQuestionResponseType wsResponse = new SendMedicalCertificateQuestionResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(
                sendQuestionToFKClient.sendMedicalCertificateQuestion(any(AttributedURIType.class),
                        any(SendMedicalCertificateQuestionType.class))).thenReturn(wsResponse);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());

        verify(intygService).fetchExternalIntygData(any(String.class));
        verify(webCertUserService).getWebCertUser();
        verify(webCertUserService).isAuthorizedForUnit(anyString());
        verify(fragasvarRepository).save(any(FragaSvar.class));
        verify(sendQuestionToFKClient).sendMedicalCertificateQuestion(any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class));

        assertEquals(Status.PENDING_EXTERNAL_ACTION, capture.getValue().getStatus());
        assertEquals(utlatandeCommonModelHolder.getExternalModel().getSkapadAv().getVardenhet().getId().getExtension(), capture.getValue().getVardperson()
                .getEnhetsId());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaNotSentToFK() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        when(intygService.fetchExternalIntygData(fraga.getIntygsReferens().getIntygsId())).thenReturn(
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
        when(intygService.fetchExternalIntygData(fraga.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());

        List<IntygStatus> list = Arrays.asList(new IntygStatus("SENT", "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(false);
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);

        when(fragasvarRepository.save(capture.capture())).thenReturn(fraga);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());

    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveFragaOnRevokedCertificate() throws IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        when(intygService.fetchExternalIntygData(fraga.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());


        List<IntygStatus> list = Arrays.asList(new IntygStatus("SENT", "FK", null), new IntygStatus("CANCELLED", "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);

        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
    
        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());

    }
    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarOnRevokedCertificate() throws IOException {
        FragaSvar svar = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());
        svar.setSvarsText("Svar på ogiltigt intyg");
        when(fragasvarRepository.findOne(1L)).thenReturn(svar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        List<IntygStatus> list = Arrays.asList(new IntygStatus("SENT", "FK", null), new IntygStatus("CANCELLED", "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);

        when(intygService.fetchExternalIntygData(svar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
    
        // test call
        service.saveSvar(svar.getInternReferens(), svar.getSvarsText());

    }
    @Test
    public void testSetVidareBefordradOK() {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());
        // set it to false initially
        fraga.setVidarebefordrad(false);

        when(fragasvarRepository.findOne(any(Long.class))).thenReturn(fraga);

        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepository.save(capture.capture())).thenReturn(fraga);

        // test call
        service.setDispatchState(fraga.getInternReferens(), true);

        verify(fragasvarRepository).findOne(any(Long.class));
        verify(fragasvarRepository).save(any(FragaSvar.class));

        assertEquals(true, capture.getValue().getVidarebefordrad());
    }

    @Test
    public void testSaveSvarOK() throws JsonParseException, JsonMappingException, IOException {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
       
        when(intygService.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());
   
        List<IntygStatus> list = Arrays.asList(new IntygStatus("SENT", "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);
        
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
        when(fragasvarRepository.save(fragaSvar)).thenReturn(fragaSvar);

        // mock ws ok response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(
                sendAnswerToFKClient.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                        any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        FragaSvar result = service.saveSvar(1L, "svarsText");

        verify(fragasvarRepository).findOne(1L);
        verify(webCertUserService).getWebCertUser();
        verify(webCertUserService).isAuthorizedForUnit(anyString());
        verify(fragasvarRepository).save(fragaSvar);
        verify(sendAnswerToFKClient).sendMedicalCertificateAnswer(any(AttributedURIType.class),
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

        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(intygService.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
        when(fragasvarRepository.save(fragaSvar)).thenReturn(fragaSvar);

        // mock ws error response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.failResult("some error"));
        when(
                sendAnswerToFKClient.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                        any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarWrongStateForAnswering() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setStatus(Status.ANSWERED);
     

        when(intygService.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());

        List<IntygStatus> list = Arrays.asList(new IntygStatus("SENT", "FK", null));
        when(intygMetadataMock.getStatuses()).thenReturn(list);
        
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
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
        
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(intygService.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
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
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(intygService.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
                getIntygContentHolder());
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveSvarNotAuthorizedForunit() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(intygService.fetchExternalIntygData(fragaSvar.getIntygsReferens().getIntygsId())).thenReturn(
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

        when(fragasvarRepository.findOne(1L)).thenReturn(null);
        service.saveSvar(1L, "svarsText");

    }
    
    @Test
    public void testCloseAshandledOk() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare("WC");
        fragaSvar.setFrageText("Fråga till FK");
        fragaSvar.setStatus(Status.PENDING_EXTERNAL_ACTION);
        
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepository.save(capture.capture())).thenReturn(fragaSvar);
        
        service.closeQuestionAsHandled(1L);
        
        verify(fragasvarRepository).findOne(1L);
        verify(fragasvarRepository).save(any(FragaSvar.class));
        assertEquals(Status.CLOSED,capture.getValue().getStatus());
    }
    
    @Test(expected = WebCertServiceException.class)
    public void testCloseAshandledNotFound() {
        when(fragasvarRepository.findOne(1L)).thenReturn(null);
        service.closeQuestionAsHandled(1L);
    }
    
    @Test
    public void testOpenAsUnhandledOk() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setFrageStallare("WC");
        fragaSvar.setFrageText("Fråga till FK");
        fragaSvar.setStatus(Status.CLOSED);
        
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepository.save(capture.capture())).thenReturn(fragaSvar);
        
        service.openQuestionAsUnhandled(1L);
        
        verify(fragasvarRepository).findOne(1L);
        verify(fragasvarRepository).save(any(FragaSvar.class));
        assertEquals(Status.PENDING_EXTERNAL_ACTION,capture.getValue().getStatus());
    }
    
    
    @Test(expected = WebCertServiceException.class)
    public void testOpenAsUnhandledNotFound() {
        when(fragasvarRepository.findOne(1L)).thenReturn(null);
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
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(fragasvarRepository.save(capture.capture())).thenReturn(fragaSvar);
        
        service.openQuestionAsUnhandled(1L);
    }

    @Test
    public void testVerifyEnhetsAuthOK() {
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
        service.verifyEnhetsAuth("enhet");

        verify(webCertUserService).isAuthorizedForUnit(anyString());
    }

    @Test(expected = WebCertServiceException.class)
    public void testVerifyEnhetsAuthFail() {
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(false);
        service.verifyEnhetsAuth("<doesnt-exist>");

    }

    @Test
    public void testGetFragaSvarByFilterCountOK() {
        WebCertUser webCertUser = webCertUser();
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(webCertUser.getVardgivare().get(0).getVardenheter().get(0).getId());

        when(fragasvarRepository.filterCountFragaSvar(Mockito.any(FragaSvarFilter.class))).thenReturn(42);
        int result = service.getFragaSvarByFilterCount(filter);
        assertEquals(42, result);

        verify(webCertUserService).isAuthorizedForUnit(anyString());
        verify(fragasvarRepository).filterCountFragaSvar(filter);
    }

    @Test(expected = WebCertServiceException.class)
    public void testGetFragaSvarByFilterCountAuthFail() {
        WebCertUser webCertUser = webCertUser();
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser);
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId("no-auth-unit");
        service.getFragaSvarByFilterCount(filter);
    }

    @Test
    public void testGetFragaSvarByFilterOK() {
        WebCertUser webCertUser = webCertUser();
        when(webCertUserService.isAuthorizedForUnit(any(String.class))).thenReturn(true);
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(webCertUser.getVardgivare().get(0).getVardenheter().get(0).getId());

        List<FragaSvar> queryResult = new ArrayList<FragaSvar>();
        queryResult.add(buildFragaSvar(1L, MAY, null));
        queryResult.add(buildFragaSvar(2L, MAY, null));

        when(fragasvarRepository.filterFragaSvar(any(FragaSvarFilter.class), anyInt(), anyInt())).thenReturn(
                queryResult);
        List<FragaSvar> result = service.getFragaSvarByFilter(filter, 10, 20);

        verify(webCertUserService).isAuthorizedForUnit(anyString());
        verify(fragasvarRepository).filterFragaSvar(any(FragaSvarFilter.class), anyInt(), anyInt());

        assertEquals(2, result.size());
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

        when(fragasvarRepository.findDistinctFragaSvarHsaIdByEnhet(anyString())).thenReturn(queryResult);
        List<LakarIdNamn> result = service.getFragaSvarHsaIdByEnhet(enhetsId);
        ArgumentCaptor<String> capture = ArgumentCaptor.forClass(String.class);
        verify(webCertUserService).isAuthorizedForUnit(capture.capture());

        verify(fragasvarRepository).findDistinctFragaSvarHsaIdByEnhet(anyString());
        assertEquals(enhetsId, capture.getValue());
        assertEquals(4, result.size());
    }

    private WebCertUser webCertUser() {
        WebCertUser user = new WebCertUser();
        user.setHsaId("testuser");
        user.setNamn("test userman");

        Vardenhet vardenhet = new Vardenhet("enhet", "Enhet");
        
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.getVardenheter().add(vardenhet);
        
        user.setVardgivare(Arrays.asList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }

}
