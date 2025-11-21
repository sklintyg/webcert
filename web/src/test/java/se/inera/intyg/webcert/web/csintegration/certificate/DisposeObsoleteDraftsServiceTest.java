package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class DisposeObsoleteDraftsServiceTest {

    private static final int PAGE_SIZE = 10;
    private static final LocalDateTime OBSOLETE_DRAFTS_DATE = LocalDateTime.now();
    @Mock
    UtkastService utkastService;
    @Mock
    DisposeObsoleteDraftsFromCertificateService disposeObsoleteDraftsFromCertificateService;
    @InjectMocks
    DisposeObsoleteDraftsService disposeObsoleteDraftsService;

    @Test
    void shouldDisposeObsoleteDraftsInWC() {
        disposeObsoleteDraftsService.dispose(OBSOLETE_DRAFTS_DATE, PAGE_SIZE);
        verify(utkastService).dispose(OBSOLETE_DRAFTS_DATE, PAGE_SIZE);
    }

    @Test
    void shouldDisposeObsoleteDraftsInCS() {
        disposeObsoleteDraftsService.dispose(OBSOLETE_DRAFTS_DATE, PAGE_SIZE);
        verify(disposeObsoleteDraftsFromCertificateService).dispose(OBSOLETE_DRAFTS_DATE);
    }
}