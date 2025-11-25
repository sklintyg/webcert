package se.inera.intyg.webcert.web.service.utkast;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    void shouldDeleteCertificateProcessedEvents() {
        final var certificateId1 = "cert-id-1";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);

        handleObsoleteDraftsService.disposeAndNotify(draft1, STALE_DRAFTS_PERIOD);

        verify(certificateEventProcessedRepository).eraseEventsProcessedByCertificateId(certificateId1);
    }

    @Test
    void shouldDeleteCertificateFailedLoadEvents() {
        final var certificateId1 = "cert-id-1";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);

        handleObsoleteDraftsService.disposeAndNotify(draft1, STALE_DRAFTS_PERIOD);

        verify(certificateEventFailedLoadRepository).eraseEventsFailedByCertificateId(certificateId1);
    }

    @Test
    void shouldDeleteCertificateEvents() {
        final var certificateId1 = "cert-id-1";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);

        handleObsoleteDraftsService.disposeAndNotify(draft1, STALE_DRAFTS_PERIOD);

        verify(certificateEventRepository).eraseCertificateEventsByCertificateId(certificateId1);
    }

    @Test
    void shouldVerifyDraftIsStillDraft() {
        final var certificateId1 = "cert-id-1";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.SIGNED);

        handleObsoleteDraftsService.disposeAndNotify(draft1, STALE_DRAFTS_PERIOD);

        verifyNoInteractions(utkastRepository);
        verifyNoInteractions(utkastRepository);
        verifyNoInteractions(certificateEventProcessedRepository);
        verifyNoInteractions(certificateEventFailedLoadRepository);
        verifyNoInteractions(certificateEventRepository);
    }

    @Test
    void shouldPublishAnalyticsMessageForDisposedDrafts() {
        final var certificateId1 = "cert-id-1";
        final var draft1 = createUtkast(certificateId1, UtkastStatus.DRAFT_COMPLETE);

        final var certificateAnalyticsMessage = CertificateAnalyticsMessage.builder().build();
        when(certificateAnalyticsMessageFactory.draftDisposed(draft1)).thenReturn(certificateAnalyticsMessage);

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