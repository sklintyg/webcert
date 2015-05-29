package se.inera.webcert.scheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.OptimisticLockException;

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.model.OmsandningOperation;
import se.inera.webcert.persistence.utkast.repository.OmsandningRepositoryCustom;
import se.inera.webcert.service.intyg.IntygOmsandningService;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;

@RunWith(MockitoJUnitRunner.class)
public class OmsandningJobTest {

    @Mock
    private OmsandningRepositoryCustom omsandningRepository;

    @Mock
    private IntygOmsandningService intygService;

    @Mock
    private PlatformTransactionManager txManager;

    private int failureCount;
    private int lockCount;
    private int skipCount;
    
    @InjectMocks
    OmsandningJob job = new OmsandningJob() {
        @Override
        protected void logTooManyFailures() {
            super.logTooManyFailures();
            failureCount++;
        }
        @Override
        protected void logLockOmsandning(Omsandning omsandning, boolean success) {
            super.logLockOmsandning(omsandning, success);
            if (success) {
                lockCount++;
            } else {
                skipCount++;
            }
        }
    };

    @Before
    public void setUp() {
        job.setTxManager(txManager);
    }
    
    @Test
    public void testSandOm1Intyg() {
        List<Omsandning> list = new ArrayList<>();
        Omsandning omsandning1 = new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-1", "typ");
        list.add(omsandning1);
        when(intygService.storeIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.OK);
        
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThanNotBearbetas(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);
        when(omsandningRepository.save(omsandning1)).thenReturn(omsandning1);
        job.sandOm();

        Assert.assertEquals(0, failureCount);
    }
    
    @Test
    public void testSandOm2Intyg() {
        List<Omsandning> list = new ArrayList<>();
        Omsandning omsandning1 = new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-1", "typ");
        Omsandning omsandning2 = new Omsandning(OmsandningOperation.SEND_INTYG, "intyg-2", "typ");
        list.add(omsandning1);
        list.add(omsandning2);
        when(intygService.storeIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.OK);
        when(intygService.sendIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.RESCHEDULED);
        
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThanNotBearbetas(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);
        when(omsandningRepository.save(omsandning1)).thenReturn(omsandning1);
        when(omsandningRepository.save(omsandning2)).thenReturn(omsandning2);

        job.sandOm();

        Assert.assertEquals(0, failureCount);
    }

    @Test
    public void testSandOm2IntygOneIsLocked() {
        List<Omsandning> list = new ArrayList<>();
        Omsandning omsandning1 = new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-1", "typ");
        Omsandning omsandning2 = new Omsandning(OmsandningOperation.SEND_INTYG, "intyg-2", "typ");
        list.add(omsandning1);
        list.add(omsandning2);
        when(intygService.sendIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.OK);
        
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThanNotBearbetas(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);
        when(omsandningRepository.save(omsandning2)).thenReturn(omsandning2);

        TransactionStatus tx1 = Mockito.mock(TransactionStatus.class);
        TransactionStatus tx2 = Mockito.mock(TransactionStatus.class);
        
        when(txManager.getTransaction(any(TransactionDefinition.class))).thenReturn(tx1, tx2);
        
        Mockito.doThrow(new OptimisticLockException()).when(txManager).commit(tx1);

        job.sandOm();

        Assert.assertEquals(1, lockCount);
        Assert.assertEquals(1, skipCount);
        
        Mockito.verify(intygService, Mockito.never()).storeIntyg(omsandning1);
        Mockito.verify(intygService).sendIntyg(omsandning2);
    }

    @Test
    public void testSandOmAndFailHard() {
        List<Omsandning> list = new ArrayList<>();
        Omsandning omsandning = new Omsandning(OmsandningOperation.STORE_INTYG, "intyg", "typ");
        for (int i = 0; i < OmsandningJob.MAX_RESENDS_PER_CYCLE; i++) {
            list.add(new Omsandning(OmsandningOperation.STORE_INTYG, "intyg-" + i, "typ" + i));
        }
        list.add(omsandning);
        when(intygService.storeIntyg(any(Omsandning.class))).thenReturn(IntygServiceResult.RESCHEDULED);
        when(omsandningRepository.findByGallringsdatumGreaterThanAndNastaForsokLessThanNotBearbetas(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(list);
        when(omsandningRepository.save(any(Omsandning.class))).thenReturn(omsandning);

        job.sandOm();

        Assert.assertEquals(1, failureCount);
    }

}