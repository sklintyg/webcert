package se.inera.intyg.webcert.web.service.srs;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.webcert.web.csintegration.aggregate.GetCertificateAggregator;

@Service("srsCertificateExtensionChainService")
@RequiredArgsConstructor
public class SrsCertificateExtensionChainService {

  //private final GetCertificateAggregator getCertificateAggregator;
  private final GetSrsCertificateAggregator getSrsCertificateAggregator;

  public List<SrsCertificate> get(String certificateId) {
    /*
    final var certificate = getCertificateAggregator.getCertificate(certificateId, false, true);
    final var certificateParentId = certificate.getMetadata().getRelations().getParent() != null ?
        certificate.getMetadata().getRelations().getParent().getCertificateId() : null;

     */

    final var resultList = getSrsCertificateAggregator.getSrsCertificateList(certificateId);
    return resultList;
  }

}
