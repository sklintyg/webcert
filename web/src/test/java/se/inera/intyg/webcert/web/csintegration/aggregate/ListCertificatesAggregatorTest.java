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
package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetPatientCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnitCertificatesRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

@ExtendWith(MockitoExtension.class)
class ListCertificatesAggregatorTest {

    private static final List<ListIntygEntry> LIST_INTYG_ENTRIES_FROM_CS = List.of(new ListIntygEntry());
    private static final String PATIENT_ID = "PATIENT_ID";
    private static final ListFilter LIST_FILTER = new ListFilter();
    private static final QueryIntygParameter QUERY_INTYG_PARAMETER = new QueryIntygParameter();
    private static final GetPatientCertificatesRequestDTO REQUEST = GetPatientCertificatesRequestDTO.builder().build();
    private static final GetUnitCertificatesRequestDTO UNIT_REQUEST = GetUnitCertificatesRequestDTO.builder().build();

    @Mock
    CertificateServiceProfile certificateServiceProfile;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    CSIntegrationService csIntegrationService;

    @InjectMocks
    ListCertificatesAggregator listCertificatesAggregator;

    @Nested
    class PatientCertificatesList {

        @Test
        void shouldReturnEmptyListIfProfileIsNotActive() {
            final var response = listCertificatesAggregator.listCertificatesForPatient(PATIENT_ID);
            assertEquals(Collections.emptyList(), response);
            verifyNoInteractions(csIntegrationService);
        }

        @Test
        void shouldReturnListFromAPIIfProfileIsActive() {
            when(csIntegrationRequestFactory.getPatientCertificatesRequest(PATIENT_ID))
                .thenReturn(REQUEST);
            when(certificateServiceProfile.active())
                .thenReturn(true);
            when(csIntegrationService.listCertificatesForPatient(REQUEST))
                .thenReturn(LIST_INTYG_ENTRIES_FROM_CS);

            final var response = listCertificatesAggregator.listCertificatesForPatient(PATIENT_ID);

            assertEquals(LIST_INTYG_ENTRIES_FROM_CS, response);
        }
    }

    @Nested
    class UnitCertificatesList {

        @Test
        void shouldReturnEmptyListIfProfileIsNotActive() {
            final var response = listCertificatesAggregator.listCertificatesForUnit(LIST_FILTER);
            assertEquals(Collections.emptyList(), response);
            verifyNoInteractions(csIntegrationService);
        }

        @Test
        void shouldReturnListFromAPIIfProfileIsActive() {
            when(csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER))
                .thenReturn(UNIT_REQUEST);
            when(certificateServiceProfile.active())
                .thenReturn(true);
            when(csIntegrationService.listCertificatesForUnit(UNIT_REQUEST))
                .thenReturn(LIST_INTYG_ENTRIES_FROM_CS);

            final var response = listCertificatesAggregator.listCertificatesForUnit(LIST_FILTER);

            assertEquals(LIST_INTYG_ENTRIES_FROM_CS, response);
        }
    }

    @Nested
    class DoctorCertificateList {

        @Test
        void shouldReturnEmptyListIfProfileIsNotActive() {
            final var response = listCertificatesAggregator.listCertificatesForDoctor(QUERY_INTYG_PARAMETER);
            assertEquals(Collections.emptyList(), response);
            verifyNoInteractions(csIntegrationService);
        }

        @Test
        void shouldReturnListFromAPIIfProfileIsActive() {
            when(csIntegrationRequestFactory.getUnitCertificatesRequest(QUERY_INTYG_PARAMETER))
                .thenReturn(UNIT_REQUEST);
            when(certificateServiceProfile.active())
                .thenReturn(true);
            when(csIntegrationService.listCertificatesForUnit(UNIT_REQUEST))
                .thenReturn(LIST_INTYG_ENTRIES_FROM_CS);

            final var response = listCertificatesAggregator.listCertificatesForDoctor(QUERY_INTYG_PARAMETER);

            assertEquals(LIST_INTYG_ENTRIES_FROM_CS, response);
        }
    }
}
