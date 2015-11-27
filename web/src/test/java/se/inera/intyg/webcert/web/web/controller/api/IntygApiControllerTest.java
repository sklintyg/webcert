package se.inera.intyg.webcert.web.web.controller.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygItemListResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.test.TestIntygFactory;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

@RunWith(MockitoJUnitRunner.class)
public class IntygApiControllerTest {

    private static final Personnummer PNR = new Personnummer("19121212-1212");

    private static final String ENHET_ID = "ABC123";

    private static final List<String> ENHET_IDS = Arrays.asList("ABC123", "DEF456");
    private static final List<UtkastStatus> DRAFT_STATUSES = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.DRAFT_INCOMPLETE);

    private static final Set<String> USER_INTYGSTYPER = new HashSet<>();

    private static List<Utkast> utkast = TestIntygFactory.createListWithUtkast();

    private static IntygItemListResponse intygItemListResponse = TestIntygFactory
            .createIntygItemListResponse(TestIntygFactory.createListWithIntygItems(), false);

    private WebCertUser user;

    @Mock
    private WebCertUserService webCertUserService = mock(WebCertUserService.class);

    @Mock
    private IntygService intygService = mock(IntygService.class);

    @Mock
    private UtkastRepository mockUtkastRepository = mock(UtkastRepository.class);

    @InjectMocks
    private IntygApiController intygCtrl = new IntygApiController();

    @Before
    public void setupExpectations() {

        mockUser();
        USER_INTYGSTYPER.clear();
        USER_INTYGSTYPER.add("fk7263");
        USER_INTYGSTYPER.add("ts-bas");
        USER_INTYGSTYPER.add("ts-diabetes");
    }

    private void mockUser() {
        user = mock(WebCertUser.class);
        SelectableVardenhet vardenhet = mock(SelectableVardenhet.class);
        when(vardenhet.getId()).thenReturn(ENHET_ID);

        when(user.getValdVardenhet()).thenReturn(vardenhet);

        when(user.getIdsOfSelectedVardenhet()).thenReturn(ENHET_IDS);
        when(user.getValdVardenhet().getId()).thenReturn(ENHET_ID);

        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Test
    public void testListIntyg() {

        // Mock call to Intygstjanst
        when(intygService.listIntyg(ENHET_IDS, PNR)).thenReturn(intygItemListResponse);

        // Mock call to database
        when(mockUtkastRepository.findDraftsByPatientAndEnhetAndStatus(PNR.getPersonnummer(), ENHET_IDS, DRAFT_STATUSES, USER_INTYGSTYPER)).thenReturn(utkast);

        Response response = intygCtrl.listDraftsAndIntygForPerson(PNR.getPersonnummer());

        @SuppressWarnings("unchecked")
        List<ListIntygEntry> res = (List<ListIntygEntry>) response.getEntity();

        assertNotNull(res);
        assertEquals(2, res.size());
    }

    @Test
    public void testListIntygWhenUserHasNoAssignments() {
        when(user.getIdsOfSelectedVardenhet()).thenReturn(Collections.<String> emptyList());

        Response response = intygCtrl.listDraftsAndIntygForPerson(PNR.getPersonnummer());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        verifyZeroInteractions(intygService);
        verifyZeroInteractions(mockUtkastRepository);
    }

    @Test
    public void testListIntygOfflineMode() {
        IntygItemListResponse offlineIntygItemListResponse = TestIntygFactory.createIntygItemListResponse(TestIntygFactory.createListWithIntygItems(),
                true);

        // Mock call to Intygstjanst
        when(intygService.listIntyg(ENHET_IDS, PNR)).thenReturn(offlineIntygItemListResponse);

        // Mock call to database
        when(mockUtkastRepository.findDraftsByPatientAndEnhetAndStatus(PNR.getPersonnummer(), ENHET_IDS, DRAFT_STATUSES, USER_INTYGSTYPER)).thenReturn(utkast);

        Response response = intygCtrl.listDraftsAndIntygForPerson(PNR.getPersonnummer());

        @SuppressWarnings("unchecked")
        List<ListIntygEntry> res = (List<ListIntygEntry>) response.getEntity();

        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals("true", response.getHeaderString("offline_mode"));
    }

}
