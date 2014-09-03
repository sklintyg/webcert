package se.inera.certificate.mc2wc.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.mc2wc.ApplicationConsoleLogger;
import se.inera.certificate.mc2wc.batch.ExportJobConstants;
import se.inera.certificate.mc2wc.jpa.Mc2wcDAO;

public class ExportCertificatesStepExecutionListener extends StepExecutionListenerSupport {

    private static Logger log = LoggerFactory.getLogger(ApplicationConsoleLogger.NAME);
    
    @Autowired
    private Mc2wcDAO mc2wcDao;
     
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        
        StringBuilder sb = new StringBuilder();
                
        sb.append("Total nbr of processed certs: ");
        sb.append(stepExecution.getReadCount());
        log.info(sb.toString());
        
        sb = new StringBuilder();
        
        sb.append("Certs that has an illegal state: ");
        sb.append(stepExecution.getExecutionContext().get(ExportJobConstants.PRE_CONVERT_FAIL_COUNT));
        log.info(sb.toString());
        
        sb = new StringBuilder();
        
        sb.append("Certs that has no contents after conversion: ");
        sb.append(stepExecution.getExecutionContext().get(ExportJobConstants.POST_CONVERT_FAIL_COUNT));
        log.info(sb.toString());
        
        sb = new StringBuilder();
        
        sb.append("Total nbr of migrated certs: ");
        sb.append(stepExecution.getWriteCount());
        log.info(sb.toString());
        
        sb = new StringBuilder();
        
        sb.append("Migrated certs with legacy contents: ");
        Long nbrOfMigratedLegacyCertificates = mc2wcDao.countNbrOfMigratedLegacyCertificates();
        sb.append(nbrOfMigratedLegacyCertificates);
        log.info(sb.toString());
        
        sb = new StringBuilder();
        
        sb.append("Migrated questions: ");
        Long nbrOfMigratedQuestions = mc2wcDao.sumNbrOfMigratedQuestions();
        sb.append(nbrOfMigratedQuestions);
        log.info(sb.toString());
        
        sb = new StringBuilder();
        
        sb.append("Migrated certs with contents without questions: ");
        Long nbrWithContentsWithoutQuestions = mc2wcDao.countMigratedCertificatesWithContentsWithoutQuestions();
        sb.append(nbrWithContentsWithoutQuestions);
        log.info(sb.toString());
        
        sb = new StringBuilder();
                
        sb.append("Migrated certs with contents and questions: ");
        Long nbrWithContentsWithQuestions = mc2wcDao.countMigratedCertificatesWithContentsWithQuestions();
        sb.append(nbrWithContentsWithQuestions);
        log.info(sb.toString());
        
        sb = new StringBuilder();
                
        sb.append("Migrated certs with questions without content: ");
        Long nbrWithoutContentsWithQuestions = mc2wcDao.countMigratedCertificatesWithoutContentsWithQuestions();
        sb.append(nbrWithoutContentsWithQuestions);
        log.info(sb.toString());
        
        return super.afterStep(stepExecution);
    }
    
}
