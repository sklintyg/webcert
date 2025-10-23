package se.inera.intyg.webcert.web.service.srs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;

@Service("getSrsCertificateAggregator")
public class GetSrsCertificateAggregator implements GetSrsCertificate {

  private static final int MAX_CHAIN_LENGTH = 100; //FIXME: Prevent infinite loops. Really needed ?

  private final GetSrsCertificate getSrsCertificateFromWC;
  private final GetSrsCertificate getSrsCertificateFromCS;

  public GetSrsCertificateAggregator(
      @Qualifier("GetSrsCertificateFromWC") GetSrsCertificate getSrsCertificateFromWC,
      @Qualifier("GetSrsCertificateFromCS") GetSrsCertificate getSrsCertificateFromCS) {
    this.getSrsCertificateFromWC = getSrsCertificateFromWC;
    this.getSrsCertificateFromCS = getSrsCertificateFromCS;
  }

  public List<SrsCertificate> getSrsCertificateList(String certificateId) {
    if (certificateId == null) {
      return new ArrayList<>();
    }

    var srsCertificates = new ArrayList<SrsCertificate>();
    var visitedIds = new HashSet<String>();
    var currentId = certificateId;

    while (currentId != null && srsCertificates.size() < MAX_CHAIN_LENGTH) {

      if (visitedIds.contains(currentId)) {
        break;
      }
      visitedIds.add(currentId);

      final var srsCertificate = getSrsCertificate(currentId);
      if (srsCertificate != null) {
        srsCertificates.add(srsCertificate);
        currentId = srsCertificate.getExtendsCertificateId();
      } else {
        currentId = null;
      }
    }

    return srsCertificates;
  }

  @Override
  public SrsCertificate getSrsCertificate(String certificateId) {

    final var responseFromCS = getSrsCertificateFromCS.getSrsCertificate(certificateId);
    return responseFromCS != null
        ? responseFromCS
        : getSrsCertificateFromWC.getSrsCertificate(certificateId);
  }
}
