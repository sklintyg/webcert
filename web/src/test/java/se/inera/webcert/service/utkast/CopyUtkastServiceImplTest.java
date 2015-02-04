package se.inera.webcert.service.utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.certificate.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.model.PersonSvar;
import se.inera.webcert.pu.services.PUService;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.service.intyg.dto.StatusType;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.utkast.dto.CreateNewDraftCopyResponse;
import se.inera.webcert.service.utkast.util.CreateIntygsIdStrategy;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class CopyUtkastServiceImplTest {

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    private static final String PATIENT_SSN = "19121212-1212";
    private static final String PATIENT_FNAME = "Adam";
    private static final String PATIENT_MNAME = "Bertil";
    private static final String PATIENT_LNAME = "Caesarsson";

    private static final String PATIENT_NEW_SSN = "19121212-1414";

    @Mock
    private UtkastRepository mockUtkastRepository;

    @Mock
    private PUService mockPUService;

    @Mock
    private CopyUtkastBuilder mockUtkastBuilder;
    
    @Mock
    private NotificationService mockNotificationService;

    @InjectMocks
    private CopyUtkastService copyService = new CopyUtkastServiceImpl();

    private HoSPerson hoSPerson;

    private se.inera.webcert.service.dto.Vardenhet vardenhet;

    @Before
    public void setup() {
        hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");
        hoSPerson.setBefattning("Befattning");
        hoSPerson.getSpecialiseringar().add("Ortoped");

        se.inera.webcert.service.dto.Vardgivare vardgivare = new se.inera.webcert.service.dto.Vardgivare();
        vardgivare.setHsaId("SE234234");
        vardgivare.setNamn("Vårdgivaren");

        vardenhet = new se.inera.webcert.service.dto.Vardenhet();
        vardenhet.setArbetsplatskod("00000");
        vardenhet.setNamn("Vårdenheten");
        vardenhet.setHsaId("SE234897348");
        vardenhet.setPostadress("Sjukvägen 1");
        vardenhet.setPostnummer("12345");
        vardenhet.setNamn("Testberga");
        vardenhet.setTelefonnummer("0123-456789");
        vardenhet.setEpost("ingen@ingen.se");
        vardenhet.setVardgivare(vardgivare);

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getHsaId());
        vardperson.setNamn(hoSPerson.getNamn());

        // utkast = createUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson);
        // signedUtkast = createUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
    }

    @Before
    public void expectCallToPUService() throws Exception {
        PersonSvar personSvar = new PersonSvar(new Person(PATIENT_SSN, "Adam", "Bertilsson", "Cedergren", "Testgatan 12", "12345", "Testberga"),
                PersonSvar.Status.FOUND);
        when(mockPUService.getPerson(PATIENT_SSN)).thenReturn(personSvar);
    }

    @Before
    public void expectSaveOfUtkast() {
        when(mockUtkastRepository.save(any(Utkast.class))).thenAnswer(new Answer<Utkast>() {
            @Override
            public Utkast answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Utkast) args[0];
            }
        });
    }

    @Test
    public void testCreateCopy() throws Exception {

        when(mockUtkastRepository.exists(INTYG_ID)).thenReturn(Boolean.FALSE);
        
        Utkast copy = createCopyUtkast();
        when(mockUtkastBuilder.populateCopyUtkastFromSignedIntyg(any(CreateNewDraftCopyRequest.class), any(Person.class))).thenReturn(copy);

        CreateNewDraftCopyRequest copyReq = buildCopyRequest();

        CreateNewDraftCopyResponse copyResp = copyService.createCopy(copyReq);

        assertNotNull(copyResp);
        assertEquals(INTYG_COPY_ID, copyResp.getNewDraftIntygId());
        assertEquals(INTYG_TYPE, copyResp.getNewDraftIntygType());
        
        verify(mockUtkastBuilder).populateCopyUtkastFromSignedIntyg(any(CreateNewDraftCopyRequest.class), any(Person.class));
        verify(mockUtkastRepository).save(any(Utkast.class));
    }

    @Test
    public void testCreateCopyWhenIntegrated() throws Exception {

        when(mockUtkastRepository.exists(INTYG_ID)).thenReturn(Boolean.TRUE);

        Utkast copy = createCopyUtkast();
        when(mockUtkastBuilder.populateCopyUtkastFromOrignalUtkast(any(CreateNewDraftCopyRequest.class), any(Person.class))).thenReturn(copy );

        CreateNewDraftCopyRequest copyReq = buildCopyRequest();
        copyReq.setDjupintegrerad(true);

        CreateNewDraftCopyResponse copyResp = copyService.createCopy(copyReq);

        assertNotNull(copyResp);
        assertEquals(INTYG_COPY_ID, copyResp.getNewDraftIntygId());
        assertEquals(INTYG_TYPE, copyResp.getNewDraftIntygType());

        verify(mockUtkastBuilder).populateCopyUtkastFromOrignalUtkast(any(CreateNewDraftCopyRequest.class), any(Person.class));
        verify(mockUtkastRepository).save(any(Utkast.class));
    }

    private Utkast createCopyUtkast() {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_COPY_ID);
        utkast.setIntygsTyp(INTYG_TYPE);
        utkast.setPatientPersonnummer(PATIENT_SSN);
        utkast.setPatientFornamn(PATIENT_FNAME);
        utkast.setPatientMellannamn(PATIENT_MNAME);
        utkast.setPatientEfternamn(PATIENT_LNAME);
        utkast.setModel(INTYG_JSON);

        return utkast;
    }

    private CreateNewDraftCopyRequest buildCopyRequest() {
        CreateNewDraftCopyRequest req = new CreateNewDraftCopyRequest();
        req.setOriginalIntygId(INTYG_ID);
        req.setTyp(INTYG_TYPE);
        req.setHosPerson(hoSPerson);
        req.setVardenhet(vardenhet);
        req.setPatientPersonnummer(PATIENT_SSN);
        return req;
    }

    private IntygContentHolder createIntygContentHolder() throws Exception {
        List<IntygStatus> status = new ArrayList<IntygStatus>();
        status.add(new IntygStatus(StatusType.RECEIVED, "MI", LocalDateTime.now()));
        status.add(new IntygStatus(StatusType.SENT, "FK", LocalDateTime.now()));
        Utlatande utlatande = new CustomObjectMapper().readValue(new ClassPathResource(
                "IntygDraftServiceImplTest/utlatande.json").getFile(), Utlatande.class);
        IntygContentHolder ich = new IntygContentHolder("<external-json/>", utlatande, status, false);
        return ich;
    }

    // testCreateNewDraftCopyPUtjanstFailed()

    // testCreateNewDraftCopyWithNewPersonnummer
}
