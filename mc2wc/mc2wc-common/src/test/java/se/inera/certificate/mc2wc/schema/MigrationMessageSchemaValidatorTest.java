package se.inera.certificate.mc2wc.schema;

import org.junit.Before;
import org.junit.Test;
import se.inera.certificate.mc2wc.message.MigrationMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

import static org.junit.Assert.*;

public class MigrationMessageSchemaValidatorTest {

    private MigrationMessageSchemaValidatorImpl validatorImpl;

    @Before
    public void setUp() {
        validatorImpl = new MigrationMessageSchemaValidatorImpl();
        validatorImpl.init();
    }

    @Test
    public void testValidatePositive() throws Exception {
        MigrationMessage migrationMessage = readMigrationMessage("/mc2wc-test-1.xml");
        CollectingErrorHandler errorHandler = validatorImpl.performValidation(migrationMessage);
        assertNotNull(errorHandler);
        assertFalse(errorHandler.hasValidationErrors());
        assertFalse(errorHandler.hasValidationWarnings());
    }

    @Test
    public void testValidateNegative() throws Exception {
        MigrationMessage migrationMessage = readMigrationMessage("/mc2wc-test-1.xml");
        migrationMessage.setCertificateId(null);
        migrationMessage.getCertificate().setMigratedFrom(null);

        CollectingErrorHandler errorHandler = validatorImpl.performValidation(migrationMessage);

        assertNotNull(errorHandler);
        assertTrue(errorHandler.hasValidationErrors());
        assertFalse(errorHandler.hasValidationWarnings());
    }

    public MigrationMessage readMigrationMessage(String filePath) throws Exception {

        JAXBContext ctx = JAXBContext.newInstance(MigrationMessage.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        InputStream is = this.getClass().getResourceAsStream(filePath);
        return (MigrationMessage) unmarshaller.unmarshal(is);
    }
}
