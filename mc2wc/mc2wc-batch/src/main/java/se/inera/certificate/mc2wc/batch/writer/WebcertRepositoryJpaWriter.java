package se.inera.certificate.mc2wc.batch.writer;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.mc2wc.batch.ImportJobConstants;
import se.inera.certificate.mc2wc.converter.FragaSvarConverter;
import se.inera.certificate.mc2wc.converter.MedcertIntygConverter;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.QuestionType;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.legacy.model.MigreratMedcertIntyg;

public class WebcertRepositoryJpaWriter implements ItemWriter<MigrationMessage> {
    
    private static Logger log = LoggerFactory.getLogger(WebcertRepositoryJpaWriter.class);
    
    @PersistenceContext(unitName = "jpa.migration.webcert")
    private EntityManager em;

    @Autowired
    private MedcertIntygConverter medcertIntygConverter;

    @Autowired
    private FragaSvarConverter fragaSvarConverter;
    
    private StepExecution stepExecution;

    @Override
    public void write(List<? extends MigrationMessage> migrationMessages) throws Exception {
        
        log.debug("Writing {} MigrationMessages to repository", migrationMessages.size());
        
        for (MigrationMessage migrationMessage : migrationMessages) {
            processMigrationMessage(migrationMessage);
        }

    }

    private void processMigrationMessage(MigrationMessage message) {
        
        log.debug("Processing MigrationMessage for certificate '{}'", message.getCertificateId());
        
        List<QuestionType> questions = message.getQuestions();
        
        log.debug("Certificate '{}' has {} questions", message.getCertificateId(), questions.size());
        
        for (QuestionType q : questions) {
            FragaSvar fs = fragaSvarConverter.toFragaSvar(q);
            em.persist(fs);
            addFragaSvarCount();
        }

        if (message.getCertificate() != null) {
            log.debug("Certificate '{}' has contents to be saved", message.getCertificateId());
            MigreratMedcertIntyg mcCert = medcertIntygConverter.toMigreratMedcertIntyg(message.getCertificate());
            em.persist(mcCert);
            addLegacyCertificateCount();
        }
    }
    
    private void addFragaSvarCount() {
        addCountToExecutionContext(1, ImportJobConstants.FRAGA_SVAR_COUNT);
    }
    
    private void addLegacyCertificateCount() {
        addCountToExecutionContext(1, ImportJobConstants.LEGACY_CERTIFICATE_COUNT);
    }
    
    private void addCountToExecutionContext(long count, String key) {
        ExecutionContext executionContext = getStepExecution().getExecutionContext();
        Long certCount = (Long) executionContext.get(key);
        
        if (certCount == null) {
            certCount = new Long(0);
        }
        
        executionContext.put(key, certCount + count);
    }

    public StepExecution getStepExecution() {
        return stepExecution;
    }

    public void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
    
}
