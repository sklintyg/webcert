/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.integration.hsa.services;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.webcert.integration.hsa.client.AuthorizationManagementService;
import se.inera.intyg.webcert.integration.hsa.stub.Medarbetaruppdrag;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.v1.CommissionType;
import se.riv.infrastructure.directory.v1.CredentialInformationType;

@RunWith(MockitoJUnitRunner.class)
public class HSAPersonServiceTest2 {

    private static final String HSA_PERSON_ID = "SE11837399";

    private static final String HSA_UNIT_ID = "SE405900000";

    @Mock
    private AuthorizationManagementService authorizationManagementService;
    //private HSAWebServiceCalls hsaWebServiceCalls;


    @InjectMocks
    private HsaPersonServiceImpl hsaPersonService;

    @Test
    public void testWithNoMius() {

        GetCredentialsForPersonIncludingProtectedPersonResponseType miuResponse = new GetCredentialsForPersonIncludingProtectedPersonResponseType();

        when(authorizationManagementService.getAuthorizationsForPerson(HSA_PERSON_ID, null, null)).thenReturn(miuResponse);

        List<CommissionType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void testWithOneMiu() {

        GetCredentialsForPersonIncludingProtectedPersonResponseType miuResponse = new GetCredentialsForPersonIncludingProtectedPersonResponseType();
        CredentialInformationType cit = new CredentialInformationType();

        CommissionType miu1 = new CommissionType();
        miu1.setCommissionHsaId("001");
        miu1.setHealthCareUnitHsaId(HSA_UNIT_ID);
        miu1.setCommissionPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu1.setHealthCareUnitEndDate(LocalDateTime.now().plusYears(1));

        cit.getCommission().add(miu1);
        miuResponse.getCredentialInformation().add(cit);

        when(authorizationManagementService.getAuthorizationsForPerson(HSA_PERSON_ID, null, null)).thenReturn(miuResponse);

        List<CommissionType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    public void testWithOneMiuNoMatch() {

        GetCredentialsForPersonIncludingProtectedPersonResponseType miuResponse = new GetCredentialsForPersonIncludingProtectedPersonResponseType();
        CredentialInformationType cit = new CredentialInformationType();

        CommissionType miu1 = new CommissionType();
        miu1.setCommissionHsaId("001");
        miu1.setHealthCareUnitHsaId("SE405900001");
        miu1.setCommissionPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu1.setHealthCareUnitEndDate(LocalDateTime.now().plusYears(1));
        cit.getCommission().add(miu1);

        miuResponse.getCredentialInformation().add(cit);
        when(authorizationManagementService.getAuthorizationsForPerson(HSA_PERSON_ID, null, null)).thenReturn(miuResponse);

        List<CommissionType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void testWithSeveralMius() {

        GetCredentialsForPersonIncludingProtectedPersonResponseType miuResponse = new GetCredentialsForPersonIncludingProtectedPersonResponseType();
        CredentialInformationType cit = new CredentialInformationType();

        CommissionType miu1 = new CommissionType();
        miu1.setCommissionHsaId("001");
        miu1.setHealthCareUnitHsaId(HSA_UNIT_ID);
        miu1.setCommissionPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu1.setHealthCareUnitEndDate(LocalDateTime.now().plusYears(1));
        cit.getCommission().add(miu1);

        // this MIU expired 10 minutes ago
        CommissionType miu2 = new CommissionType();
        miu2.setCommissionHsaId("002");
        miu2.setHealthCareUnitHsaId(HSA_UNIT_ID);
        miu2.setCommissionPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu2.setHealthCareUnitEndDate(LocalDateTime.now().minusMinutes(10));
        cit.getCommission().add(miu2);

        CommissionType miu3 = new CommissionType();
        miu3.setCommissionHsaId("003");
        miu3.setHealthCareUnitHsaId("SE405900003");
        miu3.setCommissionPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu3.setHealthCareUnitEndDate(LocalDateTime.now().plusYears(1));
        cit.getCommission().add(miu3);

        miuResponse.getCredentialInformation().add(cit);
        when(authorizationManagementService.getAuthorizationsForPerson(HSA_PERSON_ID, null, null)).thenReturn(miuResponse);

        List<CommissionType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(1, res.size());
    }

    @Test
    public void testWithSeveralMiusNoMatch() {

        GetCredentialsForPersonIncludingProtectedPersonResponseType miuResponse = new GetCredentialsForPersonIncludingProtectedPersonResponseType();
        CredentialInformationType cit = new CredentialInformationType();

        CommissionType miu1 = new CommissionType();
        miu1.setCommissionHsaId("001");
        miu1.setHealthCareUnitHsaId("SE405900001");
        miu1.setCommissionPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu1.setHealthCareUnitEndDate(LocalDateTime.now().plusYears(1));
        cit.getCommission().add(miu1);

        CommissionType miu2 = new CommissionType();
        miu2.setCommissionHsaId("002");
        miu2.setHealthCareUnitHsaId("SE405900002");
        miu2.setCommissionPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu2.setHealthCareUnitEndDate(LocalDateTime.now().plusYears(1));
        cit.getCommission().add(miu2);

        CommissionType miu3 = new CommissionType();
        miu3.setCommissionHsaId("003");
        miu3.setHealthCareUnitHsaId("SE405900003");
        miu3.setCommissionPurpose(Medarbetaruppdrag.VARD_OCH_BEHANDLING);
        miu3.setHealthCareUnitEndDate(LocalDateTime.now().plusYears(1));
        cit.getCommission().add(miu3);

        miuResponse.getCredentialInformation().add(cit);
        when(authorizationManagementService.getAuthorizationsForPerson(HSA_PERSON_ID, null, null)).thenReturn(miuResponse);

        List<CommissionType> res = hsaPersonService.checkIfPersonHasMIUsOnUnit(HSA_PERSON_ID, HSA_UNIT_ID);

        assertNotNull(res);
        assertEquals(0, res.size());
    }
}
