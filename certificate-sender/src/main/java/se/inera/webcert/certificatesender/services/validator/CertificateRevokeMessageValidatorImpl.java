package se.inera.webcert.certificatesender.services.validator;

import org.apache.camel.Message;
import org.springframework.stereotype.Component;

import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.common.Constants;

/**
 * Created by eriklupander on 2015-05-22.
 */
@Component(value = "certificateRevokeMessageValidator")
public class CertificateRevokeMessageValidatorImpl implements CertificateMessageValidator {

    @Override
    public void validate(Message message) throws PermanentException {
        String intygsId = (String) message.getHeader(Constants.INTYGS_ID);
        String logicalAddress = (String) message.getHeader(Constants.LOGICAL_ADDRESS);

        validateParameters(intygsId, logicalAddress);
        validateParameters(intygsId, logicalAddress);
        validateBodyExists(message.getBody());
    }

    private void validateBodyExists(Object body) throws PermanentException {

        if (body == null) {
            throw new PermanentException("Body of " + Constants.REVOKE_MESSAGE + " must not be null");
        }
        if (body instanceof String && nullOrEmpty( (String) body)) {
            throw new PermanentException("Body of " + Constants.REVOKE_MESSAGE + " must not be empty");
        }
    }

    private void validateParameters(String intygsTyp, String logicalAddress) throws PermanentException {
        if (nullOrEmpty(intygsTyp)) {
            throw new PermanentException("Required message header '" + Constants.INTYGS_ID + "' missing or empty.");
        }

        if (nullOrEmpty(logicalAddress)) {
            throw new PermanentException("Required message header '" + Constants.LOGICAL_ADDRESS + "' missing or empty.");
        }
    }

    // TODO Once we move to Java 8, put this into the interface as a default method.
    private boolean nullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
}
