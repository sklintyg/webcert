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
package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.ag7804.v1.rest.Ag7804ModuleApiV1;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.UtkastCandidateServiceImpl;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;

@ExtendWith(MockitoExtension.class)
class CandidateDataHelperImplTest {

    private static final String CANDIDATE_ID = "candidateId";
    private static final String CANDIDATE_TYPE = "type";
    private static final String CANDIDATE_VERSION = "0";
    private static final String PATIENT_ID = "191212121212";
    private static final UtkastCandidateMetaData metadata = createCandidateMetadata(CANDIDATE_ID, CANDIDATE_TYPE, CANDIDATE_VERSION);


    @Mock
    PatientDetailsResolver patientDetailsResolver;

    @Mock
    IntygModuleRegistry moduleRegistry;

    @Mock
    UtkastCandidateServiceImpl utkastCandidateService;

    @InjectMocks
    CandidateDataHelperImpl candidateDataHelper;

    @BeforeEach
    void setUp() throws ModuleNotFoundException {
        final var patient = new Patient();
        patient.setPersonId(Personnummer.createPersonnummer(PATIENT_ID).get());
        doReturn(patient).when(patientDetailsResolver).resolvePatient(any(), any(), any());

        final var moduleApi = mock(Ag7804ModuleApiV1.class);

        doReturn(moduleApi).when(moduleRegistry).getModuleApi(anyString(), anyString());

        when(utkastCandidateService.getCandidateMetaData(any(ModuleApi.class), anyString(), anyString(), any(Patient.class), anyBoolean()))
            .thenReturn(Optional.of(metadata));
    }

    @Test
    void shouldReturnCandidateMetadata() {
        final var response = candidateDataHelper.getCandidateMetadata("type", "version", Personnummer.createPersonnummer(PATIENT_ID).get());
        assertEquals(Optional.of(metadata), response);
    }

    private static UtkastCandidateMetaData createCandidateMetadata(String intygId, String intygType, String intygTypeVersion) {
        return new UtkastCandidateMetaData.Builder()
            .with(builder -> {
                builder.intygId = intygId;
                builder.intygType = intygType;
                builder.intygTypeVersion = intygTypeVersion;
            })
            .create();
    }

}
