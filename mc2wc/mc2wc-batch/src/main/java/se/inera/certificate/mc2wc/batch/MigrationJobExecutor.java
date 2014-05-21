package se.inera.certificate.mc2wc.batch;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import se.inera.certificate.mc2wc.ApplicationConsoleLogger;

@Component
public class MigrationJobExecutor {

	private static final String MIGRATION_JOB = "migrationJob";
	
	private static final int RUNNING = 1;
	private static final int FAILED = -1;
	private static final int FINISHED = 0;
	
	private static Logger logger = LoggerFactory.getLogger(ApplicationConsoleLogger.NAME);

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier(MIGRATION_JOB)
	private Job migrationJob;

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private JobOperator jobOperator;

	public Long startMigration() throws Exception {
		logger.info("Starting job '{}'", MIGRATION_JOB);

		Set<JobExecution> executions = jobExplorer
				.findRunningJobExecutions(MIGRATION_JOB);

		if (!executions.isEmpty()) {
			logger.error("Job '{}' is already running!", MIGRATION_JOB);
			return null;
		}
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addLong("millis", System.currentTimeMillis(), true);

		JobExecution execution = jobLauncher.run(migrationJob, builder.toJobParameters());
		
		return execution.getId();
	}

	public int checkMigrationJob(Long execId) {
		
		JobExecution execution = jobExplorer.getJobExecution(execId);

        if (execution == null) {
        	logger.info("Execution with id '{}' for job '{}' was not found!", execId, MIGRATION_JOB);
            return FINISHED;
        }
                
		logger.info("Job '{}': {}", MIGRATION_JOB, execution.getStatus());
		
		return (execution.isRunning()) ? RUNNING : FINISHED;
	}

}
