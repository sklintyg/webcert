package se.inera.intyg.webcert.web.service.utkast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    public void disposeAndNotify(List<Utkast> drafts, LocalDateTime obsoleteDraftsPeriod) {
        final var certificateIds = drafts.stream()
            .filter(Utkast::eligeableForDisposal)
            .map(Utkast::getIntygsId)
            .toList();

        if (certificateIds.isEmpty()) {
            return;
        }

        certificateEventProcessedRepository.eraseEventsProcessedByCertificateIds(certificateIds);
        certificateEventFailedLoadRepository.eraseEventsFailedByCertificateIds(certificateIds);
        certificateEventRepository.eraseCertificateEventsByCertificateIds(certificateIds);
        utkastRepository.deleteAllById(certificateIds);

        drafts.forEach(notificationService::sendNotificationForDraftDeleted);

        final var period = ChronoUnit.DAYS.between(obsoleteDraftsPeriod.toLocalDate(), LocalDate.now());
        drafts.forEach(draft -> monitoringLogService.logUtkastDisposed(draft.getIntygsId(), draft.getIntygsTyp(), period));
        drafts.forEach(draft -> publishCertificateAnalyticsMessage.publishEvent(certificateAnalyticsMessageFactory.draftDisposed(draft)));
    }
}