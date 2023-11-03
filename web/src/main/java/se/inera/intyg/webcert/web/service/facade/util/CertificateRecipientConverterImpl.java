/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.util;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.SendCertificateFunction;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@Service
public class CertificateRecipientConverterImpl implements CertificateRecipientConverter {

  private final CertificateReceiverService certificateReceiverService;
  private final AuthoritiesHelper authoritiesHelper;
  private final SendCertificateFunction sendCertificateFunction;

  public CertificateRecipientConverterImpl(CertificateReceiverService certificateReceiverService,
      AuthoritiesHelper authoritiesHelper, SendCertificateFunction sendCertificateFunction) {
    this.certificateReceiverService = certificateReceiverService;
    this.authoritiesHelper = authoritiesHelper;
    this.sendCertificateFunction = sendCertificateFunction;
  }

  @Override
  public CertificateRecipient get(String type, String certificateId, LocalDateTime sent, boolean isLatestMajorVersion) {
    final var recipients = certificateReceiverService.listPossibleReceiversWithApprovedInfo(type, certificateId);

    if (!authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SKICKA_INTYG, type)
    || sendCertificateFunction.isSendCertificateBlockedForCertificateVersion(type, isLatestMajorVersion)) {
      return null;
    }

    return recipients
        .stream()
        .filter(IntygReceiver::isLocked)
        .findFirst()
        .map(recipient -> CertificateRecipient
            .builder()
            .id(recipient.getId())
            .name(recipient.getName())
            .sent(sent)
            .build())
        .orElse(null);
  }
}
