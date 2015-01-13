package se.inera.webcert.service.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.certificate.modules.support.api.dto.HoSPersonal;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.model.PersonSvar;
import se.inera.webcert.pu.services.PUService;
import se.inera.webcert.service.draft.dto.CreateNewDraftCopyRequest;
import se.inera.webcert.service.draft.dto.CreateNewDraftCopyResponse;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.util.CreateIntygsIdStrategy;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.intyg.dto.IntygStatus;
import se.inera.webcert.service.intyg.dto.StatusType;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class IntygDraftServiceImplTest {

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    private static final String PATIENT_SSN = "19121212-1212";

    private static final String PATIENT_NEW_SSN = "19121212-1414";

    @Mock
    private IntygRepository intygRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private LogService logService;

    @Mock
    private WebCertUserService userService;

    @Mock
    private IntygService intygService;

    @Mock
    private PUService puService;

    @Mock
    private NotificationService notificationService;

    @Spy
    private CreateIntygsIdStrategy mockIdStrategy = new CreateIntygsIdStrategy() {
        @Override
        public String createId() {
            return INTYG_COPY_ID;
        }
    };

    @InjectMocks
    private IntygDraftService draftService = new IntygDraftServiceImpl();

    private Intyg intygDraft;

    private Intyg intygSigned;

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

        intygDraft = createIntyg(INTYG_ID, INTYG_TYPE, IntygsStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson);
        intygSigned = createIntyg(INTYG_ID, INTYG_TYPE, IntygsStatus.SIGNED, INTYG_JSON, vardperson);
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
    public void testDeleteDraftThatIsUnsigned() {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
        ArgumentCaptor<NotificationRequestType> notificationRequestTypeArgumentCaptor = ArgumentCaptor.forClass(NotificationRequestType.class);

        draftService.deleteUnsignedDraft(INTYG_ID);

        verify(intygRepository).findOne(INTYG_ID);
        verify(intygRepository).delete(intygDraft);
        verify(notificationService).notify(notificationRequestTypeArgumentCaptor.capture());

        // Assert notification message
        NotificationRequestType notificationRequestType = notificationRequestTypeArgumentCaptor.getValue();
        assertEquals(INTYG_ID, notificationRequestType.getIntygsId());
        assertEquals(HandelseType.INTYGSUTKAST_RADERAT, notificationRequestType.getHandelse());
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatIsSigned() {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygSigned);

        draftService.deleteUnsignedDraft(INTYG_ID);

        verify(intygRepository).findOne(INTYG_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatDoesNotExist() {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(null);

        draftService.deleteUnsignedDraft(INTYG_ID);

        verify(intygRepository).findOne(INTYG_ID);
    }

    @Test
    public void testSaveAndValidateDraft() throws Exception {

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Arrays.asList(valMsg));
        WebCertUser user = createUser();

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(validationResponse);
        when(intygRepository.save(intygDraft)).thenReturn(intygDraft);
        when(mockModuleApi.isModelChanged(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getWebCertUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(
                new InternalModelResponse("{}"));

        ArgumentCaptor<NotificationRequestType> notificationRequestTypeArgumentCaptor = ArgumentCaptor.forClass(NotificationRequestType.class);

        DraftValidation res = draftService.saveAndValidateDraft(request);

        verify(intygRepository).save(any(Intyg.class));
        verify(notificationService).notify(notificationRequestTypeArgumentCaptor.capture());

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getMessages().size());

        // Assert notification message
        NotificationRequestType notificationRequestType = notificationRequestTypeArgumentCaptor.getValue();
        assertEquals(INTYG_ID, notificationRequestType.getIntygsId());
        assertEquals(HandelseType.INTYGSUTKAST_ANDRAT, notificationRequestType.getHandelse());

    }

    private WebCertUser createUser() {
        WebCertUser user = new WebCertUser();
        user.setHsaId("hsaId");
        user.setNamn("namn");
        List<String> tmp = new ArrayList<String>();
        tmp.add("Ortoped");
        user.setSpecialiseringar(tmp);
        user.setTitel("Befattning");
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setId("vardgivarid");
        vardgivare.setNamn("vardgivarnamn");
        user.setValdVardgivare(vardgivare);
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setId("enhetid");
        vardenhet.setNamn("enhetnamn");
        user.setValdVardenhet(vardenhet);
        return user;
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftThatIsSigned() {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygSigned);

        draftService.saveAndValidateDraft(buildSaveAndValidateRequest());

        verify(intygRepository).findOne(INTYG_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftWithExceptionInModule() throws Exception {

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();
        WebCertUser user = createUser();

        when(userService.getWebCertUser()).thenReturn(user);
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(new InternalModelResponse("{}"));
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenThrow(ModuleException.class);

        ArgumentCaptor<NotificationRequestType> notificationRequestTypeArgumentCaptor = ArgumentCaptor.forClass(NotificationRequestType.class);

        draftService.saveAndValidateDraft(request);
    }

    private SaveAndValidateDraftRequest buildSaveAndValidateRequest() {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();
        request.setIntygId(INTYG_ID);
        request.setDraftAsJson(INTYG_JSON);
        request.setSavedBy(hoSPerson);
        request.setAutoSave(false);
        return request;
    }

    @Test
    public void testCreateNewDraftCopy() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();

        when(intygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        PersonSvar personSvar = new PersonSvar(new Person(PATIENT_SSN, "Adam", "Bertilsson", "Cedergren", "Testgatan 12", "12345", "Testberga"), PersonSvar.Status.FOUND);
        when(puService.getPerson(PATIENT_SSN)).thenReturn(personSvar);

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);

        InternalModelResponse imr = new InternalModelResponse(INTYG_JSON);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateNewDraftHolder.class), any(InternalModelHolder.class))).thenReturn(imr);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(vdr);

        when(intygRepository.save(any(Intyg.class))).thenAnswer(new Answer<Intyg>() {
            @Override
            public Intyg answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Intyg) args[0];
            }
        });

        CreateNewDraftCopyRequest copyReq = buildCopyRequest();
        CreateNewDraftCopyResponse copyResp = draftService.createNewDraftCopy(copyReq);
        assertNotNull(copyResp);
        assertEquals(INTYG_COPY_ID, copyResp.getNewDraftIntygId());
        assertEquals(INTYG_TYPE, copyResp.getNewDraftIntygType());

        verify(mockIdStrategy).createId();
        verify(intygRepository).save(any(Intyg.class));
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

    @Test(expected = WebCertServiceException.class)
    public void testCreateNewDraftCopyPUtjanstFailed() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();


        when(intygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        when(puService.getPerson(PATIENT_SSN)).thenReturn(new PersonSvar(null, PersonSvar.Status.NOT_FOUND));

        CreateNewDraftCopyRequest copyReq = buildCopyRequest();
        draftService.createNewDraftCopy(copyReq);

    }

    @Test
    public void testCreateNewDraftCopyWithNewPersonnummer() throws Exception {

        IntygContentHolder ich = createIntygContentHolder();

        when(intygService.fetchIntygData(INTYG_ID, INTYG_TYPE)).thenReturn(ich);

        PersonSvar personSvar = new PersonSvar(new Person(PATIENT_SSN, "Adam", "Bertilsson", "Cedergren", "Testgatan 12", "12345", "Testberga"), PersonSvar.Status.FOUND);
        when(puService.getPerson(PATIENT_NEW_SSN)).thenReturn(personSvar);

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);

        InternalModelResponse imr = new InternalModelResponse(INTYG_JSON);
        when(mockModuleApi.createNewInternalFromTemplate(any(CreateNewDraftHolder.class), any(InternalModelHolder.class))).thenReturn(imr);

        ValidateDraftResponse vdr = new ValidateDraftResponse(ValidationStatus.VALID, new ArrayList<ValidationMessage>());
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(vdr);

        when(intygRepository.save(any(Intyg.class))).thenAnswer(new Answer<Intyg>() {
            @Override
            public Intyg answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (Intyg) args[0];
            }
        });

        CreateNewDraftCopyRequest copyReq = buildCopyRequest();
        copyReq.setNyttPatientPersonnummer(PATIENT_NEW_SSN);

        CreateNewDraftCopyResponse copyResp = draftService.createNewDraftCopy(copyReq);
        assertNotNull(copyResp);
        assertEquals(INTYG_COPY_ID, copyResp.getNewDraftIntygId());
        assertEquals(INTYG_TYPE, copyResp.getNewDraftIntygType());

        verify(mockIdStrategy).createId();
        verify(intygRepository).save(any(Intyg.class));
    }

    private CreateNewDraftCopyRequest buildCopyRequest() {
        CreateNewDraftCopyRequest req = new CreateNewDraftCopyRequest();
        req.setOriginalIntygId(INTYG_ID);
        req.setTyp(INTYG_TYPE);
        req.setHosPerson(hoSPerson);
        req.setVardenhet(vardenhet);
        return req;
    }

}
