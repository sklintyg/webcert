package se.inera.intyg.webcert.web.service.facade.util;

import java.time.LocalDateTime;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;

public interface CertificateRecipientConverter {
  CertificateRecipient get(String type, String certificateId, LocalDateTime sent);
}
