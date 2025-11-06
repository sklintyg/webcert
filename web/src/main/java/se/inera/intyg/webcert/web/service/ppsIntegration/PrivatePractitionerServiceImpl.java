package se.inera.intyg.webcert.web.service.ppsIntegration;

import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.ppsIntegration.dto.PrivatePractitioner;
import se.inera.intyg.webcert.web.web.controller.api.dto.PrivatePractitionerDTO;
import se.inera.intyg.webcert.web.web.controller.api.dto.RegisterPrivatePractitionerRequest;

@Service
public class PrivatePractitionerServiceImpl implements PrivatePractitionerService {

    @Override
    public PrivatePractitioner registerPrivatePractitioner(RegisterPrivatePractitionerRequest registerPrivatePractitionerRequest) {
        return null;
    }

    @Override
    public PrivatePractitioner getPrivatePractitioner() {
        return null;
    }

    @Override
    public void updatePrivatePractitioner(PrivatePractitionerDTO privatePractitioner) {
    }
}
