package se.inera.webcert.service.draft;

import org.hibernate.ejb.criteria.expression.SearchedCaseExpression.WhenClause;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class IntygDraftServiceImplTest {
    
    private static final String INTYG_ID = "abc123";

    private static final String INTYG_JSON = "A bit of text represeting json";

    private static final String INTYG_TYPE = null;
    
    @Mock
    private IntygRepository intygRepository;
    
    @Mock
    private WebCertUserService webCertUserService;
    
    @InjectMocks
    private IntygDraftService draftService = new IntygDraftServiceImpl();
    
    private Intyg intyg;
    
    private WebCertUser webCertUser;
    
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
    }
    
    @Test
    public void testSaveAndValidateDraft() {
        
        when(intygRepository.findOne(INTYG_ID)).thenReturn(intyg);
        
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser);
                
        DraftValidation res = draftService.saveAndValidateDraft(INTYG_ID, INTYG_JSON);
        
        verify(intygRepository).save(any(Intyg.class));
        
        assertNotNull(res);
        assertTrue(res.isDraftValid());
    }
    
}
