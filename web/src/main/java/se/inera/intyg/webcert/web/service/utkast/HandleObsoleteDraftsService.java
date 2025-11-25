package se.inera.intyg.webcert.web.service.utkast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventProcessedRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@Service
@RequiredArgsConstructor
public class HandleObsoleteDraftsService {

    private final NotificationService notificationService;
    private final UtkastRepository utkastRepository;
    private final MonitoringLogService monitoringLogService;
    private final CertificateEventRepository certificateEventRepository;
    private final CertificateEventFailedLoadRepository certificateEventFailedLoadRepository;
    private final CertificateEventProcessedRepository certificateEventProcessedRepository;
    private final PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;
    private final CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Transactional
    public void disposeAndNotify(Utkast draft, LocalDateTime obsoleteDraftsPeriod) {
        if (!draft.eligeableForDisposal()) {
            return;
        }
        certificateEventProcessedRepository.eraseEventsProcessedByCertificateId(draft.getIntygsId());
        certificateEventFailedLoadRepository.eraseEventsFailedByCertificateId(draft.getIntygsId());
        certificateEventRepository.eraseCertificateEventsByCertificateId(draft.getIntygsId());
        utkastRepository.deleteById(draft.getIntygsId());

        notificationService.sendNotificationForDraftDeleted(draft);

        final var period = ChronoUnit.DAYS.between(obsoleteDraftsPeriod.toLocalDate(), LocalDate.now());
        monitoringLogService.logUtkastDisposed(draft.getIntygsId(), draft.getIntygsTyp(), period);
        publishCertificateAnalyticsMessage.publishEvent(certificateAnalyticsMessageFactory.draftDisposed(draft));

    }
}