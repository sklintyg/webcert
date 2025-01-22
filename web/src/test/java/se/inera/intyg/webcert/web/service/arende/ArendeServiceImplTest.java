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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.service.facade.list.PaginationAndLoggingService;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

public class ArendeServiceImplTest {

    private static final String PATIENT_PERSON_ID = "191212121212";

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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void filterArende() {
        //Given
        final ArendeServiceImpl arendeService = Mockito.spy(arendeServiceReal);

        //Names in hsa
        final HashMap<String, String> hsaNames = new HashMap<>();
        hsaNames.put("sign1", "D");
        hsaNames.put("sign2", "F");
        hsaNames.put("sign3", "E");

        Mockito.doReturn(hsaNames).when(arendeService).getNamesByHsaIds(any());
        WebCertUser user = Mockito.mock(WebCertUser.class);
        Mockito.when(webcertUserService.getUser()).thenReturn(user);
        Mockito.when(authoritiesHelper.getIntygstyperForPrivilege(eq(user), anyString()))
            .thenReturn(new HashSet<>(Arrays.asList("a", "b")));

        //Names in arende
        final Arende arende1 = buildArende("sign1", "B", "enhet");
        final Arende arende2 = buildArende("sign2", "C", "enhet");
        final Arende arende3 = buildArende("sign3", "A", "enhet");

        Mockito.when(arendeRepository.filterArende(any())).thenReturn(Arrays.asList(arende1, arende3, arende2));
        final Map<Personnummer, PatientDetailsResolverResponse> statusMap = Mockito.mock(Map.class);
        PatientDetailsResolverResponse response = new PatientDetailsResolverResponse();
        response.setTestIndicator(false);
        response.setDeceased(false);
        response.setProtectedPerson((SekretessStatus.FALSE));
        Mockito.when(statusMap.get(any(Personnummer.class))).thenReturn(response);
        Mockito.when(patientDetailsResolver.getPersonStatusesForList(any())).thenReturn(statusMap);
        final QueryFragaSvarResponse qfsr = new QueryFragaSvarResponse();
        qfsr.setResults(Lists.emptyList());
        Mockito.when(fragaSvarService.filterFragaSvar(any())).thenReturn(qfsr);
        final QueryFragaSvarParameter filterParameters = new QueryFragaSvarParameter();

        final var arendeListItem1 = new ArendeListItem();
        arendeListItem1.setSigneratAv(arende1.getSigneratAv());
        final var arendeListItem2 = new ArendeListItem();
        arendeListItem2.setSigneratAv(arende2.getSigneratAv());
        final var arendeListItem3 = new ArendeListItem();
        arendeListItem3.setSigneratAv(arende3.getSigneratAv());

        final var arendeListItems = List.of(arendeListItem1, arendeListItem2, arendeListItem3);
        Mockito.when(paginationAndLoggingService.get(eq(filterParameters), any(), eq(user))).thenReturn(arendeListItems);

        //When
        final QueryFragaSvarResponse queryFragaSvarResponse = arendeService.filterArende(filterParameters);

        //Then
        final List<ArendeListItem> results = queryFragaSvarResponse.getResults();
        assertEquals(3, results.size());
        assertEquals(arende1.getSigneratAv(), results.get(0).getSigneratAv());
        assertEquals(arende2.getSigneratAv(), results.get(1).getSigneratAv());
        assertEquals(arende3.getSigneratAv(), results.get(2).getSigneratAv());
    }

    private Arende buildArende(String signeratAv, String signeratAvName, String enhet) {
        return buildArende(signeratAv, signeratAvName, enhet, Status.PENDING_INTERNAL_ACTION, UUID.randomUUID().toString());
    }

    private Arende buildArende(String signeratAv, String signeratAvName, String enhet, Status status, String meddelandeId) {
        return buildArende(signeratAv, signeratAvName, enhet, status, "PAMINNELSE_MEDDELANDE_ID", "SVAR_PA_ID", "SKICKAT_AV",
            LocalDate.now(),
            ArendeAmne.OVRIGT, Boolean.TRUE, LocalDateTime.now(), meddelandeId);
    }

    private Arende buildArende(String signeratAv, String signeratAvName, String enhet, Status status, String paminnelseMeddelandeId,
        String svarPaId,
        String skickatAv, LocalDate sistaDatumForSvar, ArendeAmne amne, Boolean vidarebefordrad, LocalDateTime senasteHandelse,
        String meddelandeId) {
        return buildArende(signeratAv, signeratAvName, enhet, status, paminnelseMeddelandeId, svarPaId, skickatAv, sistaDatumForSvar, amne,
            vidarebefordrad, senasteHandelse, meddelandeId, "vardaktorName");
    }

    private Arende buildArende(String signeratAv, String signeratAvName, String enhet, Status status, String paminnelseMeddelandeId,
        String svarPaId,
        String skickatAv, LocalDate sistaDatumForSvar, ArendeAmne amne, Boolean vidarebefordrad, LocalDateTime senasteHandelse,
        String meddelandeId, String vardaktorName) {
        Arende res = new Arende();
        res.setAmne(amne);
        res.setIntygsId("INTYG_ID");
        res.setMeddelande("MEDDELANDE");
        res.setMeddelandeId(meddelandeId);
        res.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        res.setPatientPersonId(PATIENT_PERSON_ID);
        res.setReferensId("REFERENS_ID");
        res.setRubrik("RUBRIK");
        res.setSistaDatumForSvar(sistaDatumForSvar);
        res.setSkickatAv(skickatAv);
        res.setSkickatTidpunkt(LocalDateTime.now().minusDays(3));
        res.setSvarPaId(svarPaId);
        res.setSvarPaReferens("SVAR_PA_REFERENS");
        res.setIntygTyp("INTYG_TYP");
        res.setSigneratAv(signeratAv);
        res.setSigneratAvName(signeratAvName);
        res.setEnhetId(enhet);
        res.setEnhetName("ENHET_NAME");
        res.setVardgivareName("VARDGIVARE_NAME");
        res.setStatus(status);
        res.setTimestamp(LocalDateTime.now());
        res.setVidarebefordrad(vidarebefordrad);
        res.setSenasteHandelse(senasteHandelse);
        res.setVardaktorName(vardaktorName);

        res.getKomplettering().add(buildMedicinsktArende("1", 1, "text 1"));
        res.getKomplettering().add(buildMedicinsktArende("2", null, "text 2"));
        res.getKomplettering().add(buildMedicinsktArende("3", 3, "text 3"));

        res.getKontaktInfo().add("Kontakt 1");
        res.getKontaktInfo().add("Kontakt 2");
        res.getKontaktInfo().add("Kontakt 3");
        return res;
    }

    private MedicinsktArende buildMedicinsktArende(String frageId, Integer instans, String text) {
        MedicinsktArende res = new MedicinsktArende();
        res.setFrageId(frageId);
        res.setInstans(instans);
        res.setText(text);
        return res;
    }


}
