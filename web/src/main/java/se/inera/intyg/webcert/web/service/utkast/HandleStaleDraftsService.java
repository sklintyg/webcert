package se.inera.intyg.webcert.web.service.utkast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventProcessedRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@Service
@RequiredArgsConstructor
public class HandleStaleDraftsService {

    private final NotificationService notificationService;
    private final UtkastRepository utkastRepository;
    private final MonitoringLogService monitoringLogService;
    private final HandelseRepository handelseRepository;
    private final CertificateEventRepository certificateEventRepository;
    private final CertificateEventFailedLoadRepository certificateEventFailedLoadRepository;
    private final CertificateEventProcessedRepository certificateEventProcessedRepository;

    @Transactional
    public void deleteAndNotify(List<Utkast> drafts, LocalDateTime staleDraftsPeriod) {
        final var certificateIds = drafts.stream()
            .filter(Utkast::eligeableForPrune)
            .map(Utkast::getIntygsId)
            .toList();

        if (certificateIds.isEmpty()) {
            return;
        }

        handelseRepository.eraseHandelseByCertificateIds(certificateIds);
        certificateEventProcessedRepository.eraseEventsProcessedByCertificateIds(certificateIds);
        certificateEventFailedLoadRepository.eraseEventsFailedByCertificateIds(certificateIds);
        certificateEventRepository.eraseCertificateEventsByCertificateIds(certificateIds);
        utkastRepository.deleteAllById(certificateIds);

        drafts.forEach(notificationService::sendNotificationForDraftDeleted);

        final var period = ChronoUnit.DAYS.between(staleDraftsPeriod.toLocalDate(), LocalDate.now());
        drafts.forEach(draft -> monitoringLogService.logUtkastPruned(draft.getIntygsId(), draft.getIntygsTyp(), period));
    }
}