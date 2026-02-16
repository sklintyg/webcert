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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.GetUnansweredCommunicationFacadeService;
import se.inera.intyg.webcert.web.service.unansweredcommunication.UnansweredCommunicationService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationRequest;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationResponse;

import java.util.HashMap;
import java.util.List;

@Service
public class GetUnansweredCommunicationAggregator implements GetUnansweredCommunicationFacadeService {

    private final UnansweredCommunicationService getUnansweredCommunicationWC;
    private final UnansweredCommunicationService getUnansweredCommunicationFromCS;

    public GetUnansweredCommunicationAggregator(@Qualifier("getUnansweredCommunicationFromWC") UnansweredCommunicationService getUnansweredCommunicationFromWC,
                                                @Qualifier("getUnansweredCommunicationFromCS") UnansweredCommunicationService getUnansweredCommunicationFromCS) {
        this.getUnansweredCommunicationWC = getUnansweredCommunicationFromWC;
        this.getUnansweredCommunicationFromCS = getUnansweredCommunicationFromCS;
    }

    @Override
    public UnansweredCommunicationResponse get(List<String> patientId, Integer maxDaysOfUnansweredCommunication) {
        final var request = new UnansweredCommunicationRequest(patientId, maxDaysOfUnansweredCommunication);
        final var combinedResponses = new HashMap<>(getUnansweredCommunicationFromCS.get(request).getUnansweredQAsMap());
        combinedResponses.putAll(getUnansweredCommunicationWC.get(request).getUnansweredQAsMap());
        return new UnansweredCommunicationResponse(combinedResponses);
    }
}
