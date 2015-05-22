package se.inera.webcert.certificatesender.services.validator;

import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.common.Constants;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateStoreMessageValidatorTest {

    @Mock
    Message message;

    CertificateStoreMessageValidatorImpl validator = new CertificateStoreMessageValidatorImpl();

    @Before
    public void setup() {
        when(message.getHeader(Constants.LOGICAL_ADDRESS)).thenReturn("logisk-adress");
        when(message.getHeader(Constants.INTYGS_TYP)).thenReturn("ts-bas");
        when(message.getBody()).thenReturn("A body of json");
    }

    @Test
    public void testValidationOk() throws PermanentException {


        validator.validate(message);
    }

    @Test(expected = PermanentException.class)
    public void testIntygsTypIsMissing() throws PermanentException {
        when(message.getHeader(Constants.INTYGS_TYP)).thenReturn(null);
        try {
            validator.validate(message);
        } catch (PermanentException e) {
            assertTrue(e.getMessage().contains(Constants.INTYGS_TYP));
            throw e;
        }
    }

    @Test(expected = PermanentException.class)
    public void testLogicalAddressIsMissing() throws PermanentException {
        when(message.getHeader(Constants.LOGICAL_ADDRESS)).thenReturn(null);
        try {
            validator.validate(message);
        } catch (PermanentException e) {
            assertTrue(e.getMessage().contains(Constants.LOGICAL_ADDRESS));
            throw e;
        }
    }

    @Test(expected = PermanentException.class)
    public void testBodyIsMissing() throws PermanentException {
        when(message.getBody()).thenReturn(null);
        try {
            validator.validate(message);
        } catch (PermanentException e) {
            assertTrue(e.getMessage().contains(Constants.STORE_MESSAGE));
            throw e;
        }
    }

    @Test(expected = PermanentException.class)
    public void testBodyIsEmpty() throws PermanentException {
        when(message.getBody()).thenReturn("");
        try {
            validator.validate(message);
        } catch (PermanentException e) {
            assertTrue(e.getMessage().contains(Constants.STORE_MESSAGE));
            throw e;
        }
    }

    @Test(expected = PermanentException.class)
    public void testBodyIsEmptyButWithWhitespace() throws PermanentException {
        when(message.getBody()).thenReturn(" ");
        try {
            validator.validate(message);
        } catch (PermanentException e) {
            assertTrue(e.getMessage().contains(Constants.STORE_MESSAGE));
            throw e;
        }
    }
}
