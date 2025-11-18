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
class DeleteStaleDraftsServiceTest {

    private static final int PAGE_SIZE = 10;
    private static final LocalDateTime STALE_DRAFTS_PERIOD = LocalDateTime.now();
    @Mock
    UtkastService utkastService;
    @Mock
    DeleteDraftsFromCertificateService deleteDraftsFromCertificateService;
    @InjectMocks
    DeleteStaleDraftsService deleteStaleDraftsService;

    @Test
    void shouldDeleteStaleAndLockedDraftsInWC() {
        deleteStaleDraftsService.delete(STALE_DRAFTS_PERIOD, PAGE_SIZE);
        verify(utkastService).deleteStaleAndLockedDrafts(STALE_DRAFTS_PERIOD, PAGE_SIZE);
    }

    @Test
    void shouldDeleteStaleAndLockedDraftsInCS() {
        deleteStaleDraftsService.delete(STALE_DRAFTS_PERIOD, PAGE_SIZE);
        verify(deleteDraftsFromCertificateService).delete(STALE_DRAFTS_PERIOD);
    }
}