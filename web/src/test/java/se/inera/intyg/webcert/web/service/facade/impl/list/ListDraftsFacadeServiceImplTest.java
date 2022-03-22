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

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.infra.integration.hsatk.services.HsatkEmployeeService;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.converter.util.IntygDraftDecorator;
import se.inera.intyg.webcert.web.service.facade.impl.CopyCertificateFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.ListDraftsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolverResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.riv.infrastructure.directory.v1.PersonInformationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Personnummer PATIENT_PERSONNUMMER = createPnr("19121212-1212");
    private static final Personnummer PATIENT_PERSONNUMMER_PU_SEKRETESS = createPnr("20121212-1212");

    @Before
    public void setup() throws ModuleNotFoundException {
        when(patientDetailsResolver.getSekretessStatus(eq(PATIENT_PERSONNUMMER))).thenReturn(SekretessStatus.FALSE);
        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString())).thenReturn(buildPatient());

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

    private static Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
    }

    private Patient buildPatient() {
        Patient patient = new Patient();
        patient.setPersonId(PATIENT_PERSONNUMMER);
        patient.setEfternamn(PATIENT_EFTERNAMN);
        patient.setFornamn(PATIENT_FORNAMN);
        patient.setMellannamn(PATIENT_MELLANNAMN);
        patient.setFullstandigtNamn(PATIENT_FORNAMN + " " + PATIENT_MELLANNAMN + " " + PATIENT_EFTERNAMN);

        patient.setPostadress(PATIENT_POSTADRESS);
        patient.setPostnummer(PATIENT_POSTNUMMER);
        patient.setPostort(PATIENT_POSTORT);

        return patient;
    }
}