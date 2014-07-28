package se.inera.webcert.scheduler;

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.persistence.intyg.model.Omsandning;
import se.inera.webcert.persistence.intyg.repository.OmsandningRepositoryCustom;
import se.inera.webcert.service.intyg.IntygService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OmsandningJobTest {

    @Mock
    private OmsandningRepositoryCustom omsandningRepository;

    @Mock
    private IntygService intygService;

    private int logCount;
    @InjectMocks
    OmsandningJob job = new OmsandningJob() {
        @Override
        protected void logTooManyFailures() {
            super.logTooManyFailures();
            logCount++;
        }
    };

    @Test
    public void testSandOm1Intyg() {
        List<Omsandning> list = new ArrayList<>();
        list.add(new Omsandning());
        when(intygService.storeIntyg(any(Omsandning.class))).thenReturn(true);
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThan(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);

        job.sandOm();

        Assert.assertEquals(0, logCount);
    }

    @Test
    public void testSandOmAndFailHard() {
        List<Omsandning> list = new ArrayList<>();
        for (int i = 0; i < OmsandningJob.MAX_OMSANDNING_PER_CYCLE + 1; i++) {
            list.add(new Omsandning());
        }
        when(intygService.storeIntyg(any(Omsandning.class))).thenReturn(false);
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThan(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);

        job.sandOm();

        Assert.assertEquals(1, logCount);
    }

}