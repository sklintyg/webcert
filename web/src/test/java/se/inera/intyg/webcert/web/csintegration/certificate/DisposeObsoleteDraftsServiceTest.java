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
    private static final LocalDateTime STALE_DRAFTS_PERIOD = LocalDateTime.now();
    @Mock
    UtkastService utkastService;
    @Mock
    DisposeObsoleteDraftsFromCertificateService disposeObsoleteDraftsFromCertificateService;
    @InjectMocks
    DisposeObsoleteDraftsService disposeObsoleteDraftsService;

    @Test
    void shouldDisposeStaleAndLockedDraftsInWC() {
        disposeObsoleteDraftsService.dispose(STALE_DRAFTS_PERIOD, PAGE_SIZE);
        verify(utkastService).dispose(STALE_DRAFTS_PERIOD, PAGE_SIZE);
    }

    @Test
    void shouldDisposeStaleAndLockedDraftsInCS() {
        disposeObsoleteDraftsService.dispose(STALE_DRAFTS_PERIOD, PAGE_SIZE);
        verify(disposeObsoleteDraftsFromCertificateService).dispose(STALE_DRAFTS_PERIOD);
    }
}