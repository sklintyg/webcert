package se.inera.certificate.mc2wc.batch.job;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
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

import se.inera.certificate.mc2wc.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.mc2wc.dbunit.CustomFlatXmlDataSetLoader;
import se.inera.certificate.mc2wc.jpa.Mc2wcDAO;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

@DbUnitConfiguration(databaseConnection = "medcertDataSource", dataSetLoader = CustomFlatXmlDataSetLoader.class)
@DatabaseSetup({"/data/certificate_dataset_25.xml"})
@ActiveProfiles({"export","export-unittest"})
public class ExportJobTest extends AbstractDbUnitSpringTest {

    private Logger logger = LoggerFactory.getLogger(ExportJobTest.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("exportJob")
    private Job exportJob;
    
    @Autowired
    private Mc2wcDAO mc2wcDAO;

    @Test
    public void testRunMigrationJob() throws Exception {

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("sender", "SLL");
        final JobExecution execution = jobLauncher.run(exportJob, builder.toJobParameters());

        await().atMost(10, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                logger.debug("exitStatus: {}", execution.getExitStatus());
                return execution.getExitStatus().equals(ExitStatus.COMPLETED);
            }
        });

        assertEquals("Migrated certs", 11L, mc2wcDAO.countMigratedCertificates().longValue());
        assertEquals("Migrated questions", 9L, mc2wcDAO.sumNbrOfMigratedQuestions().longValue());
        assertEquals("Migrated questions with answers", 5L, mc2wcDAO.sumNbrOfMigratedAnsweredQuestions().longValue());
    }
}
