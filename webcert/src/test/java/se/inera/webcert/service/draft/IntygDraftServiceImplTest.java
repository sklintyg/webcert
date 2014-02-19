package se.inera.webcert.service.draft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.modules.ModuleRestApiFactory;
import se.inera.webcert.modules.api.ModuleRestApi;
import se.inera.webcert.modules.api.dto.DraftValidationMessage;
import se.inera.webcert.modules.api.dto.DraftValidationResponse;
import se.inera.webcert.modules.api.dto.DraftValidationStatus;
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
    private ModuleRestApiFactory moduleRestApiFactory;

    @InjectMocks
    private IntygDraftService draftService = new IntygDraftServiceImpl();

    private Intyg intyg;

    private HoSPerson hoSPerson;
    
    private DraftValidationResponse draftValidationResponse;

    public IntygDraftServiceImplTest() {

    }

    @Before
    public void setupReturnValues() {
        this.intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYPE);
        intyg.setModel(INTYG_JSON);

        this.hoSPerson = new HoSPerson();
        hoSPerson.setHsaId("AAA");
        hoSPerson.setNamn("Dr Dengroth");

        this.draftValidationResponse = new DraftValidationResponse();
        draftValidationResponse.setStatus(DraftValidationStatus.INVALID);
        draftValidationResponse.getValidationErrors().add(
                new DraftValidationMessage("a.field.somewhere", "This is soooo wrong!"));

    }

    @Test
    public void testSaveAndValidateDraft() {

        when(intygRepository.findOne(INTYG_ID)).thenReturn(intyg);

        ModuleRestApi mockRestApi = mock(ModuleRestApi.class);
        when(moduleRestApiFactory.getModuleRestService(INTYG_TYPE)).thenReturn(mockRestApi);

        Response mockModuleResponse = mock(Response.class);
        when(mockRestApi.validate(INTYG_JSON)).thenReturn(mockModuleResponse);

        when(mockModuleResponse.getStatusInfo()).thenReturn(Response.Status.OK);
        when(mockModuleResponse.readEntity(DraftValidationResponse.class)).thenReturn(draftValidationResponse);

        SaveAndValidateDraftRequest request = buildSaveAndValidatRequest();
        
        DraftValidation res = draftService.saveAndValidateDraft(request);

        verify(intygRepository).save(any(Intyg.class));

        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getMessages().size());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSaveAndValidateDraftWithException() {
    
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intyg);
    
        ModuleRestApi mockRestApi = mock(ModuleRestApi.class);
        when(moduleRestApiFactory.getModuleRestService(INTYG_TYPE)).thenReturn(mockRestApi);
    
        Response mockModuleResponse = mock(Response.class);
        when(mockRestApi.validate(INTYG_JSON)).thenReturn(mockModuleResponse);
    
        // Oooops! Something failed in the module
        when(mockModuleResponse.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);
        
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
