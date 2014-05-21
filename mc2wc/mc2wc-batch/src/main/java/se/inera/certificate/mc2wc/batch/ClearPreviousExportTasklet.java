package se.inera.certificate.mc2wc.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.inera.certificate.mc2wc.jpa.Mc2wcDAO;

public class ClearPreviousExportTasklet implements Tasklet {

	private static Logger log = LoggerFactory.getLogger(ClearPreviousExportTasklet.class);
	
	@Autowired
	private Mc2wcDAO mc2wcDAO;
	
	@Value("${medcert.exporting.entity}")
	private String exporter;
	
	@Override
	public RepeatStatus execute(StepContribution stepContrib, ChunkContext chunkCtx)
			throws Exception {
		
		log.info("Clearing previous export from '{}' from target db", exporter);
		
		mc2wcDAO.clearMigratedCertificates();
		
		
		return RepeatStatus.FINISHED;
	}

}
