package se.inera.webcert.scheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.model.OmsandningOperation;
import se.inera.webcert.persistence.utkast.model.ScheduleratJobb;
import se.inera.webcert.persistence.utkast.repository.OmsandningRepositoryCustom;
import se.inera.webcert.persistence.utkast.repository.ScheduleratJobbRepository;
import se.inera.webcert.service.intyg.IntygOmsandningService;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class OmsandningJobTest {

    @Mock
    private OmsandningRepositoryCustom omsandningRepository;

    @Mock
    private ScheduleratJobbRepository jobbRepository;
    
    @Mock
    private IntygOmsandningService intygService;

    @Mock
    private TransactionTemplate transactionTemplate;
    
    private int logCount;
    
    @InjectMocks
    OmsandningJob job = new OmsandningJob() {
        @Override
        protected void logTooManyFailures() {
            super.logTooManyFailures();
            logCount++;
        }
    };

    @Before
    public void setUp() {
        job.setTransactionTemplate(transactionTemplate);
    }
    
    @Test
    public void testScheduleratJobbPagar() {
        ScheduleratJobb jobb = new ScheduleratJobb(OmsandningJob.JOBB_ID);
        jobb.setBearbetas(true);
        when(jobbRepository.findOne(OmsandningJob.JOBB_ID)).thenReturn(jobb);

        List<Omsandning> list = new ArrayList<>();
        list.add(new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-1", "typ"));
        
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThan(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);

        job.sandOm();

        Mockito.verifyNoMoreInteractions(intygService);
        Mockito.verify(jobbRepository, Mockito.times(0)).save(any(ScheduleratJobb.class));
    }
    
    
    @Test
    public void testScheduleratJobbPagarOptimistisktLas() {
        ScheduleratJobb jobb = new ScheduleratJobb(OmsandningJob.JOBB_ID);
        when(jobbRepository.findOne(OmsandningJob.JOBB_ID)).thenReturn(jobb);
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenThrow(new OptimisticLockingFailureException(""));

        List<Omsandning> list = new ArrayList<>();
        list.add(new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-1", "typ"));
        
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThan(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);

        job.sandOm();

        Mockito.verifyNoMoreInteractions(intygService);
        
        Mockito.verify(jobbRepository, Mockito.times(0)).save(any(ScheduleratJobb.class));
    }
    
    @Test
    public void testSandOm1Intyg() {
        ScheduleratJobb jobb = new ScheduleratJobb(OmsandningJob.JOBB_ID);
        when(jobbRepository.findOne(OmsandningJob.JOBB_ID)).thenReturn(jobb);
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenReturn(jobb);

        List<Omsandning> list = new ArrayList<>();
        list.add(new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-1", "typ"));
        when(intygService.storeIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.OK);
        
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThan(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);

        job.sandOm();

        Assert.assertEquals(0, logCount);
        
        Mockito.verify(jobbRepository).save(any(ScheduleratJobb.class));
    }
    
    @Test
    public void testSandOm2Intyg() {
        ScheduleratJobb jobb = new ScheduleratJobb(OmsandningJob.JOBB_ID);
        when(jobbRepository.findOne(OmsandningJob.JOBB_ID)).thenReturn(jobb);
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenReturn(jobb);
        List<Omsandning> list = new ArrayList<>();
        list.add(new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-1", "typ1"));
        list.add(new Omsandning(OmsandningOperation.SEND_INTYG, "intyg-2", "typ1"));
        when(intygService.storeIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.OK);
        when(intygService.sendIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.RESCHEDULED);
        
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThan(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);

        job.sandOm();

        Assert.assertEquals(0, logCount);

        Mockito.verify(jobbRepository).save(any(ScheduleratJobb.class));
    }

    @Test
    public void testSandOmAndFailHard() {
        ScheduleratJobb jobb = new ScheduleratJobb(OmsandningJob.JOBB_ID);
        when(jobbRepository.findOne(OmsandningJob.JOBB_ID)).thenReturn(jobb);
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenReturn(jobb);
        List<Omsandning> list = new ArrayList<>();
        for (int i = 0; i < OmsandningJob.MAX_RESENDS_PER_CYCLE + 1; i++) {
            list.add(new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-" + i, "typ" + i));
        }
        when(intygService.storeIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.RESCHEDULED);
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThan(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);

        job.sandOm();

        Assert.assertEquals(1, logCount);
        
        Mockito.verify(jobbRepository).save(any(ScheduleratJobb.class));
    }

}