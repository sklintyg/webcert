package se.inera.intyg.webcert.web.csintegration.certificate;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisposeObsoleteDraftsService {

    private final UtkastService utkastService;
    private final DeleteDraftsFromCertificateService deleteDraftsFromCertificateService;

    public void dispose(LocalDateTime obsoleteDraftsPeriod, Integer pageSize) {
        log.info("Starting job to dispose obsolete drafts");

        final var obsoleteDraftsWC = utkastService.dispose(obsoleteDraftsPeriod, pageSize);
        final var obsoleteDraftsCS = deleteDraftsFromCertificateService.delete(obsoleteDraftsPeriod);

        log.info("Successfully disposed {} obsolete drafts - {} in Webcert - {} in CertificateService",
            obsoleteDraftsWC + obsoleteDraftsCS, obsoleteDraftsWC, obsoleteDraftsCS);
    }
}