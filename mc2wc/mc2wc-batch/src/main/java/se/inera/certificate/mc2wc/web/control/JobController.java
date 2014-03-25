package se.inera.certificate.mc2wc.web.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Controller
public class JobController {

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

    @RequestMapping("/listJobExecutions")
    public String listJobExecutions(Model model) {

        List<JobExecution> jobExecutions = new ArrayList<JobExecution>();
        List<JobInstance> instances = jobExplorer.getJobInstances(MIGRATION_JOB, 0, Integer.MAX_VALUE);
        if (!instances.isEmpty()) {
            for (JobInstance instance : instances) {

                List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
                jobExecutions.addAll(executions);
            }
        }
        model.addAttribute("jobExecutions", jobExecutions);

        return "listJobExecutions";
    }

    @RequestMapping("/restart")
    public String restartJob(@RequestParam("executionId") long executionId) {

        JobExecution execution = jobExplorer.getJobExecution(executionId);

        if (execution.isRunning()) {
            return "redirect:/d/home";
        }

        try {
            Long newId = jobOperator.restart(executionId);

        } catch (JobInstanceAlreadyCompleteException e) {
            logger.error("Could not restart job with id: {}", executionId, e);
        } catch (NoSuchJobExecutionException e) {
            logger.error("Could not restart job with id: {}", executionId, e);
        } catch (NoSuchJobException e) {
            logger.error("Could not restart job with id: {}", executionId, e);
        } catch (JobRestartException e) {
            logger.error("Could not restart job with id: {}", executionId, e);
        } catch (JobParametersInvalidException e) {
            logger.error("Could not restart job with id: {}", executionId, e);
        }

        return "redirect:/d/checkMigration";
    }

    @RequestMapping("/startMigration")
    public String runMigrationJob(@RequestParam("dryRun") boolean dryRun, @RequestParam("sender") String sender, Model model) throws Exception {

        Set<JobExecution> executions = jobExplorer.findRunningJobExecutions(MIGRATION_JOB);

        if (executions.isEmpty()) {
            JobParametersBuilder builder = new JobParametersBuilder();
            if (dryRun) {
                builder.addString("dryRun", "true");
            }
            builder.addString("sender", sender);

            jobLauncher.run(migrationJob, builder.toJobParameters());
        }

        return "redirect:/d/checkMigration";
    }

    @RequestMapping("/checkMigration")
    public String checkMigrationJob(Model model) throws Exception {

        Set<JobExecution> executions = jobExplorer.findRunningJobExecutions(MIGRATION_JOB);

        if (executions.isEmpty()) {
            return migrationFinished(model);
        }

        JobExecution execution = executions.iterator().next();

        model.addAttribute("status", execution.getStatus());

        ExecutionContext executionContext = execution.getExecutionContext();
        for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }

        return "checkMigration";
    }

    @RequestMapping("/migrationFinished")
    public String migrationFinished(Model model) throws Exception {
        List<JobInstance> instances = jobExplorer.getJobInstances(MIGRATION_JOB, 0, Integer.MAX_VALUE);

        if (instances.isEmpty()) {
            logger.debug("No job found!");
            return "home";
        }

        JobInstance instance = instances.iterator().next();

        List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(instance);

        if (jobExecutions.isEmpty()) {
            logger.debug("No execution found!");
            return "home";
        }

        JobExecution execution = jobExecutions.iterator().next();

        model.addAttribute("status", execution.getStatus());

        ExecutionContext executionContext = execution.getExecutionContext();
        for (Map.Entry<String, Object> entry : executionContext.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }

        return "migrationSummary";
    }
}
