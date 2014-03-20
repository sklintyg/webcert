package se.inera.certificate.mc2wc.web.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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

//    @Autowired
//    private CertificateMigrationListener migrationListener;

    @Autowired
    @Qualifier(MIGRATION_JOB)
    private Job migrationJob;

    @Autowired
    private JobExplorer jobExplorer;

    @RequestMapping("/startMigration")
    public String runMigrationJob(Model model) throws Exception {

        Set<JobExecution> executions = jobExplorer.findRunningJobExecutions(MIGRATION_JOB);

        if (executions.isEmpty()) {
            JobParameters params = new JobParametersBuilder().addLong("millis", System.currentTimeMillis(), true).toJobParameters();

            jobLauncher.run(migrationJob, params);
        }

        return checkMigrationJob(model);
    }

    @RequestMapping("/checkMigration")
    public String checkMigrationJob(Model model) throws Exception {

        Set<JobExecution> executions = jobExplorer.findRunningJobExecutions(MIGRATION_JOB);

        if (executions.isEmpty()) {
            return migrationFinished(model);
        }

        JobExecution execution = executions.iterator().next();

        model.addAttribute("status", execution.getStatus());

//        model.addAttribute("readCount", migrationListener.getReadCount());
//        model.addAttribute("readError", migrationListener.getReadError());
//        model.addAttribute("skipCount", migrationListener.getSkipCount());
//        model.addAttribute("writeCount", migrationListener.getWriteCount());
//        model.addAttribute("writeError", migrationListener.getWriteError());


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
