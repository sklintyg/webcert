package se.inera.webcert.service.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.api.ModuleApi;
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
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class IntygDraftServiceImplTest {

    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text representing json";

    private static final String INTYG_TYPE = "fk7263";

    @Mock
    private IntygRepository intygRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private LogService logService;

    @Mock
    private WebCertUserService userService;

    @InjectMocks
    private IntygDraftService draftService = new IntygDraftServiceImpl();

    private Intyg intygDraft;

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
                
        draftService.deleteUnsignedDraft(INTYG_ID);
        
        verify(intygRepository).findOne(INTYG_ID);
        verify(intygRepository).delete(intygDraft);
        verify(logService).logDeleteOfDraft(any(LogRequest.class));
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

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
        
        ModuleApi mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", "This is soooo wrong!");
        
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Arrays.asList(valMsg));
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(validationResponse);

        when(intygRepository.save(intygDraft)).thenReturn(intygDraft);

        WebCertUser user = createUser();

        when(userService.getWebCertUser()).thenReturn(user);
        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();

        when(mockModuleApi.updateInternal(any(InternalModelHolder.class), any(HoSPersonal.class))).thenReturn(new InternalModelResponse("{}"));

        DraftValidation res = draftService.saveAndValidateDraft(request);

        verify(intygRepository).save(any(Intyg.class));

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getMessages().size());
    }

    private WebCertUser createUser() {
        WebCertUser user = new WebCertUser();
        user.setHsaId("hsaId");
        user.setNamn("namn");
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
    
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intygDraft);
    
        ModuleApi mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);

        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenThrow(ModuleException.class);

        SaveAndValidateDraftRequest request = buildSaveAndValidateRequest();
        draftService.saveAndValidateDraft(request);
    }

    private SaveAndValidateDraftRequest buildSaveAndValidateRequest() {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();
        request.setIntygId(INTYG_ID);
        request.setDraftAsJson(INTYG_JSON);
        request.setSavedBy(hoSPerson);
        return request;
    }

}
