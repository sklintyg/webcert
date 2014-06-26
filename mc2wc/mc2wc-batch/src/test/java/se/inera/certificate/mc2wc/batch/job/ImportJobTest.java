package se.inera.certificate.mc2wc.batch.job;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.certificate.mc2wc.jpa.webcert.WebcertDAO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/application-context.xml"})
@ActiveProfiles({"dev","import","import-unittest"})
public class ImportJobTest {
    
    private Logger logger = LoggerFactory.getLogger(ImportJobTest.class);

    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private WebcertDAO webcertDao;

    @Autowired
    @Qualifier("importJob")
    private Job importJob;  
    
    @Test
    public void testRunImportJob() throws Exception {
        
        JobParametersBuilder builder = new JobParametersBuilder();
        final JobExecution execution = jobLauncher.run(importJob, builder.toJobParameters());

        await().atMost(10, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                logger.debug("exitStatus: {}", execution.getExitStatus());
                return execution.getExitStatus().equals(ExitStatus.COMPLETED);
            }
        });
        
        assertEquals(9, webcertDao.countNbrOfFragaSvar());
        assertEquals(10, webcertDao.countNbrOfMigreratMedcertIntyg());
    }
    
}
