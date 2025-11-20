package se.inera.intyg.webcert.web.service.utkast;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventProcessedRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class HandleStaleDraftsServiceTest {

    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final LocalDateTime STALE_DRAFTS_PERIOD = LocalDateTime.now().minusMonths(3);
    private static final long PERIOD = ChronoUnit.DAYS.between(STALE_DRAFTS_PERIOD.toLocalDate(), LocalDate.now());
    @Mock
    private MonitoringLogService monitoringLogService;
    @Mock
    private NotificationService notificationService;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private HandelseRepository handelseRepository;

    @Mock
    private CertificateEventRepository certificateEventRepository;

    @Mock
    private CertificateEventFailedLoadRepository certificateEventFailedLoadRepository;

    @Mock
    private CertificateEventProcessedRepository certificateEventProcessedRepository;

    @InjectMocks
    private HandleStaleDraftsService handleStaleDraftsService;

    @Test
    void shouldDeleteDraftsAndSendNotifications() {
        final var draft1 = createUtkast("cert-id-1");
        final var draft2 = createUtkast("cert-id-2");
        final var draft3 = createUtkast("cert-id-3");
        final var drafts = List.of(draft1, draft2, draft3);

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(utkastRepository).deleteAllById(List.of("cert-id-1", "cert-id-2", "cert-id-3"));
        verify(notificationService).sendNotificationForDraftDeleted(draft1);
        verify(notificationService).sendNotificationForDraftDeleted(draft2);
        verify(notificationService).sendNotificationForDraftDeleted(draft3);
    }

    @Test
    void shouldDeleteSingleDraftAndSendNotification() {
        final var draft = createUtkast("cert-id-1");
        final var drafts = List.of(draft);

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(utkastRepository).deleteAllById(List.of("cert-id-1"));
        verify(notificationService).sendNotificationForDraftDeleted(draft);
    }

    @Test
    void shouldHandleEmptyListWithoutErrors() {
        final var drafts = Collections.<Utkast>emptyList();

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(utkastRepository).deleteAllById(Collections.emptyList());
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldDeleteDraftsInCorrectOrder() {
        final var draft1 = createUtkast("cert-id-1");
        final var draft2 = createUtkast("cert-id-2");
        final var drafts = List.of(draft1, draft2);

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(utkastRepository).deleteAllById(List.of("cert-id-1", "cert-id-2"));
    }

    @Test
    void shouldMonitorLogUtkastPruned() {
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1);
        final var draft2 = createUtkast(certificateId2);
        final var drafts = List.of(draft1, draft2);

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(monitoringLogService).logUtkastPruned(certificateId1, CERTIFICATE_TYPE, PERIOD);
        verify(monitoringLogService).logUtkastPruned(certificateId2, CERTIFICATE_TYPE, PERIOD);
    }

    @Test
    void shouldDeleteHandelse(){
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1);
        final var draft2 = createUtkast(certificateId2);
        final var drafts = List.of(draft1, draft2);

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(handelseRepository).eraseHandelseByCertificateIds(List.of(certificateId1, certificateId2));
    }

    @Test
    void shouldDeleteCertificateProcessedEvents(){
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1);
        final var draft2 = createUtkast(certificateId2);
        final var drafts = List.of(draft1, draft2);

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(certificateEventProcessedRepository).eraseEventsProcessedByCertificateIds(List.of(certificateId1, certificateId2));
    }

    @Test
    void shouldDeleteCertificateFailedLoadEvents(){
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1);
        final var draft2 = createUtkast(certificateId2);
        final var drafts = List.of(draft1, draft2);

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(certificateEventFailedLoadRepository).eraseEventsFailedByCertificateIds(List.of(certificateId1, certificateId2));
    }
    @Test
    void shouldDeleteCertificateEvents(){
        final var certificateId1 = "cert-id-1";
        final var certificateId2 = "cert-id-2";
        final var draft1 = createUtkast(certificateId1);
        final var draft2 = createUtkast(certificateId2);
        final var drafts = List.of(draft1, draft2);

        handleStaleDraftsService.deleteAndNotify(drafts, STALE_DRAFTS_PERIOD);

        verify(certificateEventRepository).eraseCertificateEventsByCertificateIds(List.of(certificateId1, certificateId2));
    }

    private Utkast createUtkast(String certificateId) {
        final var utkast = new Utkast();
        utkast.setIntygsId(certificateId);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        return utkast;
    }
}