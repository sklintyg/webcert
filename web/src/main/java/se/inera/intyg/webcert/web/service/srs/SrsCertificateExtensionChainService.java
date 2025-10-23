package se.inera.intyg.webcert.web.service.srs;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;

@Service("srsCertificateExtensionChainService")
@RequiredArgsConstructor
public class SrsCertificateExtensionChainService {

  private final GetSrsCertificateAggregator getSrsCertificateAggregator;

  public List<SrsCertificate> get(String certificateId) {
    return getSrsCertificateAggregator.getSrsCertificateList(certificateId);
  }

}
