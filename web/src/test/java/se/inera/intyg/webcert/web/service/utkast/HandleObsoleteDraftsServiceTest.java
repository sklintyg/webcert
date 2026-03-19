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
package se.inera.intyg.webcert.web.service.utkast;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class HandleObsoleteDraftsServiceTest {

  private static final String CERTIFICATE_TYPE = "certificateType";
  private static final LocalDateTime STALE_DRAFTS_PERIOD = LocalDateTime.now().minusMonths(3);
  private static final long PERIOD =
      ChronoUnit.DAYS.between(STALE_DRAFTS_PERIOD.toLocalDate(), LocalDate.now());

  @Mock PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
  @Mock CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
  @Mock private MonitoringLogService monitoringLogService;
  @Mock private NotificationService notificationService;
  @Mock private UtkastRepository utkastRepository;
  @Mock private CertificateEventRepository certificateEventRepository;
  @InjectMocks private HandleObsoleteDraftsService handleObsoleteDraftsService;

  @Test
  void shouldDeleteSingleDraftAndSendNotification() {
    final var draft = createUtkast("cert-id-1", UtkastStatus.DRAFT_COMPLETE);

    handleObsoleteDraftsService.disposeAndNotify(draft, STALE_DRAFTS_PERIOD);

    verify(utkastRepository).deleteById("cert-id-1");
    verify(notificationService).sendNotificationForDraftDeleted(draft);
  }

  @Test
  void shouldMonitorLogUtkastDisposed() {
    final var certificateId1 = "cert-id-1";
    final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);

    handleObsoleteDraftsService.disposeAndNotify(draft1, STALE_DRAFTS_PERIOD);

    verify(monitoringLogService).logUtkastDisposed(certificateId1, CERTIFICATE_TYPE, PERIOD);
  }

  @Test
  void shouldDeleteCertificateEvents() {
    final var certificateId1 = "cert-id-1";
    final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);

    handleObsoleteDraftsService.disposeAndNotify(draft1, STALE_DRAFTS_PERIOD);

    verify(certificateEventRepository)
        .eraseCertificateEventsByCertificateIds(Collections.singletonList(certificateId1));
  }

  @Test
  void shouldVerifyDraftIsStillDraft() {
    final var certificateId1 = "cert-id-1";
    final var draft1 = createUtkast(certificateId1, UtkastStatus.SIGNED);

    handleObsoleteDraftsService.disposeAndNotify(draft1, STALE_DRAFTS_PERIOD);

    verifyNoInteractions(utkastRepository);
    verifyNoInteractions(utkastRepository);
    verifyNoInteractions(certificateEventRepository);
  }

  @Test
  void shouldPublishAnalyticsMessageForDisposedDrafts() {
    final var certificateId1 = "cert-id-1";
    final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);

    final var certificateAnalyticsMessage = CertificateAnalyticsMessage.builder().build();
    when(certificateAnalyticsMessageFactory.draftDisposed(draft1))
        .thenReturn(certificateAnalyticsMessage);

    handleObsoleteDraftsService.disposeAndNotify(draft1, STALE_DRAFTS_PERIOD);

    verify(publishCertificateAnalyticsMessage, times(1)).publishEvent(certificateAnalyticsMessage);
  }

  private Utkast createUtkast(String certificateId, UtkastStatus status) {
    final var utkast = new Utkast();
    utkast.setIntygsId(certificateId);
    utkast.setIntygsTyp(CERTIFICATE_TYPE);
    utkast.setStatus(status);
    return utkast;
  }
}
