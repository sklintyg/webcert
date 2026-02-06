package se.inera.intyg.webcert.web.service.facade;

import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationResponse;
import java.util.List;

public interface GetUnansweredCommunicationFacadeService {

    UnansweredCommunicationResponse get(List<String> patientId);
}
