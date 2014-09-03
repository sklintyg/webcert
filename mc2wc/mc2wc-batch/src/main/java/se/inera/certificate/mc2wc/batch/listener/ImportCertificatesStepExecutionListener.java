package se.inera.certificate.mc2wc.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.item.ExecutionContext;

import se.inera.certificate.mc2wc.ApplicationConsoleLogger;
import se.inera.certificate.mc2wc.batch.ImportJobConstants;

public class ImportCertificatesStepExecutionListener extends StepExecutionListenerSupport {

    private static Logger log = LoggerFactory.getLogger(ApplicationConsoleLogger.NAME);
    
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("Total nbr of migration messages in export: ");
        sb.append(stepExecution.getReadCount());
        log.info(sb.toString());
        
        sb = new StringBuilder();
        
        sb.append("Total nbr of fragasvar inserted: ");
        sb.append(executionContext.get(ImportJobConstants.FRAGA_SVAR_COUNT));
        log.info(sb.toString());
        
        sb = new StringBuilder();
        
        sb.append("Total nbr of legacy certs inserted: ");
        sb.append(executionContext.get(ImportJobConstants.LEGACY_CERTIFICATE_COUNT));
        log.info(sb.toString());
        
        return super.afterStep(stepExecution);
    }
       
}
