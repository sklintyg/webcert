package se.inera.intyg.webcert.web.service.utkast;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventProcessedRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class HandleObsoleteDraftsServiceTest {

    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final LocalDateTime STALE_DRAFTS_PERIOD = LocalDateTime.now().minusMonths(3);
    private static final long PERIOD = ChronoUnit.DAYS.between(STALE_DRAFTS_PERIOD.toLocalDate(), LocalDate.now());

    @Mock
    PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    @Mock
    CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;
    @Mock
    private MonitoringLogService monitoringLogService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private CertificateEventRepository certificateEventRepository;
    @Mock
    private CertificateEventFailedLoadRepository certificateEventFailedLoadRepository;
    @Mock
    private CertificateEventProcessedRepository certificateEventProcessedRepository;
    @InjectMocks
    private HandleObsoleteDraftsService handleObsoleteDraftsService;

    @Test
    void shouldDeleteDraftsAndSendNotifications() {
        final var draft1 = createUtkast("cert-id-1", UtkastStatus.DRAFT_COMPLETE);
        final var draft2 = createUtkast("cert-id-2", UtkastStatus.DRAFT_COMPLETE);
        final var draft3 = createUtkast("cert-id-3", UtkastStatus.DRAFT_COMPLETE);
        final var drafts = List.of(draft1, draft2, draft3);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(utkastRepository).deleteAllById(List.of("cert-id-1", "cert-id-2", "cert-id-3"));
        verify(notificationService).sendNotificationForDraftDeleted(draft1);
        verify(notificationService).sendNotificationForDraftDeleted(draft2);
        verify(notificationService).sendNotificationForDraftDeleted(draft3);
    }

    @Test
    void shouldDeleteSingleDraftAndSendNotification() {
        final var draft = createUtkast("cert-id-1", UtkastStatus.DRAFT_COMPLETE);
        final var drafts = List.of(draft);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(utkastRepository).deleteAllById(List.of("cert-id-1"));
        verify(notificationService).sendNotificationForDraftDeleted(draft);
    }

    @Test
    void shouldHandleEmptyListWithoutErrors() {
        final var drafts = Collections.<Utkast>emptyList();

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verifyNoInteractions(utkastRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldDeleteDraftsInCorrectOrder() {
        final var draft1 = createUtkast("cert-id-1", UtkastStatus.DRAFT_COMPLETE);
        final var draft2 = createUtkast("cert-id-2", UtkastStatus.DRAFT_COMPLETE);
        final var drafts = List.of(draft1, draft2);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(utkastRepository).deleteAllById(List.of("cert-id-1", "cert-id-2"));
    }

    @Test
    void shouldMonitorLogUtkastDisposed() {
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);
        final var draft2 = createUtkast(certificateId2, UtkastStatus.DRAFT_COMPLETE);
        final var drafts = List.of(draft1, draft2);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(monitoringLogService).logUtkastDisposed(certificateId1, CERTIFICATE_TYPE, PERIOD);
        verify(monitoringLogService).logUtkastDisposed(certificateId2, CERTIFICATE_TYPE, PERIOD);
    }

    @Test
    void shouldDeleteCertificateProcessedEvents() {
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);
        final var draft2 = createUtkast(certificateId2, UtkastStatus.DRAFT_COMPLETE);
        final var drafts = List.of(draft1, draft2);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(certificateEventProcessedRepository).eraseEventsProcessedByCertificateIds(List.of(certificateId1, certificateId2));
    }

    @Test
    void shouldDeleteCertificateFailedLoadEvents() {
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);
        final var draft2 = createUtkast(certificateId2, UtkastStatus.DRAFT_COMPLETE);
        final var drafts = List.of(draft1, draft2);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(certificateEventFailedLoadRepository).eraseEventsFailedByCertificateIds(List.of(certificateId1, certificateId2));
    }

    @Test
    void shouldDeleteCertificateEvents() {
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);
        final var draft2 = createUtkast(certificateId2, UtkastStatus.DRAFT_COMPLETE);
        final var drafts = List.of(draft1, draft2);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(certificateEventRepository).eraseCertificateEventsByCertificateIds(List.of(certificateId1, certificateId2));
    }

    @Test
    void shouldVerifyDraftIsStillDraft() {
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.SIGNED);
        final var draft2 = createUtkast(certificateId2, UtkastStatus.SIGNED);
        final var drafts = List.of(draft1, draft2);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verifyNoInteractions(utkastRepository);
        verifyNoInteractions(utkastRepository);
        verifyNoInteractions(certificateEventProcessedRepository);
        verifyNoInteractions(certificateEventFailedLoadRepository);
        verifyNoInteractions(certificateEventRepository);
    }

    @Test
    void shouldPublishAnalyticsMessageForDisposedDrafts() {
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);
        final var draft2 = createUtkast(certificateId2, UtkastStatus.DRAFT_COMPLETE);
        final var drafts = List.of(draft1, draft2);

        final var certificateAnalyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.draftDisposed(draft1)).thenReturn(certificateAnalyticsMessage);
        when(certificateAnalyticsMessageFactory.draftDisposed(draft2)).thenReturn(certificateAnalyticsMessage);

        handleObsoleteDraftsService.disposeAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(publishCertificateAnalyticsMessage, times(2)).publishEvent(certificateAnalyticsMessage);
    }

    private Utkast createUtkast(String certificateId, UtkastStatus status) {
        final var utkast = new Utkast();
        utkast.setIntygsId(certificateId);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        utkast.setStatus(status);
        return utkast;
    }
}