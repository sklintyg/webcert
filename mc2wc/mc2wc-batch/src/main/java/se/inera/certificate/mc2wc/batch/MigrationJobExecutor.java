package se.inera.certificate.mc2wc.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.mc2wc.ApplicationConsoleLogger;
import se.inera.certificate.mc2wc.ApplicationMode;

@Component
public class MigrationJobExecutor {
	
	private static final int RUNNING = 1;
	private static final int FAILED = -1;
	private static final int FINISHED = 0;
	
	private static Logger logger = LoggerFactory.getLogger(ApplicationConsoleLogger.NAME);

	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private JobLocator jobLocator;
	
	@Autowired
	private JobExplorer jobExplorer;

	public Long startJob(ApplicationMode appMode) throws Exception {
				
		String jobName = appMode.jobName();
		Job job = jobLocator.getJob(jobName); 
		
		logger.info("Located job '{}'", job.getName());
		
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addLong("millis", System.currentTimeMillis(), true);

		JobExecution execution = jobLauncher.run(job, builder.toJobParameters());
		
		logger.info("Started job '{}'", execution.getJobInstance().getJobName());
		
		return execution.getId();
	}

	public int checkJob(Long execId) {
		
		JobExecution execution = jobExplorer.getJobExecution(execId);
		
        if (execution == null) {
        	logger.info("Execution with id '{}' was not found!", execId);
            return FAILED;
        }
                
		logger.info("Job '{}': {}", execution.getJobInstance().getJobName(), execution.getStatus());
		
		return (execution.isRunning()) ? RUNNING : FINISHED;
	}

}
