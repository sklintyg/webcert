package se.inera.webcert.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.Utlatande;
import se.inera.webcert.fkstub.util.ResultOfCallUtil;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Id;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Komplettering;
import se.inera.webcert.persistence.fragasvar.model.Status;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.security.Vardenhet;
import se.inera.webcert.security.Vardgivare;
import se.inera.webcert.security.WebCertUser;
import se.inera.webcert.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.webcert.sendmedicalcertificatequestion.v1.rivtabp20.SendMedicalCertificateQuestionResponderInterface;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.SendMedicalCertificateQuestionType;
import se.inera.webcert.web.service.WebCertUserService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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

    @InjectMocks
    private FragaSvarServiceImpl service;

    private LocalDateTime JANUARY = new LocalDateTime("2013-01-12T11:22:11");
    private LocalDateTime MAY = new LocalDateTime("2013-05-01T11:11:11");
    private LocalDateTime AUGUST = new LocalDateTime("2013-08-02T11:11:11");
    private LocalDateTime DECEMBER = new LocalDateTime("2013-12-11T10:22:00");

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByEnhetsIdSorting() {
        List<FragaSvar> unsortedList = new ArrayList<FragaSvar>();
        unsortedList.add(buildFragaSvar(1L, MAY, null));
        unsortedList.add(buildFragaSvar(2L, DECEMBER, null));
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
        unsortedList.add(buildFragaSvar(2L, DECEMBER, null));
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
        fragaSvarList.add(buildFragaSvar(1L, DECEMBER, DECEMBER));
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
        fragaSvarList.add(buildFragaSvar(1L, DECEMBER, DECEMBER));
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
    public void testSaveFragaOK() throws JsonParseException, JsonMappingException, IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        Utlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
                "FragaSvarServiceTest/utlatande.json").getFile(), Utlatande.class);
        when(intygService.fetchIntygCommonModel(fraga.getIntygsReferens().getIntygsId())).thenReturn(utlatande);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
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

        verify(intygService).fetchIntygCommonModel(any(String.class));
        verify(webCertUserService).getWebCertUser();
        verify(fragasvarRepository).save(any(FragaSvar.class));
        verify(sendQuestionToFKClient).sendMedicalCertificateQuestion(any(AttributedURIType.class),
                any(SendMedicalCertificateQuestionType.class));

        assertEquals(Status.PENDING_EXTERNAL_ACTION, capture.getValue().getStatus());
        assertEquals(utlatande.getSkapadAv().getVardenhet().getId().getExtension(), capture.getValue().getVardperson()
                .getEnhetsId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveFragaNoFrageText() throws JsonParseException, JsonMappingException, IOException {
        FragaSvar fraga = buildFraga(1L, null, Amne.OVRIGT, new LocalDateTime());
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveFragaNoAmne() throws JsonParseException, JsonMappingException, IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", null, new LocalDateTime());
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());
    }

    @Test(expected = RuntimeException.class)
    public void testSaveFragaNotAuthorizedForUnit() throws JsonParseException, JsonMappingException, IOException {
        FragaSvar fraga = buildFraga(1L, "frageText", Amne.OVRIGT, new LocalDateTime());

        // create mocked Utlatande from intygstjansten
        Utlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
                "FragaSvarServiceTest/utlatande.json").getFile(), Utlatande.class);
        utlatande.getSkapadAv().getVardenhet().getId().setExtension("no-auth-for-this-unit");
        when(intygService.fetchIntygCommonModel(fraga.getIntygsReferens().getIntygsId())).thenReturn(utlatande);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        ArgumentCaptor<FragaSvar> capture = ArgumentCaptor.forClass(FragaSvar.class);

        when(fragasvarRepository.save(capture.capture())).thenReturn(fraga);

        // test call
        service.saveNewQuestion(fraga.getIntygsReferens().getIntygsId(), fraga.getAmne(), fraga.getFrageText());

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
    public void testSaveSvarOK() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());

        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
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
        verify(fragasvarRepository).save(fragaSvar);
        verify(sendAnswerToFKClient).sendMedicalCertificateAnswer(any(AttributedURIType.class),
                any(SendMedicalCertificateAnswerType.class));

        assertEquals("svarsText", result.getSvarsText());
        assertEquals(Status.CLOSED, result.getStatus());
        assertNotNull(result.getSvarSkickadDatum());
    }

    @Test(expected = ExternalWebServiceCallFailedException.class)
    public void testSaveSvarWsError() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());

        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(fragasvarRepository.save(fragaSvar)).thenReturn(fragaSvar);

        // mock ws error response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.failResult("some error"));
        when(
                sendAnswerToFKClient.sendMedicalCertificateAnswer(any(AttributedURIType.class),
                        any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = IllegalStateException.class)
    public void testSaveSvarWrongStateForAnswering() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setStatus(Status.ANSWERED);
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = IllegalStateException.class)
    public void testSaveSvarForKompletteringAndNotDoctor() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setAmne(Amne.KOMPLETTERING_AV_LAKARINTYG);
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        WebCertUser nonDoctor = webCertUser();
        nonDoctor.setLakare(false);
        when(webCertUserService.getWebCertUser()).thenReturn(nonDoctor);

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = IllegalStateException.class)
    public void testSaveSvarForPaminnelse() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setAmne(Amne.PAMINNELSE);
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = RuntimeException.class)
    public void testSaveSvarNotAuthorizedForunit() {
        FragaSvar fragaSvar = buildFragaSvar(1L, new LocalDateTime(), new LocalDateTime());
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        WebCertUser webCertUser = webCertUser();
        webCertUser.getVardgivare().setVardenheter(new ArrayList<Vardenhet>()); // remove all medarbetaruppdrag
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser);

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveBadInput() {
        service.saveSvar(1L, null);
    }

    @Test(expected = RuntimeException.class)
    public void testSaveSvarIntygNotFound() {

        when(fragasvarRepository.findOne(1L)).thenReturn(null);
        service.saveSvar(1L, "svarsText");

    }

    private WebCertUser webCertUser() {
        WebCertUser user = new WebCertUser();
        user.setHsaId("testuser");
        user.setVardgivare(new Vardgivare());
        user.getVardgivare().getVardenheter().add(new Vardenhet("enhet", "Enhet"));
        return user;
    }

}
