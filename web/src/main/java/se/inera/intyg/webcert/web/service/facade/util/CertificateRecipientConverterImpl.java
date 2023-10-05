package se.inera.intyg.webcert.web.service.facade.util;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;

@Service
public class CertificateRecipientConverterImpl implements CertificateRecipientConverter {

  private final CertificateReceiverService certificateReceiverService;

  public CertificateRecipientConverterImpl(CertificateReceiverService certificateReceiverService) {
    this.certificateReceiverService = certificateReceiverService;
  }

  @Override
  public CertificateRecipient get(String type, String certificateId, LocalDateTime sent) {
    final var recipients = certificateReceiverService.listPossibleReceiversWithApprovedInfo(type, certificateId);

    return recipients
        .stream()
        .findFirst()
        .map(recipient -> CertificateRecipient
            .builder()
            .id(recipient.getId())
            .name(recipient.getName())
            .sent(sent)
            .build())
        .orElse(null);
  }
}
