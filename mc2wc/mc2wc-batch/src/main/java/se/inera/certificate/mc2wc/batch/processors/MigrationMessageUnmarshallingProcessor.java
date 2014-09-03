package se.inera.certificate.mc2wc.batch.processors;

import java.io.ByteArrayInputStream;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import se.inera.certificate.mc2wc.jpa.MigratedCertificate;
import se.inera.certificate.mc2wc.message.MigrationMessage;

public class MigrationMessageUnmarshallingProcessor implements
        ItemProcessor<MigratedCertificate, MigrationMessage> {

    private static Logger log = LoggerFactory.getLogger(MigrationMessageUnmarshallingProcessor.class);

    private JAXBContext context;

    @Override
    public MigrationMessage process(MigratedCertificate migratedCertificate)
            throws Exception {

        log.debug("Unmarshalling contents of migratedCertificate with id '{}' for certificate '{}'", migratedCertificate.getId(),
                migratedCertificate.getCertificateId());

        byte[] mcDoc = migratedCertificate.getDocument();
        MigrationMessage migrationMessage = unmarshall(mcDoc);
        
        log.debug("MigrationMessage for certificate '{}'", migrationMessage.getCertificateId());
        
        return migrationMessage;
    }

    private MigrationMessage unmarshall(byte[] mcDoc) throws JAXBException {
        ByteArrayInputStream bis = new ByteArrayInputStream(mcDoc);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (MigrationMessage) unmarshaller.unmarshal(bis);
    }

    @PostConstruct
    private void initJaxbContext() {
        try {
            this.context = JAXBContext.newInstance(MigrationMessage.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context: "
                    + e.getMessage(), e);
        }
    }
}
