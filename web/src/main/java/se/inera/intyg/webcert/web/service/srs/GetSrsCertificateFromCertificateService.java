package se.inera.intyg.webcert.web.service.srs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@Slf4j
@RequiredArgsConstructor
@Service("GetSrsCertificateFromCS")
public class GetSrsCertificateFromCertificateService implements GetSrsCertificate {

  private final CSIntegrationService csIntegrationService;

  @Override
  public SrsCertificate getSrsCertificate(String certificateId) {
    final var sickLeaveCertificate = csIntegrationService.getSickLeaveCertificate(certificateId);
    if (sickLeaveCertificate != null && sickLeaveCertificate.isPresent() && sickLeaveCertificate.get().isAvailable()) {
      return new SrsCertificate(
          sickLeaveCertificate.get().getSickLeaveCertificate().getId(),
          sickLeaveCertificate.get().getSickLeaveCertificate().getDiagnoseCode(),
          sickLeaveCertificate.get().getSickLeaveCertificate().getSignedDateTime().toLocalDate(),
          sickLeaveCertificate.get().getSickLeaveCertificate().getExtendsCertificateId()
      );
    }
    return null;
  }
}
