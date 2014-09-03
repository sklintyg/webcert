package se.inera.certificate.mc2wc.batch.processors;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.inera.certificate.mc2wc.batch.ExportJobConstants;
import se.inera.certificate.mc2wc.converter.MigrationMessageConverter;
import se.inera.certificate.mc2wc.medcert.jpa.model.Certificate;
import se.inera.certificate.mc2wc.medcert.jpa.model.CreatorOrigin;
import se.inera.certificate.mc2wc.medcert.jpa.model.State;
import se.inera.certificate.mc2wc.message.MigrationMessage;

public class CertificateConverterProcessor implements ItemProcessor<Certificate, MigrationMessage> {
    
    private static final CreatorOrigin CREATOR_APPLICATION = CreatorOrigin.APPLICATION;

    private static final List<State> UNMIGRATABLE_CERTIFICATE_STATES = Arrays.asList(State.CREATED, State.EDITED);
    
    private static Logger log = LoggerFactory.getLogger(CertificateConverterProcessor.class);
    
    @Autowired
    private MigrationMessageConverter converter;

    @Value("${medcert.exporting.entity}")
    private String sender;

    private StepExecution stepExecution;
    
    @Override
    public MigrationMessage process(Certificate cert) throws Exception {
        
        log.info("Processing certificate {}", cert.getId());
        
        if (checkCertificateInvalidState(cert)) {
            log.info("Pre-conversion check failed, certificate {} will not be migrated since it has origin {} and state {}!", 
                    new Object[]{cert.getId(), cert.getOrigin(), cert.getState()});
            addPreConversionCheckFail();
            return null;
        }
        
        MigrationMessage migrationMessage = converter.toMigrationMessage(cert, sender);
        
        if (hasNoCertificate(migrationMessage) && hasNoQuestions(migrationMessage)) {
            log.info("Post-conversion check failed, certificate {} will not be migrated since it has neither migratable questions nor certificate contents!", migrationMessage.getCertificateId());
            addPostConversionCheckFail();
            return null;
        }
        
        return migrationMessage;
    }
    
    private boolean checkCertificateInvalidState(Certificate cert) {
        CreatorOrigin certOrigin = cert.getOrigin();
        State certState =  cert.getState();
        return (CREATOR_APPLICATION.equals(certOrigin) && UNMIGRATABLE_CERTIFICATE_STATES.contains(certState));
    }
    
    private boolean hasNoCertificate(MigrationMessage message) {
        return (message.getCertificate() == null);
    }

    private boolean hasNoQuestions(MigrationMessage message) {
        return (message.getQuestions().isEmpty()); 
    }
    
    private void addPreConversionCheckFail() {
        addCountToExecutionContext(1, ExportJobConstants.PRE_CONVERT_FAIL_COUNT);
    }
    
    private void addPostConversionCheckFail() {
        addCountToExecutionContext(1, ExportJobConstants.POST_CONVERT_FAIL_COUNT);
    }
    
    private void addCountToExecutionContext(long count, String key) {
        ExecutionContext executionContext = getStepExecution().getExecutionContext();
        Long certCount = (Long) executionContext.get(key);
        
        if (certCount == null) {
            certCount = new Long(0);
        }
        
        executionContext.put(key, certCount + count);
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }

    public StepExecution getStepExecution() {
        return stepExecution;
    }

    public void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}
