package se.inera.webcert.service.draft;

import javax.ws.rs.core.Response;

import org.hibernate.ejb.criteria.expression.SearchedCaseExpression.WhenClause;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.modules.ModuleRestApiFactory;
import se.inera.webcert.modules.api.ModuleRestApi;
import se.inera.webcert.modules.api.dto.CreateNewIntygModuleRequest;
import se.inera.webcert.modules.api.dto.DraftValidationMessage;
import se.inera.webcert.modules.api.dto.DraftValidationResponse;
import se.inera.webcert.modules.api.dto.DraftValidationStatus;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class IntygDraftServiceImplTest {
    
    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text represeting json";

    private static final String INTYG_TYPE = "fk7263";
    
    @Mock
    private IntygRepository intygRepository;
    
    @Mock
    private WebCertUserService webCertUserService;
    
    @Mock
    private ModuleRestApiFactory moduleRestApiFactory;
    
    @InjectMocks
    private IntygDraftService draftService = new IntygDraftServiceImpl();
    
    private Intyg intyg;
    
    private WebCertUser webCertUser;
    
    private DraftValidationResponse draftValidationResponse;
    
    public IntygDraftServiceImplTest() {

    }   
    
    @Before
    public void setupReturnValues() {
        this.intyg = new Intyg();
        intyg.setIntygsId(INTYG_ID);
        intyg.setIntygsTyp(INTYG_TYPE);
        intyg.setModel(INTYG_JSON);
        
        this.webCertUser = new WebCertUser();
        webCertUser.setHsaId("AAA");
        webCertUser.setNamn("Dr Dengroth");
        
        this.draftValidationResponse = new DraftValidationResponse();
        draftValidationResponse.setStatus(DraftValidationStatus.INVALID);
        draftValidationResponse.getMessages().add(new DraftValidationMessage("a.field.somewhere","This is soooo wrong!"));
        
    }
    
    @Test
    public void testSaveAndValidateDraft() {
        
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intyg);
        
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser);
        
        ModuleRestApi mockRestApi = mock(ModuleRestApi.class);
        when(moduleRestApiFactory.getModuleRestService(INTYG_TYPE)).thenReturn(mockRestApi);
        
        Response mockValidationResponse = mock(Response.class);
        when(mockRestApi.validate(INTYG_JSON)).thenReturn(mockValidationResponse);
        when(mockValidationResponse.readEntity(DraftValidationResponse.class)).thenReturn(draftValidationResponse);
                
        DraftValidation res = draftService.saveAndValidateDraft(INTYG_ID, INTYG_JSON);
        
        verify(intygRepository).save(any(Intyg.class));
        
        assertNotNull("An DraftValidation should be returned", res);
        assertFalse("Validation should fail", res.isDraftValid());
        assertEquals("Validation should have 1 message", 1, res.getMessages().size());
    }
    
}
