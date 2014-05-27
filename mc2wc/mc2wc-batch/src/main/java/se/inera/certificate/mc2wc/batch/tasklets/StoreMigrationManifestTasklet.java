package se.inera.certificate.mc2wc.batch.tasklets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.inera.certificate.mc2wc.ApplicationConsoleLogger;
import se.inera.certificate.mc2wc.jpa.Mc2wcDAO;
import se.inera.certificate.mc2wc.jpa.MigrationManifest;
import se.inera.certificate.mc2wc.medcert.jpa.MedcertDAO;

public class StoreMigrationManifestTasklet implements Tasklet {

	private static Logger log = LoggerFactory.getLogger(ApplicationConsoleLogger.NAME);
	
	@Autowired
	private MedcertDAO medcertDao;
	
	@Autowired
	private Mc2wcDAO mc2wcDao;

	@Value("${medcert.exporting.entity}")
    private String exporter;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		MigrationManifest manifest = new MigrationManifest(exporter);
		
		Long certificatesWithContents = medcertDao.countCertificatesWithContents();
		manifest.setCertificatesWithContents(certificatesWithContents);
		
		Long emptyCertificates = medcertDao.countEmptyCertificates();
		manifest.setCertificatesWithoutContents(emptyCertificates);
		
		Long questions = medcertDao.countQuestions();
		manifest.setQuestions(questions);
		
		Long answers = medcertDao.countAnswers();
		manifest.setAnswers(answers);
		
		mc2wcDao.insertMigrationManifest(manifest);
		
		log.info("Storing manifest: {}", manifest);
		
		return RepeatStatus.FINISHED;
	}

}
