package se.inera.intyg.webcert.web.service.srs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetCertificateAggregator;

@Service("srsCertificateExtensionChainService")
@RequiredArgsConstructor
public class SrsCertificateExtensionChainService {

  private final GetSrsCertificateAggregator getSrsCertificateAggregator;

  public List<SrsCertificate> get(String certificateId) {
    final var srsCertificates = new ArrayList<SrsCertificate>();

    if (certificateId == null) {
      return srsCertificates;
    }

    addSrsCertificate(certificateId, srsCertificates);
    return srsCertificates;
  }

  private void addSrsCertificate(String certificateId, List<SrsCertificate> srsCertificates) {
    final var srsCert= getSrsCertificateAggregator.getSrsCertificate(certificateId);
    srsCertificates.add(srsCert);

    if (srsCert.getExtendsCertificateId() != null) {
      addSrsCertificate(srsCert.getExtendsCertificateId(), srsCertificates);
    }
  }

}