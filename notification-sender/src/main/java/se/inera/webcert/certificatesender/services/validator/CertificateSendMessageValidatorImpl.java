package se.inera.webcert.certificatesender.services.validator;

import org.apache.camel.Message;
import org.springframework.stereotype.Component;

import se.inera.webcert.certificatesender.exception.PermanentException;
import se.inera.webcert.common.Constants;

/**
 * Created by eriklupander on 2015-05-22.
 */
@Component(value = "certificateSendMessageValidator")
public class CertificateSendMessageValidatorImpl implements CertificateMessageValidator {

    @Override
    public void validate(Message message) throws PermanentException {
        String intygsId = (String) message.getHeader(Constants.INTYGS_ID);
        String personId = (String) message.getHeader(Constants.PERSON_ID);
        String recipient = (String) message.getHeader(Constants.RECIPIENT);
        String logicalAddress = (String) message.getHeader(Constants.LOGICAL_ADDRESS);

        validateParameters(intygsId, personId, recipient, logicalAddress);
    }


    private void validateParameters(String intygsId, String personId, String recipient, String logicalAddress) throws PermanentException {
        if (nullOrEmpty(intygsId)) {
            throw new PermanentException("Required message header '" + Constants.INTYGS_ID + "' missing or empty.");
        }
        if (nullOrEmpty(personId)) {
            throw new PermanentException("Required message header '" + Constants.PERSON_ID + "' missing or empty.");
        }
        if (nullOrEmpty(recipient) ) {
            throw new PermanentException("Required message header '" + Constants.RECIPIENT + "' missing or empty.");
        }
        if (nullOrEmpty(logicalAddress)  ) {
            throw new PermanentException("Required message header '" + Constants.LOGICAL_ADDRESS + "' missing or empty.");
        }
    }

    // TODO Once we move to Java 8, put this into the interface as a default method.
    private boolean nullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

}
