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
import se.inera.intyg.webcert.web.csintegration.certificate.DeleteStaleDraftsService;

@ExtendWith(MockitoExtension.class)
class DeleteStaleDraftsJobTest {

    @Mock
    MdcHelper mdcHelper;
    @Mock
    DeleteStaleDraftsService deleteStaleDraftsService;
    @InjectMocks
    DeleteStaleDraftsJob deleteStaleDraftsJob;

    @Test
    void shouldExecuteJob() {
        ReflectionTestUtils.setField(deleteStaleDraftsJob, "staleDraftsPeriod", "P3M");
        ReflectionTestUtils.setField(deleteStaleDraftsJob, "staleDraftsPageSize", 10);

        deleteStaleDraftsJob.run();

        verify(deleteStaleDraftsService).delete(any(LocalDateTime.class), eq(10));
    }
}