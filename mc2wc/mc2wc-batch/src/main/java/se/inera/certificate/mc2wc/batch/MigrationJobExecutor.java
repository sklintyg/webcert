package se.inera.certificate.mc2wc.batch;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MigrationJobExecutor {

	private static final String MIGRATION_JOB = "migrationJob";
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier(MIGRATION_JOB)
	private Job migrationJob;

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private JobOperator jobOperator;

	public void startMigration() throws Exception {
		System.out.println("START!");

		Set<JobExecution> executions = jobExplorer
				.findRunningJobExecutions(MIGRATION_JOB);

		if (!executions.isEmpty()) {
			System.err.println("Already running!");
			return;
		}
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addLong("millis", System.currentTimeMillis(), true);

		jobLauncher.run(migrationJob, builder.toJobParameters());
	}

	public void checkMigrationJob() {
		System.out.println("CHECK!");

	}

}
