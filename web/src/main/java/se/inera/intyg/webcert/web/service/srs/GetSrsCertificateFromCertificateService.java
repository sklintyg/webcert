package se.inera.intyg.webcert.web.service.srs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetCertificateAggregator;

@Slf4j
@RequiredArgsConstructor
@Service("GetSrsCertificateFromCS")
public class GetSrsCertificateFromCertificateService implements GetSrsCertificate {

  public static final String CATEGORY_DIAGNOSIS_ID = "6";
  public static final String MAIN_DIAGNOSIS_ID = "huvuddiagnos";
  private final GetCertificateAggregator getCertificateAggregator;
  //private final GetCertificateFacadeService getCertificateFromCS;
  //private final CSIntegrationService csIntegrationService;

  @Override
  public SrsCertificate getSrsCertificate(String certificateId) {
    final var certificate = getCertificateAggregator.getCertificate(certificateId, false, true);

    if (isCertificateInWebcert(certificate)) {
      return null; // signed and huvuddiagnos is missing, go to webcert
    }

    if (isCertificateMetaDataPresent(certificate) && certificate.getData().get(
            CATEGORY_DIAGNOSIS_ID)
        .getValue() instanceof CertificateDataValueDiagnosisList diagnosisList) {
      final var diagnosisCode = diagnosisList
          .getList().stream()
          .filter(diagnosis -> diagnosis.getId().equals(MAIN_DIAGNOSIS_ID))
          .findFirst()
          .get()
          .getCode();

      return new SrsCertificate(
          certificate.getMetadata().getId(),
          diagnosisCode,
          certificate.getMetadata().getSigned() != null ? certificate.getMetadata().getSigned().toLocalDate() : null,
          certificate.getMetadata().getRelations().getParent() != null ?
              certificate.getMetadata().getRelations().getParent().getCertificateId() : null
      );
    }
    return null;
  }

  private static boolean isCertificateInWebcert(Certificate certificate) {
    return certificate.getMetadata().getTypeVersion() != null && certificate.getMetadata()
        .getTypeVersion().equals("1.3");
  }

  private static boolean isCertificateMetaDataPresent(Certificate certificate) {
    return certificate != null && certificate.getMetadata() != null;
  }

//  @Override
//  public SrsCertificate getSrsCertificate(String certificateId) {
//    final var sickLeaveCertificate = csIntegrationService.getSickLeaveCertificate(certificateId);
//    if (sickLeaveCertificate != null && sickLeaveCertificate.isPresent() && sickLeaveCertificate.get().isAvailable()) {
//      return new SrsCertificate(
//          sickLeaveCertificate.get().getSickLeaveCertificate().getId(),
//          sickLeaveCertificate.get().getSickLeaveCertificate().getDiagnoseCode(),
//          sickLeaveCertificate.get().getSickLeaveCertificate().getSignedDateTime().toLocalDate(),
//          sickLeaveCertificate.get().getSickLeaveCertificate().getExtendsCertificateId()
//      );
//    }
//    return null;
//  }
}
