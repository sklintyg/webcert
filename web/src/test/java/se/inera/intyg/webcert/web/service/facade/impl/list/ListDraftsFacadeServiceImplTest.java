/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.impl.list;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.luse.support.LuseEntryPoint;

import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.converter.util.IntygDraftDecorator;
import se.inera.intyg.webcert.web.service.facade.list.ListDraftsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.riv.infrastructure.directory.v1.PersonInformationType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDraftsFacadeServiceImplTest {

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private UtkastService utkastService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private LogService logService;
    @Mock
    private IntygDraftDecorator intygDraftDecorator;
    @Mock
    private HsatkEmployeeService hsaEmployeeService;

    @InjectMocks
    private ListDraftsFacadeServiceImpl listDraftsFacadeService;

    private static final String PATIENT_EFTERNAMN = "Tolvansson";
    private static final String PATIENT_FORNAMN = "Tolvan";
    private static final String PATIENT_MELLANNAMN = "Von";
    private static final String PATIENT_POSTADRESS = "Testadress";
    private static final String PATIENT_POSTNUMMER = "12345";
    private static final String PATIENT_POSTORT = "Testort";

    private static final Personnummer PATIENT_PERSONNUMMER = ListTestHelper.createPnr("19121212-1212");
    private static final Personnummer PATIENT_PERSONNUMMER_PU_SEKRETESS = ListTestHelper.createPnr("20121212-1212");

    @BeforeEach
    public void setup() throws ModuleNotFoundException {
        when(patientDetailsResolver.getSekretessStatus(eq(PATIENT_PERSONNUMMER))).thenReturn(SekretessStatus.FALSE);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString())).thenReturn(ListTestHelper.buildPatient());

        when(hsaEmployeeService.getEmployee(anyString(), any())).thenAnswer(invocation -> {
            PersonInformationType personInformation = new PersonInformationType();
            personInformation.setMiddleAndSurName((String) invocation.getArguments()[0]);

            List<PersonInformationType> personInformationTypeList = new ArrayList<>();
            personInformationTypeList.add(personInformation);
            return personInformationTypeList;
        });

        final var statusMap = mock(Map.class);
        final var response = new PatientDetailsResolverResponse();
        response.setTestIndicator(false);
        response.setDeceased(false);
        response.setProtectedPerson(SekretessStatus.FALSE);
        when(statusMap.get(any(Personnummer.class))).thenReturn(response);
        Mockito.when(patientDetailsResolver.getPersonStatusesForList(any())).thenReturn(statusMap);
    }

    @Test
    public void shouldExcludeCertificatesIfUndefinedProtectedStatus() {
        setupUserWithPrivileges();
        setupUndefinedPatientResponse();

        when(utkastService.filterIntyg(any()))
                .thenReturn(Arrays.asList(ListTestHelper.buildUtkast(PATIENT_PERSONNUMMER), ListTestHelper.buildUtkast(PATIENT_PERSONNUMMER)));

        final var listInfo = listDraftsFacadeService.get(ListTestHelper.createListFilter());
        assertEquals(0, listInfo.getTotalCount());
        assertEquals(0, listInfo.getList().size());

    }

    private void setupUserWithPrivileges() {
        ListTestHelper.setupUser(webCertUserService, AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                LuseEntryPoint.MODULE_ID, AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST);
    }

    private void setupUndefinedPatientResponse() {
        final var statusMap = mock(Map.class);
        final var patientResponse = new PatientDetailsResolverResponse();
        patientResponse.setProtectedPerson(SekretessStatus.UNDEFINED);
        when(statusMap.get(any(Personnummer.class))).thenReturn(patientResponse);
        when(patientDetailsResolver.getPersonStatusesForList(anyList())).thenReturn(statusMap);
    }
}