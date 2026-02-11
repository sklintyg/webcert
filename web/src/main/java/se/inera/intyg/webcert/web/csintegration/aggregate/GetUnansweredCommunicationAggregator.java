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
