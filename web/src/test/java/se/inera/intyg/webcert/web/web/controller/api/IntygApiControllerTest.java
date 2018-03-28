/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.api;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.test.TestIntygFactory;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntygApiControllerTest {

    private static final Personnummer PNR = Personnummer.createPersonnummer("19121212-1212").get();

    private static final String ENHET_ID = "ABC123";

    private static final List<String> ENHET_IDS = Arrays.asList("ABC123", "DEF456");
    private static final List<UtkastStatus> DRAFT_STATUSES = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.DRAFT_INCOMPLETE);

    private static final Set<String> USER_INTYGSTYPER = new HashSet<>();

    private static List<Utkast> utkast = TestIntygFactory.createListWithUtkast();

    private static Pair<List<ListIntygEntry>, Boolean> intygItemListResponse = Pair.of(TestIntygFactory.createListWithIntygItems(), false);

    private WebCertUser user;

    @Mock
    private WebCertUserService webCertUserService = mock(WebCertUserService.class);

    @Mock
    private IntygService intygService = mock(IntygService.class);

    @Mock
    private UtkastRepository mockUtkastRepository = mock(UtkastRepository.class);

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    private IntygApiController intygCtrl = new IntygApiController();

    @Before
    public void setupExpectations() {
        mockUser();
        USER_INTYGSTYPER.clear();
        USER_INTYGSTYPER.add("fk7263");
        USER_INTYGSTYPER.add("ts-bas");
        USER_INTYGSTYPER.add("ts-diabetes");

        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
    }

    private void mockUser() {
        user = mock(WebCertUser.class);
        SelectableVardenhet vardenhet = mock(SelectableVardenhet.class);

        when(user.getIdsOfSelectedVardenhet()).thenReturn(ENHET_IDS);
        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Test
    public void testListIntyg() {

        // Mock call to Intygstjanst
        when(intygService.listIntyg(ENHET_IDS, PNR)).thenReturn(intygItemListResponse);

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
        Pair<List<ListIntygEntry>, Boolean> offlineIntygItemListResponse = Pair.of(TestIntygFactory.createListWithIntygItems(), true);

        // Mock call to Intygstjanst
        when(intygService.listIntyg(ENHET_IDS, PNR)).thenReturn(offlineIntygItemListResponse);

        Response response = intygCtrl.listDraftsAndIntygForPerson(PNR.getPersonnummer());

        @SuppressWarnings("unchecked")
        List<ListIntygEntry> res = (List<ListIntygEntry>) response.getEntity();

        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals("true", response.getHeaderString("offline_mode"));
    }

}
