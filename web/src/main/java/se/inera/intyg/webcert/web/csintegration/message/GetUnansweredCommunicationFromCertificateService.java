package se.inera.intyg.webcert.web.csintegration.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.service.facade.GetUnansweredCommunicationFacadeService;
import se.inera.intyg.webcert.web.service.unansweredcommunication.UnansweredCommunicationService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationRequest;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationResponse;

import java.util.Map;
import java.util.Optional;

@Service("getUnansweredCommunicationFromCS")
@RequiredArgsConstructor
public class GetUnansweredCommunicationFromCertificateService implements UnansweredCommunicationService {
    private final CSIntegrationService csIntegrationService;

    @Override
    public UnansweredCommunicationResponse get(UnansweredCommunicationRequest request) {
        final var messages = csIntegrationService.getUnansweredCommunicationMessages(request.getPatientIds(), request.getMaxDaysOfUnansweredCommunication());
        return messages.map(getUnansweredCommunicationInternalResponseDTO -> new UnansweredCommunicationResponse(getUnansweredCommunicationInternalResponseDTO.getMessages())).orElseGet(() -> new UnansweredCommunicationResponse(Map.of()));
    }
}
