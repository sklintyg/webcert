package se.inera.certificate.mc2wc.batch.writer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import se.inera.certificate.mc2wc.jpa.MigratedCertificate;
import se.inera.certificate.mc2wc.jpa.webcert.WebcertDAO;

public class WebcertRepositoryJpaDeleter extends WebcertDAO implements ItemWriter<MigratedCertificate> {

    private static Logger log = LoggerFactory.getLogger(WebcertRepositoryJpaDeleter.class);
    
    @Override
    public void write(List<? extends MigratedCertificate> migratedCertificates) throws Exception {
                
        for (MigratedCertificate migratedCertificate : migratedCertificates) {
            String certificateId = migratedCertificate.getCertificateId();
            
            if (migratedCertificate.getNbrOfQuestions() > 0) {
                log.info("Removing all FragaSvar for certificate {}", certificateId);
                removeFragaSvar(certificateId);
            }
            
            if (migratedCertificate.isHasLegacyCertificate()) {
                log.info("Removing legacy Medcert certificate {}", certificateId);
                removeMigreratMedcertIntyg(certificateId);
            }
            
        }
        
    }

}
