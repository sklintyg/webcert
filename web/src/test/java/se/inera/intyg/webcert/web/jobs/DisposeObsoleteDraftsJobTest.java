package se.inera.intyg.webcert.web.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.web.csintegration.certificate.DisposeObsoleteDraftsService;

@ExtendWith(MockitoExtension.class)
class DisposeObsoleteDraftsJobTest {

    @Mock
    MdcHelper mdcHelper;
    @Mock
    DisposeObsoleteDraftsService disposeObsoleteDraftsService;
    @InjectMocks
    DisposeObsoleteDraftsJob disposeObsoleteDraftsJob;

    @Test
    void shouldExecuteJob() {
        ReflectionTestUtils.setField(disposeObsoleteDraftsJob, "staleDraftsPeriod", "P3M");
        ReflectionTestUtils.setField(disposeObsoleteDraftsJob, "staleDraftsPageSize", 10);

        disposeObsoleteDraftsJob.run();

        verify(disposeObsoleteDraftsService).dispose(any(LocalDateTime.class), eq(10));
    }
}