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
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.exception.WebCertServiceException;

@RunWith(MockitoJUnitRunner.class)
public class IntygDraftServiceImplTest {

    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text represeting json";

    private static final String INTYG_TYPE = "fk7263";

    @Mock
    private IntygRepository intygRepository;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @InjectMocks
    private IntygDraftService draftService = new IntygDraftServiceImpl();

    private Intyg intyg;

    private HoSPerson hoSPerson;
    
    public IntygDraftServiceImplTest() {

    }

    @Before
    public void setup() {
        this.intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYPE);
        intyg.setModel(INTYG_JSON);

        this.hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");

    }

    @Test
    public void testSaveAndValidateDraft() throws Exception {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intyg);
        
        ModuleApi mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
        
        ValidationMessage valMsg = new ValidationMessage("a.field.somewhere", "This is soooo wrong!");
        
        ValidateDraftResponse validationResponse = new ValidateDraftResponse(ValidationStatus.INVALID, Arrays.asList(valMsg));
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenReturn(validationResponse);
        
        SaveAndValidateDraftRequest request = buildSaveAndValidatRequest();
        
        DraftValidation res = draftService.saveAndValidateDraft(request);

        verify(intygRepository).save(any(Intyg.class));

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getMessages().size());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftWithException() throws Exception {
    
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intyg);
    
        ModuleApi mockModuleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(INTYG_TYPE)).thenReturn(mockModuleApi);
    
        // Oooops! Something failed in the module
        when(mockModuleApi.validateDraft(any(InternalModelHolder.class))).thenThrow(ModuleException.class);
        
        SaveAndValidateDraftRequest request = buildSaveAndValidatRequest();
        
        draftService.saveAndValidateDraft(request);
    }

    private SaveAndValidateDraftRequest buildSaveAndValidatRequest() {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();
        request.setIntygId(INTYG_ID);
        request.setDraftAsJson(INTYG_JSON);
        request.setSavedBy(hoSPerson);
        return request;
    }

}
