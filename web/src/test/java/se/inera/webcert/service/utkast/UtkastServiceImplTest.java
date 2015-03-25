package se.inera.webcert.service.utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.HoSPersonal;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationMessageType;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.service.utkast.dto.DraftValidation;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.utkast.util.CreateIntygsIdStrategy;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class UtkastServiceImplTest {

    private static final String INTYG_ID = "abc123";
    private static final String INTYG_COPY_ID = "def456";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    @Mock
    private UtkastRepository mockUtkastRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private LogService logService;

    @Mock
    private WebCertUserService userService;

    @Mock
    private IntygService intygService;

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
    private UtkastService draftService = new UtkastServiceImpl();

    private Utkast utkast;

    private Utkast signedUtkast;

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

        utkast = createUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson);
        signedUtkast = createUtkast(INTYG_ID, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
    }

    private Utkast createUtkast(String intygId, String type, UtkastStatus status, String model, VardpersonReferens vardperson) {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        return utkast;
    }

    @Test
    public void testDeleteDraftThatIsUnsigned() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        draftService.deleteUnsignedDraft(INTYG_ID);

        verify(mockUtkastRepository).findOne(INTYG_ID);
        verify(mockUtkastRepository).delete(utkast);
        
        // Assert notification message
        verify(notificationService).sendNotificationForDraftDeleted(any(Utkast.class));
        
        // Assert pdl log
        verify(logService).logDeleteIntyg(any(LogRequest.class), any(WebCertUser.class));
    }

    @Test
    public void testLogPrintOfIntygAsDraft() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);

        draftService.logPrintOfDraftToPDL(INTYG_ID);
        
        // Assert pdl log
        verify(logService).logPrintOfIntygAsDraft(any(LogRequest.class), any(WebCertUser.class));
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatIsSigned() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);

        draftService.deleteUnsignedDraft(INTYG_ID);

        verify(mockUtkastRepository).findOne(INTYG_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testDeleteDraftThatDoesNotExist() {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(null);

        draftService.deleteUnsignedDraft(INTYG_ID);

        verify(mockUtkastRepository).findOne(INTYG_ID);
    }

    @Test
    public void testSaveAndValidateDraftFirstSave() throws Exception {

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Arrays.asList(valMsg));
        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(validationResponse);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(mockModuleApi.isModelChanged(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getWebCertUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(
                new InternalModelResponse("{}"));

        DraftValidation res = draftService.saveAndValidateDraft(request, true);

        verify(mockUtkastRepository).save(any(Utkast.class));
        
        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert pdl log
        verify(logService).logUpdateIntyg(any(LogRequest.class), any(WebCertUser.class));

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getMessages().size());
    }

    @Test
    public void testSaveAndValidateDraftSecondSave() throws Exception {

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", ValidationMessageType.OTHER, "This is soooo wrong!");
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Arrays.asList(valMsg));
        WebCertUser user = createUser();

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(validationResponse);
        when(mockUtkastRepository.save(utkast)).thenReturn(utkast);
        when(mockModuleApi.isModelChanged(any(String.class), any(String.class))).thenReturn(true);
        when(userService.getWebCertUser()).thenReturn(user);
        when(mockModuleApi.updateBeforeSave(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(
                new InternalModelResponse("{}"));

        DraftValidation res = draftService.saveAndValidateDraft(request, false);

        verify(mockUtkastRepository).save(any(Utkast.class));
        
        // Assert notification message
        verify(notificationService).sendNotificationForDraftChanged(any(Utkast.class));

        // Assert pdl log
        verifyZeroInteractions(logService);

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getMessages().size());
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

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);

        draftService.saveAndValidateDraft(buildSaveAndValidateRequest(), false);

        verify(mockUtkastRepository).findOne(INTYG_ID);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftWithExceptionInModule() throws Exception {

        ModuleApi mockModuleApi = mock(ModuleApi.class);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();
        WebCertUser user = createUser();

        when(userService.getWebCertUser()).thenReturn(user);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        when(mockModuleApi.updateBeforeSave(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(new InternalModelResponse("{}"));
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenThrow(ModuleException.class);

        draftService.saveAndValidateDraft(request, false);
    }

    private SaveAndValidateDraftRequest buildSaveAndValidateRequest() {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();
        request.setIntygId(INTYG_ID);
        request.setDraftAsJson(INTYG_JSON);
        request.setSavedBy(hoSPerson);
        request.setAutoSave(false);
        return request;
    }

}
