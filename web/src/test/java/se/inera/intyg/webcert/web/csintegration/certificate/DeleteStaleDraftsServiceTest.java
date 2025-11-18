package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.mockito.Mockito.verify;

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
        deleteStaleDraftsService.delete();
        
        verify(utkastService).deleteStaleAndLockedDrafts();
    }
}