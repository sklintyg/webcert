package se.inera.webcert.certificatesender.services.validator;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.common.Constants;

/**
 * Created by eriklupander on 2015-05-22.
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateRevokeMessageValidatorTest {

    @Mock
    Message message;

    CertificateRevokeMessageValidatorImpl validator = new CertificateRevokeMessageValidatorImpl();

    @Before
    public void setup() {
        when(message.getHeader(Constants.INTYGS_ID)).thenReturn("intygs-id");
        when(message.getHeader(Constants.LOGICAL_ADDRESS)).thenReturn("logisk-adress");
        when(message.getBody()).thenReturn("A body");
    }

    @Test
    public void testValidationOk() throws PermanentException {
        validator.validate(message);
    }

    @Test(expected = PermanentException.class)
    public void testIntygsIdIsMissing() throws PermanentException {
        when(message.getHeader(Constants.INTYGS_ID)).thenReturn(null);
        try {
            validator.validate(message);
        } catch (PermanentException e) {
            assertTrue(e.getMessage().contains(Constants.INTYGS_ID));
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
            assertTrue(e.getMessage().contains(Constants.REVOKE_MESSAGE));
            throw e;
        }
    }

    @Test(expected = PermanentException.class)
    public void testBodyIsEmpty() throws PermanentException {
        when(message.getBody()).thenReturn("");
        try {
            validator.validate(message);
        } catch (PermanentException e) {
            assertTrue(e.getMessage().contains(Constants.REVOKE_MESSAGE));
            throw e;
        }
    }

    @Test(expected = PermanentException.class)
    public void testBodyIsEmptyButWithWhitespace() throws PermanentException {
        when(message.getBody()).thenReturn(" ");
        try {
            validator.validate(message);
        } catch (PermanentException e) {
            assertTrue(e.getMessage().contains(Constants.REVOKE_MESSAGE));
            throw e;
        }
    }
}
