package se.inera.certificate.mc2wc.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;
import se.inera.certificate.mc2wc.message.MigrationMessage;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.text.MessageFormat;

public class MigrationMessageSchemaValidatorImpl implements MigrationMessageSchemaValidator {

    private static Logger log = LoggerFactory.getLogger(MigrationMessageSchemaValidator.class);

    private static JAXBContext jaxbContext;

    private static Schema schema;

    /* (non-Javadoc)
     * @see se.inera.certificate.mc2wc.schema.MigrationMessageSchemaValidator#validateMigrationMessage(se.inera.certificate.mc2wc.message.MigrationMessage)
     */
    @Override
    public void validateMigrationMessage(MigrationMessage migrationMessage) throws SchemaValidatorException {

        CollectingErrorHandler errorHandler = performValidation(migrationMessage);

        if (errorHandler.hasValidationWarnings()) {
            log.warn("Schema validation warnings detected in the MigrationMessage for certificate {}",
                    migrationMessage.getCertificateId());
            log.warn(errorHandler.getValidationWarningsAsString());
        }

        if (errorHandler.hasValidationErrors()) {
            log.error("Schema validation errors detected in the MigrationMessage for certificate {}, throwing exception!", migrationMessage.getCertificate());
            String errMsg = MessageFormat
                    .format("Schema validation errors detected in the MigrationMessage for certificate {0}, throwing exception!",
                            migrationMessage.getCertificateId());
            throw new SchemaValidatorException(errMsg, errorHandler.getValidationErrors());
        }
    }

    CollectingErrorHandler performValidation(MigrationMessage migrationMessage) throws SchemaValidatorException {

        log.debug("Performing validation of MigrationMessage for certificate {}",
                migrationMessage.getCertificateId());

        CollectingErrorHandler errorHandler = new CollectingErrorHandler();

        Validator validator = schema.newValidator();
        validator.setErrorHandler(errorHandler);

        try {
            Source migrationMessageSource = new JAXBSource(jaxbContext, migrationMessage);
            validator.validate(migrationMessageSource);
        } catch (JAXBException | SAXException | IOException ex) {
            ex.printStackTrace();
            throw new SchemaValidatorException(ex);
        }

        return errorHandler;
    }

    @PostConstruct
    public void init() {
        initJaxbContext();
        initSchema();
    }

    private void initJaxbContext() {
        try {
            jaxbContext = JAXBContext.newInstance(MigrationMessage.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context: " + e.getMessage(), e);
        }
    }

    private void initSchema() {
        try {
            Source schemaFile = new StreamSource(new ClassPathResource("/schema/mc2wc-schema.xsd").getInputStream());
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = schemaFactory.newSchema(schemaFile);
        } catch (IOException | SAXException e) {
            throw new RuntimeException("Could not create JAXB schema: " + e.getMessage(), e);
        }
    }

}
