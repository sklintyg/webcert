/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.arende;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.ArendeListItemProjection;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.list.PaginationAndLoggingService;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.message.MessageImportService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@ExtendWith(MockitoExtension.class)
class ArendeServiceImplTest {

    private static final String PATIENT_PERSON_ID = "191212121212";
    private static final String CERTIFICATE_ID = "certificateId";

    @InjectMocks
    private ArendeServiceImpl arendeServiceReal = new ArendeServiceImpl();

    @Mock
    private WebCertUserService webcertUserService;

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @Mock
    private ArendeRepository arendeRepository;

    @Mock
    private FragaSvarService fragaSvarService;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private LogService logService;

    @Mock
    private PaginationAndLoggingService paginationAndLoggingService;

    @Mock
    private MessageImportService messageImportService;

    @Test
    void filterArende() {
        //Given
        final ArendeServiceImpl arendeService = Mockito.spy(arendeServiceReal);

        //Names in hsa
        final HashMap<String, String> hsaNames = new HashMap<>();
        hsaNames.put("sign1", "D");
        hsaNames.put("sign2", "F");
        hsaNames.put("sign3", "E");

        Mockito.doReturn(hsaNames).when(arendeService).getNamesByHsaIds(any());
        WebCertUser user = Mockito.mock(WebCertUser.class);
        when(webcertUserService.getUser()).thenReturn(user);
        when(authoritiesHelper.getIntygstyperForPrivilege(eq(user), anyString()))
            .thenReturn(new HashSet<>(Arrays.asList("a", "b")));

        final var projection1 = buildArendeListItemProjection("sign1", "B", Status.PENDING_INTERNAL_ACTION,
            "SKICKAT_AV", ArendeAmne.OVRIGT, true, LocalDateTime.now(), "meddelande1");
        final var projection2 = buildArendeListItemProjection("sign2", "C", Status.PENDING_INTERNAL_ACTION,
            "SKICKAT_AV", ArendeAmne.OVRIGT, true, LocalDateTime.now(), "meddelande2");
        final var projection3 = buildArendeListItemProjection("sign3", "A", Status.PENDING_INTERNAL_ACTION,
            "SKICKAT_AV", ArendeAmne.OVRIGT, true, LocalDateTime.now(), "meddelande3");

        when(arendeRepository.filterArendeForList(any())).thenReturn(List.of(projection1, projection3, projection2));
        when(arendeRepository.findPaminnelseMeddelandeIdByMeddelandeIdIn(any())).thenReturn(List.of());

        final Map<Personnummer, PatientDetailsResolverResponse> statusMap = Mockito.mock(Map.class);
        PatientDetailsResolverResponse response = new PatientDetailsResolverResponse();
        response.setTestIndicator(false);
        response.setDeceased(false);
        response.setProtectedPerson((SekretessStatus.FALSE));
        when(statusMap.get(any(Personnummer.class))).thenReturn(response);
        when(patientDetailsResolver.getPersonStatusesForList(any())).thenReturn(statusMap);
        final QueryFragaSvarResponse qfsr = new QueryFragaSvarResponse();
        qfsr.setResults(List.of());
        when(fragaSvarService.filterFragaSvar(any())).thenReturn(qfsr);
        final QueryFragaSvarParameter filterParameters = new QueryFragaSvarParameter();

        final var arendeListItem1 = new ArendeListItem();
        arendeListItem1.setSigneratAv(projection1.getSigneratAv());
        final var arendeListItem2 = new ArendeListItem();
        arendeListItem2.setSigneratAv(projection2.getSigneratAv());
        final var arendeListItem3 = new ArendeListItem();
        arendeListItem3.setSigneratAv(projection3.getSigneratAv());

        final var arendeListItems = List.of(arendeListItem1, arendeListItem2, arendeListItem3);
        when(paginationAndLoggingService.get(eq(filterParameters), any(), eq(user))).thenReturn(arendeListItems);

        //When
        final QueryFragaSvarResponse queryFragaSvarResponse = arendeService.filterArende(filterParameters);

        //Then
        final List<ArendeListItem> results = queryFragaSvarResponse.getResults();
        assertEquals(3, results.size());
        assertEquals(projection1.getSigneratAv(), results.get(0).getSigneratAv());
        assertEquals(projection2.getSigneratAv(), results.get(1).getSigneratAv());
        assertEquals(projection3.getSigneratAv(), results.get(2).getSigneratAv());
    }

    @Nested
    class GetArendeInternalTests {


        @Test
        void shouldImportMessageIfNeeded() {
            when(messageImportService.isImportNeeded(CERTIFICATE_ID)).thenReturn(true);

            arendeServiceReal.getArendenInternal(CERTIFICATE_ID);

            verify(messageImportService).importMessages(CERTIFICATE_ID);
        }
    }

    private ArendeListItemProjection buildArendeListItemProjection(
        String signeratAv, String signeratAvName, Status status,
        String skickatAv, ArendeAmne amne, Boolean vidarebefordrad, LocalDateTime senasteHandelse,
        String meddelandeId) {
        return new se.inera.intyg.webcert.persistence.arende.model.ArendeListItemProjection(
            meddelandeId,
            "INTYG_ID",
            "INTYG_TYP",
            signeratAv,
            signeratAvName,
            status,
            PATIENT_PERSON_ID,
            senasteHandelse,
            vidarebefordrad,
            skickatAv,
            amne,
            "ENHET_NAME",
            "VARDGIVARE_NAME"
        );
    }
}