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

package se.inera.intyg.webcert.web.csintegration.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetUnansweredCommunicationInternalResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationRequest;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredQAs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetUnansweredCommunicationFromCertificateServiceTest {

    private static final List<String> PATIENT_IDS = List.of("patientId1", "patientId2");
    private static final int MAX_DAYS = 90;

    @Mock
    private CSIntegrationService csIntegrationService;

    @InjectMocks
    private GetUnansweredCommunicationFromCertificateService getUnansweredCommunicationFromCertificateService;

    private UnansweredCommunicationRequest request;

    @BeforeEach
    void setUp() {
        request = new UnansweredCommunicationRequest(PATIENT_IDS, MAX_DAYS);
    }

    @Test
    void shallReturnEmptyMapWhenCSIntegrationServiceReturnsEmptyOptional() {
        doReturn(Optional.empty()).when(csIntegrationService)
            .getUnansweredCommunicationMessages(PATIENT_IDS, MAX_DAYS);

        final var result = getUnansweredCommunicationFromCertificateService.get(request);

        assertTrue(result.getUnansweredQAsMap().isEmpty());
    }

    @Test
    void shallReturnMessagesWhenCSIntegrationServiceReturnsMessagesInOptional() {
        final var messages = new HashMap<String, UnansweredQAs>();
        messages.put("certificate1", new UnansweredQAs());
        messages.put("certificate2", new UnansweredQAs());

        final var responseDTO = GetUnansweredCommunicationInternalResponseDTO.builder()
            .messages(messages)
            .build();

        doReturn(Optional.of(responseDTO)).when(csIntegrationService)
            .getUnansweredCommunicationMessages(PATIENT_IDS, MAX_DAYS);

        final var result = getUnansweredCommunicationFromCertificateService.get(request);

        assertEquals(2, result.getUnansweredQAsMap().size());
        assertEquals(messages.get("certificate1"), result.getUnansweredQAsMap().get("certificate1"));
        assertEquals(messages.get("certificate2"), result.getUnansweredQAsMap().get("certificate2"));
    }

    @Test
    void shallReturnEmptyMapWhenCSIntegrationServiceReturnsEmptyMessagesMap() {
        final var responseDTO = GetUnansweredCommunicationInternalResponseDTO.builder()
            .messages(Map.of())
            .build();

        doReturn(Optional.of(responseDTO)).when(csIntegrationService)
            .getUnansweredCommunicationMessages(PATIENT_IDS, MAX_DAYS);

        final var result = getUnansweredCommunicationFromCertificateService.get(request);

        assertTrue(result.getUnansweredQAsMap().isEmpty());
    }

    @Test
    void shallCallCSIntegrationServiceWithCorrectParameters() {
        doReturn(Optional.empty()).when(csIntegrationService)
            .getUnansweredCommunicationMessages(PATIENT_IDS, MAX_DAYS);

        getUnansweredCommunicationFromCertificateService.get(request);

        verify(csIntegrationService).getUnansweredCommunicationMessages(PATIENT_IDS, MAX_DAYS);
    }
}