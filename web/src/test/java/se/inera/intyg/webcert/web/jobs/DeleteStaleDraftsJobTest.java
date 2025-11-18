package se.inera.intyg.webcert.web.jobs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.web.csintegration.certificate.DeleteStaleDraftsService;
import se.inera.intyg.webcert.web.csintegration.util.DeleteStaleDraftsProfile;

@ExtendWith(MockitoExtension.class)
class DeleteStaleDraftsJobTest {

    @Mock
    MdcHelper mdcHelper;
    @Mock
    DeleteStaleDraftsService deleteStaleDraftsService;
    @Mock
    DeleteStaleDraftsProfile deleteStaleDraftsProfile;
    @InjectMocks
    DeleteStaleDraftsJob deleteStaleDraftsJob;

    @Test
    void shouldNotExecuteJobIfProfileNotActive() {
        when(deleteStaleDraftsProfile.active()).thenReturn(false);

        deleteStaleDraftsJob.run();

        verifyNoInteractions(mdcHelper);
        verifyNoInteractions(deleteStaleDraftsService);
    }

    @Test
    void shouldExecuteJobIfProfileIsActive() {
        when(deleteStaleDraftsProfile.active()).thenReturn(true);

        deleteStaleDraftsJob.run();

        verify(deleteStaleDraftsService).delete();
    }
}