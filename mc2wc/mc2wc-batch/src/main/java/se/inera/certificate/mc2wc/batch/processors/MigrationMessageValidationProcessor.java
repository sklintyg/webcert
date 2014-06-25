package se.inera.certificate.mc2wc.batch.processors;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.mc2wc.medcert.jpa.model.CreatorOrigin;
import se.inera.certificate.mc2wc.medcert.jpa.model.State;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.schema.MigrationMessageSchemaValidator;
import se.inera.certificate.mc2wc.schema.SchemaValidatorException;

/**
 * Processor to validate if the MigrationMessage contains anything at all to migrate.
 * 
 * @author nikpet
 */
public class MigrationMessageValidationProcessor implements ItemProcessor<MigrationMessage, MigrationMessage> {

    private static final Object CREATOR_APPLICATION = "APPLICATION";

    private static final List<String> UNMIGRATABLE_CERTIFICATE_STATES = Arrays.asList("CREATED","EDITED");

    public static Logger log = LoggerFactory.getLogger(MigrationMessageValidationProcessor.class);

    @Autowired
    private MigrationMessageSchemaValidator schemaValidator;

    @Override
    public MigrationMessage process(MigrationMessage message) throws Exception {

        if (message == null) {
            return null;
        }

        log.debug("Validating MigrationMessage for Certificate {}", message.getCertificateId());

        if (validateIfMigrationMessageIsEmpty(message)) {
            if (validateIfMigrationMessageIsSchemaValid(message)) {
                return message;
            }
        }

        return null;
    }

    private boolean validateIfMigrationMessageIsEmpty(MigrationMessage message) {

        if (message == null) {
            return false;
        }
        
        if (checkCertificateInvalidState(message)) {
            log.info("MigrationMessage for Certificate {} will not be migrated since it has origin {} and state {}!", 
                    new Object[]{message.getCertificateId(), message.getCertificateOrigin(), message.getCertificateState()});
            return false;
        }

        if (!hasCertificate(message) && !hasQuestions(message)) {
            log.info("MigrationMessage for Certificate {} will not be migrated since it has neither questions nor certificate contents!", message.getCertificateId());
            return false;
        }

        return true;
    }

    private boolean checkCertificateInvalidState(MigrationMessage message) {
        String certOrigin = message.getCertificateOrigin();
        String certState =  message.getCertificateState();
        return (CREATOR_APPLICATION.equals(certOrigin) && UNMIGRATABLE_CERTIFICATE_STATES.contains(certState));
    }

    private boolean validateIfMigrationMessageIsSchemaValid(MigrationMessage message) {

        if (message == null) {
            return false;
        }

        try {
            schemaValidator.validateMigrationMessage(message);
            log.debug("MigrationMessage for Certificate {} is schema valid!", message.getCertificateId());
            return true;
        } catch (SchemaValidatorException e) {
            log.error("Schema validation exception occured for MigrationMessage for certificate {}.\\n{}", message.getCertificateId(), e.getMessage());
        }

        return false;
    }

    private boolean hasCertificate(MigrationMessage message) {
        return (message.getCertificate() != null);
    }

    private boolean hasQuestions(MigrationMessage message) {
        if (message.getQuestions() != null) {
            return !(message.getQuestions().isEmpty());
        }

        return false;
    }
}
