package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import java.util.Optional;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;

public interface CertificateSignAndSendDescriptionFunction {

    Optional<ResourceLinkDTO> get(Certificate certificate);
}
