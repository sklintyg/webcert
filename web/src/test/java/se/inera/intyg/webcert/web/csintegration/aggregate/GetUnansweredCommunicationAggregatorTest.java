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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.unansweredcommunication.UnansweredCommunicationService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationRequest;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationResponse;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredQAs;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetUnansweredCommunicationAggregatorTest {

    private static final List<String> PATIENT_IDS = List.of("patientId1", "patientId2");

    @Mock
    private UnansweredCommunicationService getUnansweredCommunicationFromWC;
    @Mock
    private UnansweredCommunicationService getUnansweredCommunicationFromCS;

    private GetUnansweredCommunicationAggregator getUnansweredCommunicationAggregator;

    @BeforeEach
    void setUp() {
        getUnansweredCommunicationAggregator = new GetUnansweredCommunicationAggregator(
            getUnansweredCommunicationFromWC, getUnansweredCommunicationFromCS
        );
    }

    @Test
    void shallCombineResponsesFromCSAndWC() {
        final var csMap = new HashMap<String, UnansweredQAs>();
        csMap.put("certificate1", new UnansweredQAs());
        final var csResponse = new UnansweredCommunicationResponse(csMap);

        final var wcMap = new HashMap<String, UnansweredQAs>();
        wcMap.put("certificate2", new UnansweredQAs());
        final var wcResponse = new UnansweredCommunicationResponse(wcMap);

        doReturn(csResponse).when(getUnansweredCommunicationFromCS).get(any(UnansweredCommunicationRequest.class));
        doReturn(wcResponse).when(getUnansweredCommunicationFromWC).get(any(UnansweredCommunicationRequest.class));

        final var actualResult = getUnansweredCommunicationAggregator.get(PATIENT_IDS);

        assertEquals(2, actualResult.getUnansweredQAsMap().size());
        assertEquals(csResponse.getUnansweredQAsMap().get("certificate1"), actualResult.getUnansweredQAsMap().get("certificate1"));
        assertEquals(wcResponse.getUnansweredQAsMap().get("certificate2"), actualResult.getUnansweredQAsMap().get("certificate2"));
    }

    @Test
    void shallOverwriteCSResponseWithWCResponseWhenKeysMatch() {
        final var csMap = new HashMap<String, UnansweredQAs>();
        final var csQAs = new UnansweredQAs();
        csMap.put("certificate1", csQAs);
        final var csResponse = new UnansweredCommunicationResponse(csMap);

        final var wcMap = new HashMap<String, UnansweredQAs>();
        final var wcQAs = new UnansweredQAs();
        wcMap.put("certificate1", wcQAs);
        final var wcResponse = new UnansweredCommunicationResponse(wcMap);

        doReturn(csResponse).when(getUnansweredCommunicationFromCS).get(any(UnansweredCommunicationRequest.class));
        doReturn(wcResponse).when(getUnansweredCommunicationFromWC).get(any(UnansweredCommunicationRequest.class));

        final var actualResult = getUnansweredCommunicationAggregator.get(PATIENT_IDS);

        assertEquals(1, actualResult.getUnansweredQAsMap().size());
        assertEquals(wcQAs, actualResult.getUnansweredQAsMap().get("certificate1"));
    }

    @Test
    void shallReturnOnlyCSResponseWhenWCResponseIsEmpty() {
        final var csMap = new HashMap<String, UnansweredQAs>();
        csMap.put("certificate1", new UnansweredQAs());
        final var csResponse = new UnansweredCommunicationResponse(csMap);

        final var wcResponse = new UnansweredCommunicationResponse(new HashMap<>());

        doReturn(csResponse).when(getUnansweredCommunicationFromCS).get(any(UnansweredCommunicationRequest.class));
        doReturn(wcResponse).when(getUnansweredCommunicationFromWC).get(any(UnansweredCommunicationRequest.class));

        final var actualResult = getUnansweredCommunicationAggregator.get(PATIENT_IDS);

        assertEquals(1, actualResult.getUnansweredQAsMap().size());
        assertEquals(csResponse.getUnansweredQAsMap().get("certificate1"), actualResult.getUnansweredQAsMap().get("certificate1"));
    }

    @Test
    void shallReturnOnlyWCResponseWhenCSResponseIsEmpty() {
        final var csResponse = new UnansweredCommunicationResponse(new HashMap<>());

        final var wcMap = new HashMap<String, UnansweredQAs>();
        wcMap.put("certificate2", new UnansweredQAs());
        final var wcResponse = new UnansweredCommunicationResponse(wcMap);

        doReturn(csResponse).when(getUnansweredCommunicationFromCS).get(any(UnansweredCommunicationRequest.class));
        doReturn(wcResponse).when(getUnansweredCommunicationFromWC).get(any(UnansweredCommunicationRequest.class));

        final var actualResult = getUnansweredCommunicationAggregator.get(PATIENT_IDS);

        assertEquals(1, actualResult.getUnansweredQAsMap().size());
        assertEquals(wcResponse.getUnansweredQAsMap().get("certificate2"), actualResult.getUnansweredQAsMap().get("certificate2"));
    }

    @Test
    void shallCallBothServicesWithCorrectRequest() {
        final var csResponse = new UnansweredCommunicationResponse(new HashMap<>());
        final var wcResponse = new UnansweredCommunicationResponse(new HashMap<>());

        doReturn(csResponse).when(getUnansweredCommunicationFromCS).get(any(UnansweredCommunicationRequest.class));
        doReturn(wcResponse).when(getUnansweredCommunicationFromWC).get(any(UnansweredCommunicationRequest.class));

        getUnansweredCommunicationAggregator.get(PATIENT_IDS);

        verify(getUnansweredCommunicationFromCS).get(any(UnansweredCommunicationRequest.class));
        verify(getUnansweredCommunicationFromWC).get(any(UnansweredCommunicationRequest.class));
    }
}