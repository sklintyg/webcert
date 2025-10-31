package se.inera.intyg.webcert.web.service.srs;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetSickLeaveCertificateInternalResponseDTO;

@Slf4j
@RequiredArgsConstructor
@Service("GetSrsCertificateFromCS")
public class GetSrsCertificateFromCertificateService implements GetSrsCertificate {

  private final CSIntegrationService csIntegrationService;

  @Override
  public SrsCertificate getSrsCertificate(String certificateId) {
    final var sickLeaveCertificate = csIntegrationService.getSickLeaveCertificate(certificateId);
    if (isSickLeaveCertificatePresent(sickLeaveCertificate)) {
      return new SrsCertificate(
          sickLeaveCertificate.get().getSickLeaveCertificate().getId(),
          sickLeaveCertificate.get().getSickLeaveCertificate().getDiagnoseCode(),
          sickLeaveCertificate.get().getSickLeaveCertificate().getSigningDateTime() != null ?
              sickLeaveCertificate.get().getSickLeaveCertificate().getSigningDateTime().toLocalDate() : null,
          sickLeaveCertificate.get().getSickLeaveCertificate().getExtendsCertificateId()
      );
    }
    return null;
  }

  private static boolean isSickLeaveCertificatePresent(
      Optional<GetSickLeaveCertificateInternalResponseDTO> sickLeaveCertificate) {
    return sickLeaveCertificate.isPresent()
        && sickLeaveCertificate.get().isAvailable();
  }
}
