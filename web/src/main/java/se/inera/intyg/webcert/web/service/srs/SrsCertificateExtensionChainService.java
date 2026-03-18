/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

  private void addSrsCertificate(
      String certificateId,
      List<SrsCertificate> srsCertificates,
      AtomicInteger extensionChainLengthCounter) {
    final var srsCert = getSrsCertificateAggregator.getSrsCertificate(certificateId);
    srsCertificates.add(srsCert);
    extensionChainLengthCounter.incrementAndGet();

    if (srsCert.getExtendsCertificateId() != null
        && extensionChainLengthCounter.get() < MAX_CHAIN_LENGTH) {
      addSrsCertificate(
          srsCert.getExtendsCertificateId(), srsCertificates, extensionChainLengthCounter);
    }
  }
}
