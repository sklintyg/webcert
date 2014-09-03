package se.inera.certificate.mc2wc.batch.processors;

import java.io.ByteArrayOutputStream;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import se.inera.certificate.mc2wc.jpa.MigratedCertificate;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.QuestionType;

public class MigratedCertificateConverterProcessor implements
        ItemProcessor<MigrationMessage, MigratedCertificate> {

    private static Logger log = LoggerFactory.getLogger(MigratedCertificateConverterProcessor.class);

    private JAXBContext context;

    @Override
    public MigratedCertificate process(MigrationMessage migrationMessage) throws Exception {

        String certificateId = migrationMessage.getCertificateId();
        
        log.debug("Preparing MigratedCertificate entity for certficate '{}'", certificateId);

        MigratedCertificate migratedCertificate = new MigratedCertificate();
        migratedCertificate.setCertificateId(certificateId);
            
        if (migrationMessage.getCertificate() != null) {
            migratedCertificate.setHasLegacyCertificate(true);
            log.debug("MigratedCertficate contains a legacy certificate");
         }
        
        byte[] certificateDocument = convertMigrationMessageToXML(migrationMessage);
        migratedCertificate.setDocument(certificateDocument);
        
        log.debug("Adding certificate document of {} bytes", certificateDocument.length);
        
        addCountOfNbrOfQuestions(migratedCertificate, migrationMessage);
        
        return migratedCertificate;
    }
    
    private void addCountOfNbrOfQuestions(MigratedCertificate migrCert, MigrationMessage migrationMessage) {
        
        int nbrOfQuestions = 0;
        
        int nbrOfAnsweredQuestions  = 0;
        
        if (migrationMessage.getQuestions() == null) {
            return;
        }
        
        for (QuestionType qt : migrationMessage.getQuestions()) {
            nbrOfQuestions++;
            nbrOfAnsweredQuestions += (qt.getAnswer() != null) ? 1 : 0;
        }
        
        migrCert.setNbrOfQuestions(nbrOfQuestions);
        migrCert.setNbrOfAnsweredQuestions(nbrOfAnsweredQuestions);
        
        log.debug("Migrated certificate has {} questions, {} with answers", nbrOfQuestions, nbrOfAnsweredQuestions);
    }
        
    private byte[] convertMigrationMessageToXML(MigrationMessage migrationMessage) throws JAXBException {
        Marshaller marshaller = this.context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        ByteArrayOutputStream xmlBaos = new ByteArrayOutputStream();
        marshaller.marshal(migrationMessage, xmlBaos);
        return xmlBaos.toByteArray();
    }

    @PostConstruct
    private void initJaxbContext() {
        try {
            this.context = JAXBContext.newInstance(MigrationMessage.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context: " + e.getMessage(), e);
        }
    }
}
