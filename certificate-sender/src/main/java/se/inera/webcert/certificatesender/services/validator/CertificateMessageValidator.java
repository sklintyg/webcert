package se.inera.webcert.certificatesender.services.validator;

import org.apache.camel.Message;
import se.inera.webcert.certificatesender.exception.PermanentException;

/**
 * Created by eriklupander on 2015-05-22.
 */
public interface CertificateMessageValidator {
    void validate(Message message) throws PermanentException;
}
