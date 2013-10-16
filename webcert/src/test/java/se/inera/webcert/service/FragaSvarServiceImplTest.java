package se.inera.webcert.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
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
        unsortedList.add(buildFragaSvarFraga(1L, MAY, null));
        unsortedList.add(buildFragaSvarFraga(2L, DECEMBER, null));
        unsortedList.add(buildFragaSvarFraga(3L, null, JANUARY));
        unsortedList.add(buildFragaSvarFraga(4L, null, AUGUST));
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
        unsortedList.add(buildFragaSvarFraga(1L, MAY, null));
        unsortedList.add(buildFragaSvarFraga(2L, DECEMBER, null));
        unsortedList.add(buildFragaSvarFraga(3L, null, JANUARY));
        unsortedList.add(buildFragaSvarFraga(4L, null, AUGUST));
        when(fragasvarRepository.findByIntygsReferensIntygsId((Mockito.any(String.class)))).thenReturn(unsortedList);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> result = service.getFragaSvar("intygsid");

        assertEquals(4, result.size());

        assertEquals(2, (long) result.get(0).getInternReferens());
        assertEquals(4, (long) result.get(1).getInternReferens());
        assertEquals(1, (long) result.get(2).getInternReferens());
        assertEquals(3, (long) result.get(3).getInternReferens());

    }

    private FragaSvar buildFragaSvarFraga(Long id, LocalDateTime fragaSkickadDatum, LocalDateTime svarSkickadDatum) {
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

    @Test
    public void testGetFragaSvarForIntyg() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvarFraga(1L, DECEMBER, DECEMBER));
        fragaSvarList.add(buildFragaSvarFraga(2L, new LocalDateTime(), new LocalDateTime()));
        fragaSvarList.add(buildFragaSvarFraga(3L, JANUARY, JANUARY));

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
        fragaSvarList.add(buildFragaSvarFraga(1L, DECEMBER, DECEMBER));
        fragaSvarList.add(buildFragaSvarFraga(2L, new LocalDateTime(), new LocalDateTime()));
        fragaSvarList.add(buildFragaSvarFraga(3L, JANUARY, JANUARY));

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
    public void testSaveSvarOK() {
        FragaSvar fragaSvar = buildFragaSvarFraga(1L, new LocalDateTime(), new LocalDateTime());

        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(fragasvarRepository.save(fragaSvar)).thenReturn(fragaSvar);
        
        //mock ws ok response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.okResult());
        when(sendAnswerToFKClient.sendMedicalCertificateAnswer(any(AttributedURIType.class), any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);
        

        FragaSvar result = service.saveSvar(1L, "svarsText");

        verify(fragasvarRepository).findOne(1L);
        verify(webCertUserService).getWebCertUser();
        verify(fragasvarRepository).save(fragaSvar);
        verify(sendAnswerToFKClient).sendMedicalCertificateAnswer(any(AttributedURIType.class),any(SendMedicalCertificateAnswerType.class));

        assertEquals("svarsText", result.getSvarsText());
        assertEquals(Status.CLOSED, result.getStatus());
        assertNotNull(result.getSvarSkickadDatum());
    }

    @Test(expected = ExternalWebServiceCallFailedException.class)
    public void testSaveSvarWsError() {
        FragaSvar fragaSvar = buildFragaSvarFraga(1L, new LocalDateTime(), new LocalDateTime());

        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());
        when(fragasvarRepository.save(fragaSvar)).thenReturn(fragaSvar);
        
        //mock ws error response
        SendMedicalCertificateAnswerResponseType wsResponse = new SendMedicalCertificateAnswerResponseType();
        wsResponse.setResult(ResultOfCallUtil.failResult("some error"));
        when(sendAnswerToFKClient.sendMedicalCertificateAnswer(any(AttributedURIType.class), any(SendMedicalCertificateAnswerType.class))).thenReturn(wsResponse);
        

        service.saveSvar(1L, "svarsText");
    }
    
    @Test(expected = IllegalStateException.class)
    public void testSaveSvarWrongStateForAnswering() {
        FragaSvar fragaSvar = buildFragaSvarFraga(1L, new LocalDateTime(), new LocalDateTime());
        fragaSvar.setStatus(Status.ANSWERED);
        when(fragasvarRepository.findOne(1L)).thenReturn(fragaSvar);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        service.saveSvar(1L, "svarsText");
    }

    @Test(expected = RuntimeException.class)
    public void testSaveSvarNotAuthorizedForunit() {
        FragaSvar fragaSvar = buildFragaSvarFraga(1L, new LocalDateTime(), new LocalDateTime());
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
        user.setVardgivare(new Vardgivare());
        user.getVardgivare().getVardenheter().add(new Vardenhet("enhet", "Enhet"));
        return user;
    }

}
