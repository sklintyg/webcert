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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.web.csintegration.certificate.PublishCertificateStatusUpdateService;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;

@ExtendWith(MockitoExtension.class)
class NotificationRedeliveryForCertificateServiceTest {

  private static final String CERTIFICATE_ID = "certificateId";
  @Mock CSIntegrationService csIntegrationService;
  @Mock PublishCertificateStatusUpdateService publishCertificateStatusUpdateService;

  @InjectMocks
  NotificationRedeliveryForCertificateService notificationRedeliveryForCertificateService;

  @Test
  void shallReturnFalseIfCertificateDoesNotExistInCertificateService() {
    final var event = mock(Handelse.class);
    final var notificationRedelivery = mock(NotificationRedelivery.class);

    doReturn(CERTIFICATE_ID).when(event).getIntygsId();
    doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);

    assertFalse(notificationRedeliveryForCertificateService.resend(notificationRedelivery, event));
  }

  @Test
  void shallReturnTrueIfCertificateDoesExistInCertificateService() {
    final var event = mock(Handelse.class);
    final var certificate = mock(Certificate.class);
    final var notificationRedelivery = mock(NotificationRedelivery.class);

    doReturn(CERTIFICATE_ID).when(event).getIntygsId();
    doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
    doReturn(certificate).when(csIntegrationService).getInternalCertificate(CERTIFICATE_ID);

    assertTrue(notificationRedeliveryForCertificateService.resend(notificationRedelivery, event));
    verify(publishCertificateStatusUpdateService)
        .resend(certificate, event, notificationRedelivery);
  }
}
