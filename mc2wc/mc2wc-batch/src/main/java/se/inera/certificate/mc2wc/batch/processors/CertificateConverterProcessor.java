package se.inera.certificate.mc2wc.batch.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.inera.certificate.mc2wc.converter.MigrationMessageConverter;
import se.inera.certificate.mc2wc.medcert.jpa.model.Certificate;
import se.inera.certificate.mc2wc.message.MigrationMessage;

public class CertificateConverterProcessor implements ItemProcessor<Certificate, MigrationMessage> {

    private static Logger log = LoggerFactory.getLogger(CertificateConverterProcessor.class);

    @Autowired
    private MigrationMessageConverter converter;

    @Value("${medcert.exporting.entity}")
    private String sender;

    @Override
    public MigrationMessage process(Certificate cert) throws Exception {
        
        String mcCertId = cert.getId();
        
        if (!checkIfCertCanBeMigrated(cert)) {
            log.info("Certificate {} has neither contents nor questions and will not be migrated", mcCertId);
            return null;
        }
        
        return converter.toMigrationMessage(cert, sender);
    }
    
    private boolean checkIfCertCanBeMigrated(Certificate cert) {
        return hasCertAnyContents(cert) || hasCertAnyQuestions(cert);
    }
    
    private boolean hasCertAnyContents(Certificate cert) {
        return (cert.getDocument() != null && cert.getDocument().length > 0);
    }
    
    private boolean hasCertAnyQuestions(Certificate cert) {
        return (cert.getQuestions() != null && cert.getQuestions().size() > 0);
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
}
