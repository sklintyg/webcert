package se.inera.webcert.web.controller.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.test.TestIntygFactory;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;
import se.inera.webcert.web.service.WebCertUserService;

@RunWith(MockitoJUnitRunner.class)
public class IntygApiControllerTest {
    
    private static final String PNR = "19121212-1212";
    
    private static List<String> ENHET_IDS = Arrays.asList("ABC123","DEF456");
    
    private static List<Intyg> intygDrafts = TestIntygFactory.createListWithIntygDrafts();
    
    private static List<IntygItem> intygSigned = TestIntygFactory.createListWithIntygItems();
    
    @Mock
    private WebCertUserService webCertUserService = mock(WebCertUserService.class);
    
    @Mock
    private IntygService intygService = mock(IntygService.class);
    
    @Mock
    private IntygRepository intygRepository = mock(IntygRepository.class);
    
    @InjectMocks
    private IntygApiController intygCtrl = new IntygApiController();
    
    @Test
    public void testListIntyg() {
        
        WebCertUser user = mock(WebCertUser.class);
        
        when(webCertUserService.getWebCertUser()).thenReturn(user);
        when(user.getVardenheterIds()).thenReturn(ENHET_IDS);
        
        when(intygService.listIntyg(ENHET_IDS, PNR)).thenReturn(intygSigned);
        
        when(intygRepository.findDraftsByPatientPnrAndEnhetsId(ENHET_IDS, PNR)).thenReturn(intygDrafts);
        
        Response response = intygCtrl.listIntyg(PNR);
        
        List<ListIntygEntry> res = (List<ListIntygEntry>) response.getEntity();
        
        assertNotNull(res);
        assertEquals(4, res.size());
    }
}
