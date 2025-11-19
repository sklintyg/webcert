package se.inera.intyg.webcert.web.service.utkast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void deleteAndNotify(List<Utkast> drafts, LocalDateTime staleDraftsPeriod) {
        final var certificateIds = drafts.stream()
            .map(Utkast::getIntygsId)
            .toList();

        utkastRepository.deleteAllById(certificateIds);
        drafts.forEach(notificationService::sendNotificationForDraftDeleted);

        final var period = Period.between(LocalDate.now(), staleDraftsPeriod.toLocalDate());
        drafts.forEach(draft -> monitoringLogService.logUtkastPruned(draft.getIntygsId(), draft.getIntygsTyp(), period));
    }
}