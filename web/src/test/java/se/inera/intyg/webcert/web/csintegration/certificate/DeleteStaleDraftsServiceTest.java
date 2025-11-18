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

    @Mock
    UtkastService utkastService;
    @InjectMocks
    DeleteStaleDraftsService deleteStaleDraftsService;

    @Test
    void shouldDeleteStaleAndLockedDraftsInWC() {
        final var staleDraftsPeriod = LocalDateTime.now();
        deleteStaleDraftsService.delete(staleDraftsPeriod);
        verify(utkastService).deleteStaleAndLockedDrafts(staleDraftsPeriod);
    }
}