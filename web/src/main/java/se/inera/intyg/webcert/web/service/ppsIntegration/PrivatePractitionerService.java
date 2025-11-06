package se.inera.intyg.webcert.web.service.ppsIntegration;

import se.inera.intyg.webcert.web.service.ppsIntegration.dto.PrivatePractitioner;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.RegisterPrivatePractitionerRequest;

public interface PrivatePractitionerService {

    PrivatePractitioner registerPrivatePractitioner(RegisterPrivatePractitionerRequest registerPrivatePractitionerRequest);

    PrivatePractitioner getPrivatePractitioner();

    void updatePrivatePractitioner(PrivatePractitionerDTO privatePractitioner);
}
