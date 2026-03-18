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
package se.inera.intyg.webcert.web.service.notification;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.services.redelivery.NotificationRedeliveryService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRedeliveryAggregator {
  private final NotificationRedeliveryStatusUpdateCreatorService
      notificationRedeliveryStatusUpdateCreatorService;
  private final NotificationRedeliveryService notificationRedeliveryService;
  private final NotificationRedeliveryForCertificateService
      notificationRedeliveryForCertificateService;

  public boolean resend(NotificationRedelivery notificationRedelivery, Handelse event)
      throws TemporaryException,
          ModuleNotFoundException,
          JAXBException,
          IOException,
          ModuleException {

    final var redeliveryHandled =
        notificationRedeliveryForCertificateService.resend(notificationRedelivery, event);

    if (redeliveryHandled) {
      return true;
    }

    notificationRedeliveryService.resend(
        notificationRedelivery,
        event,
        getCertificateStatusUpdateXmlABytes(notificationRedelivery, event));

    return true;
  }

  private byte[] getCertificateStatusUpdateXmlABytes(
      NotificationRedelivery notificationRedelivery, Handelse event)
      throws ModuleNotFoundException,
          TemporaryException,
          ModuleException,
          IOException,
          JAXBException {
    final var statusUpdateXml =
        notificationRedeliveryStatusUpdateCreatorService.getCertificateStatusUpdateXml(
            notificationRedelivery, event);
    return statusUpdateXml.getBytes(StandardCharsets.UTF_8);
  }
}
