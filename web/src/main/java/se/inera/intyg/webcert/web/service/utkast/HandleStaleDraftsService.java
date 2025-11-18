package se.inera.intyg.webcert.web.service.utkast;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.notification.NotificationService;

@Service
@RequiredArgsConstructor
public class HandleStaleDraftsService {

    private final NotificationService notificationService;

    private final UtkastRepository utkastRepository;


    @Transactional
    public void deleteAndNotify(List<Utkast> drafts) {
        final var certificateIds = drafts.stream()
            .map(Utkast::getIntygsId)
            .toList();

        utkastRepository.deleteAllById(certificateIds);
        drafts.forEach(notificationService::sendNotificationForDraftDeleted);
    }
}