package se.inera.intyg.webcert.web.csintegration.certificate;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteStaleDraftsService {

    private final UtkastService utkastService;
    private final DeleteDraftsFromCertificateService deleteDraftsFromCertificateService;

    public void delete(LocalDateTime staleDraftsPeriod) {
        log.info("Staring job to delete stale drafts");

        final var deletedStaleDraftsWC = utkastService.deleteStaleAndLockedDrafts(staleDraftsPeriod);
        final var deletedStaleDraftsCS = deleteDraftsFromCertificateService.delete(staleDraftsPeriod);

        log.info("Successfully deleted {} stale drafts - {} in Webcert - {} in CertificateService",
            deletedStaleDraftsWC + deletedStaleDraftsCS, deletedStaleDraftsWC, deletedStaleDraftsCS);
    }
}