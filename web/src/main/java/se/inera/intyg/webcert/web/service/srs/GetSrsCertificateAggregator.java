package se.inera.intyg.webcert.web.service.srs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@Service("getSrsCertificateAggregator")
public class GetSrsCertificateAggregator implements GetSrsCertificate {

  private final CSIntegrationService csIntegrationService;
  private final GetSrsCertificate getSrsCertificateFromWC;
  private final GetSrsCertificate getSrsCertificateFromCS;

  public GetSrsCertificateAggregator(
      CSIntegrationService csIntegrationService, @Qualifier("GetSrsCertificateFromWC") GetSrsCertificate getSrsCertificateFromWC,
      @Qualifier("GetSrsCertificateFromCS") GetSrsCertificate getSrsCertificateFromCS) {
    this.csIntegrationService = csIntegrationService;
    this.getSrsCertificateFromWC = getSrsCertificateFromWC;
    this.getSrsCertificateFromCS = getSrsCertificateFromCS;
  }

  @Override
  public SrsCertificate getSrsCertificate(String certificateId) {
    if (Boolean.TRUE.equals(csIntegrationService.certificateExists(certificateId))) {
      return getSrsCertificateFromCS.getSrsCertificate(certificateId);
    }
    return getSrsCertificateFromWC.getSrsCertificate(certificateId);
  }
}
