package se.inera.certificate.mc2wc.batch.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.schema.MigrationMessageSchemaValidator;
import se.inera.certificate.mc2wc.schema.SchemaValidatorException;

/**
 * Processor to validate if the MigrationMessage contains anything at all to migrate.
 *
 * @author nikpet
 */
public class MigrationMessageValidationProcessor implements ItemProcessor<MigrationMessage, MigrationMessage> {

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

        if (hasCertificate(message) || hasQuestions(message)) {
            log.debug("MigrationMessage for Certificate {} is not empty!", message.getCertificateId());
            return true;
        }

        log.info("MigrationMessage for Certificate {} has neither questions nor certificate contents!", message.getCertificateId());

        return false;
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
