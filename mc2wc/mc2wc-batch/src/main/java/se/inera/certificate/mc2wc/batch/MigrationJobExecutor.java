package se.inera.certificate.mc2wc.batch;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import se.inera.certificate.mc2wc.Main;

@Component
public class MigrationJobExecutor {

	private static final String MIGRATION_JOB = "migrationJob";
	
	private static final int RUNNING = 1;
	private static final int FAILED = -1;
	private static final int FINISHED = 0;
	
	private static Logger logger = LoggerFactory.getLogger(Main.CONSOLE_LOGGER);

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier(MIGRATION_JOB)
	private Job migrationJob;

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private JobOperator jobOperator;

	public int startMigration() throws Exception {
		logger.info("Running job '{}'", MIGRATION_JOB);

		Set<JobExecution> executions = jobExplorer
				.findRunningJobExecutions(MIGRATION_JOB);

		if (!executions.isEmpty()) {
			logger.error("Job '{}' is already running!", MIGRATION_JOB);
			return FAILED;
		}
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addLong("millis", System.currentTimeMillis(), true);

		jobLauncher.run(migrationJob, builder.toJobParameters());
		
		return RUNNING;
	}

	public int checkMigrationJob() {
		
		Set<JobExecution> executions = jobExplorer.findRunningJobExecutions(MIGRATION_JOB);

        if (executions.isEmpty()) {
        	logger.info("Job '{}' was not found!", MIGRATION_JOB);
            return FINISHED;
        }

        JobExecution execution = executions.iterator().next();
		
		logger.info("Job '{}': {}", MIGRATION_JOB, execution.getStatus());
		
		return RUNNING;
	}

}
