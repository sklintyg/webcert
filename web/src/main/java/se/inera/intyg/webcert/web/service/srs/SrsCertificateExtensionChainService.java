package se.inera.intyg.webcert.web.service.srs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;

@Service("srsCertificateExtensionChainService")
@RequiredArgsConstructor
public class SrsCertificateExtensionChainService {
  private static final int MAX_CHAIN_LENGTH = 3;

  private final GetSrsCertificateAggregator getSrsCertificateAggregator;

  public List<SrsCertificate> get(String certificateId) {
    final var srsCertificates = new ArrayList<SrsCertificate>();

    if (certificateId == null) {
      return srsCertificates;
    }

    final var extensionChainLengthCounter = new AtomicInteger(0);
    addSrsCertificate(certificateId, srsCertificates, extensionChainLengthCounter);
    return srsCertificates;
  }

  private void addSrsCertificate(String certificateId, List<SrsCertificate> srsCertificates, AtomicInteger extensionChainLengthCounter) {
    final var srsCert= getSrsCertificateAggregator.getSrsCertificate(certificateId);
    srsCertificates.add(srsCert);
    extensionChainLengthCounter.incrementAndGet();

    if (srsCert.getExtendsCertificateId() != null && extensionChainLengthCounter.get() < MAX_CHAIN_LENGTH) {
      addSrsCertificate(srsCert.getExtendsCertificateId(), srsCertificates, extensionChainLengthCounter);
    }
  }

}