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

public class MigratedCertificateConverterProcessor implements
        ItemProcessor<MigrationMessage, MigratedCertificate> {

    private static Logger log = LoggerFactory.getLogger(MigratedCertificateConverterProcessor.class);

    private JAXBContext context;

    private Marshaller marshaller;

    @Override
    public MigratedCertificate process(MigrationMessage migrationMessage) throws Exception {

        log.debug("Preparing MigratedCertificate entity for certficate '{}'", migrationMessage.getCertificateId());

        MigratedCertificate migratedCertificate = new MigratedCertificate();
        String certificateId = migrationMessage.getCertificateId();

        byte[] certificateDocument = convertMigrationMessageToXML(migrationMessage);
        log.debug("Adding payload of {} bytes", certificateDocument.length);

        migratedCertificate.setCertificateId(certificateId);
        migratedCertificate.setDocument(certificateDocument);

        return migratedCertificate;
    }

    private byte[] convertMigrationMessageToXML(MigrationMessage migrationMessage) throws JAXBException {
        ByteArrayOutputStream xmlBaos = new ByteArrayOutputStream();
        marshaller.marshal(migrationMessage, xmlBaos);
        return xmlBaos.toByteArray();
    }

    @PostConstruct
    private void initJaxbContext() {
        try {
            this.context = JAXBContext.newInstance(MigrationMessage.class);
            this.marshaller = context.createMarshaller();
            this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context: " + e.getMessage(), e);
        }
    }
}
