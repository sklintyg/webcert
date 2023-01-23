/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-08-30.
 * <p>
 * NOT:
 * Intygen TS-BAS och TS-DIABETES får ej skrivas på patienter som
 * har SekretessStatus.TRUE. De intygen ska med andra ord inte
 * räknas med.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatisticsGroupByUtilTest extends AuthoritiesConfigurationTestSetup {

    private static final String HSA1 = "hsa-1";
    private static final String HSA2 = "hsa-2";
    private static final String PNR1 = "191212121212";
    private static final String PNR2 = "191313131313";
    private static final String PNR3 = "191414141414";

    private static final String FK7263 = "fk7263";
    private static final String TSBAS = "ts-bas";

    private static final String PNR_INVALID = "thiswillnotwork";

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private StatisticsGroupByUtil testee;

    @Before
    public void setup() {
        Personnummer pnr1 = Personnummer.createPersonnummer(PNR1).get();
        Personnummer pnr2 = Personnummer.createPersonnummer(PNR2).get();
        Personnummer pnr3 = Personnummer.createPersonnummer(PNR3).get();

        Map<Personnummer, SekretessStatus> sekrMap = new HashMap<>();
        sekrMap.put(pnr1, SekretessStatus.FALSE);
        sekrMap.put(pnr2, SekretessStatus.TRUE);
        sekrMap.put(pnr3, SekretessStatus.FALSE);

        when(patientDetailsResolver.getSekretessStatusForList(anyList())).thenReturn(sekrMap);
    }

    @Test
    public void testFilterAndGroupForTwoResultsOfSameUnitOneIsSekrForLakare() {

        when(webCertUserService.getUser()).thenReturn(createUser());

        List<GroupableItem> queryResult = new ArrayList<>();
        queryResult.add(new GroupableItem("id-1", HSA1, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-2", HSA1, PNR2, FK7263));

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);

        assertEquals(1, result.size());
        assertEquals(new Long(2L), result.get(HSA1));
    }

    @Test
    public void testFilterAndGroupForTwoResultsOfSameUnitOneIsSekrForVardadmin() {

        when(webCertUserService.getUser()).thenReturn(buildUserOfRole(AUTHORITIES_RESOLVER.getRole("VARDADMINISTRATOR")));

        Personnummer pnr1 = Personnummer.createPersonnummer(PNR1).get();
        Personnummer pnr2 = Personnummer.createPersonnummer(PNR2).get();

        Map<Personnummer, SekretessStatus> sekrMap = new HashMap<>();
        sekrMap.put(pnr1, SekretessStatus.FALSE);
        sekrMap.put(pnr2, SekretessStatus.TRUE);
        when(patientDetailsResolver.getSekretessStatusForList(anyList())).thenReturn(sekrMap);

        List<GroupableItem> queryResult = new ArrayList<>();
        queryResult.add(new GroupableItem("id-1", HSA1, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-2", HSA1, PNR2, FK7263));
        queryResult.add(new GroupableItem("id-3", HSA1, PNR2, TSBAS));    // Should be filtered away

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);

        assertEquals(1, result.size());
        assertEquals(new Long(1L), result.get(HSA1));
    }

    @Test
    public void testFilterAndGroupForThreeResultsOfSameUnitTwoIsSekrForOfWhichOneIsTS() {

        when(webCertUserService.getUser()).thenReturn(createUser());

        List<GroupableItem> queryResult = new ArrayList<>();
        queryResult.add(new GroupableItem("id-1", HSA1, PNR1, FK7263));   // No S. All can see.
        queryResult.add(new GroupableItem("id-2", HSA1, PNR2, FK7263));   // Is S, LAKARE can see.
        queryResult.add(new GroupableItem("id-3", HSA1, PNR1, TSBAS));    // Should be OK
        queryResult.add(new GroupableItem("id-4", HSA1, PNR2, TSBAS));    // Should be filtered away

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);

        assertEquals(1, result.size());
        assertEquals(new Long(3L), result.get(HSA1));
    }


    @Test
    public void testFilterAndGroupForMultipleUnitsForLakare() {

        when(webCertUserService.getUser()).thenReturn(createUser());

        List<GroupableItem> queryResult = new ArrayList<>();
        queryResult.add(new GroupableItem("id-1", HSA1, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-2", HSA1, PNR2, FK7263));
        queryResult.add(new GroupableItem("id-3", HSA1, PNR3, FK7263));
        queryResult.add(new GroupableItem("id-4", HSA2, PNR3, FK7263));
        queryResult.add(new GroupableItem("id-5", HSA2, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-6", HSA2, PNR3, FK7263));
        queryResult.add(new GroupableItem("id-7", HSA2, PNR1, TSBAS));
        queryResult.add(new GroupableItem("id-8", HSA1, PNR2, TSBAS));     // Never this one.

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);
        assertEquals(2, result.size());
        assertEquals(new Long(3L), result.get(HSA1));
        assertEquals(new Long(4L), result.get(HSA2));
    }

    @Test
    public void testFilterAndGroupForMultipleUnitsForVardadmin() {

        when(webCertUserService.getUser()).thenReturn(buildUserOfRole(AUTHORITIES_RESOLVER.getRole("VARDADMINISTRATOR")));

        List<GroupableItem> queryResult = new ArrayList<>();
        queryResult.add(new GroupableItem("id-1", HSA1, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-2", HSA1, PNR2, FK7263));      // Not this
        queryResult.add(new GroupableItem("id-3", HSA1, PNR3, FK7263));
        queryResult.add(new GroupableItem("id-4", HSA2, PNR3, FK7263));
        queryResult.add(new GroupableItem("id-5", HSA2, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-6", HSA2, PNR3, FK7263));
        queryResult.add(new GroupableItem("id-7", HSA2, PNR1, TSBAS));
        queryResult.add(new GroupableItem("id-8", HSA1, PNR2, TSBAS));     // Never this one.

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);
        assertEquals(2, result.size());
        assertEquals(new Long(2L), result.get(HSA1));
        assertEquals(new Long(4L), result.get(HSA2));
    }

    @Test
    public void testAssumeNotSekrWhenPUNotResponding() {

        when(webCertUserService.getUser()).thenReturn(createUser());

        List<GroupableItem> queryResult = new ArrayList<>();
        queryResult.add(new GroupableItem("id-1", HSA1, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-2", HSA1, PNR2, FK7263));

        Map<String, Long> result = testee.toSekretessFilteredMap(queryResult);
        assertEquals(1, result.size());
        assertEquals(new Long(2L), result.get(HSA1));
    }

    @Test
    public void testFilterEmptyMap() {
        Map<String, Long> result = testee.toSekretessFilteredMap(new ArrayList<>());
        assertEquals(0, result.size());
    }

    @Test
    public void testFilterInvalidPersonnummer() {
        List<GroupableItem> queryResult = new ArrayList<>();
        queryResult.add(new GroupableItem("id-1", HSA1, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-2", HSA2, PNR2, TSBAS));
        queryResult.add(new GroupableItem("id-3", HSA1, null, FK7263));
        queryResult.add(new GroupableItem("id-4", HSA1, "", TSBAS));
        queryResult.add(new GroupableItem("id-5", HSA2, "thisisainvalidparameter", FK7263));

        List<Personnummer> result = testee.getPersonummerList(queryResult);

        assertEquals(2, result.size());
    }

    @Test
    public void testFilterInvalidGroupableItems() {
        List<GroupableItem> queryResult = new ArrayList<>();
        queryResult.add(new GroupableItem("id-1", HSA1, PNR1, FK7263));
        queryResult.add(new GroupableItem("id-2", HSA2, PNR2, TSBAS));
        queryResult.add(new GroupableItem("id-3", HSA1, null, FK7263));
        queryResult.add(new GroupableItem("id-4", HSA1, "", TSBAS));
        queryResult.add(new GroupableItem("id-5", HSA2, "thisisainvalidparameter", FK7263));

        List<GroupableItem> result = testee.getFilteredGroupableItemList(queryResult);

        assertEquals(2, result.size());
    }

    private WebCertUser buildUserOfRole(Role role) {
        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        user.setHsaId("testuser");
        user.setNamn("test userman");

        Vardenhet vardenhet = new Vardenhet(HSA1, "enhet");

        Vardgivare vardgivare = new Vardgivare("vardgivare", "Vardgivaren");
        vardgivare.getVardenheter().add(vardenhet);

        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);

        return user;
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        return buildUserOfRole(role);
    }
}
