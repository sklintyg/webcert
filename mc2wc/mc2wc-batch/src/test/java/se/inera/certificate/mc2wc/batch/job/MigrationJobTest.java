package se.inera.certificate.mc2wc.batch.job;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import se.inera.certificate.mc2wc.batch.writer.MockMigrationRecieverBean;
import se.inera.certificate.mc2wc.dbunit.AbstractDbUnitSpringTest;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

@ContextConfiguration(locations = {"/spring/rest-client-test-context.xml",
        "/spring/rest-client-context.xml", "/spring/batch-infrastructure-context.xml",
        "/spring/beans-context.xml", "/spring/migration-job-context.xml"})
@DatabaseSetup({"/data/certificate_dataset_25.xml"})
public class MigrationJobTest extends AbstractDbUnitSpringTest {

    private Logger logger = LoggerFactory.getLogger(MigrationJobTest.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("migrationJob")
    private Job migrationJob;
    @Autowired
    private MockMigrationRecieverBean recieverBean;

    @Test
    public void testRunMigrationJob() throws Exception {

        JobParameters params = new JobParameters();
        final JobExecution execution = jobLauncher.run(migrationJob, params);

        await().atMost(10, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                logger.debug("exitStatus: {}", execution.getExitStatus());
                return execution.getExitStatus().equals(ExitStatus.COMPLETED);
            }
        });

        assertEquals(11, recieverBean.getMessages().size());
    }
}
