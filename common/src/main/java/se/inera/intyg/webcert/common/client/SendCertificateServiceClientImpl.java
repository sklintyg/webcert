package se.inera.intyg.webcert.common.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;

/**
 * Exposes the SendCertificateToRecipient SOAP service.
 *
 * Created by eriklupander on 2015-06-03.
 */
@Component
public class SendCertificateServiceClientImpl implements SendCertificateServiceClient {

    @Autowired
    private SendCertificateToRecipientResponderInterface sendService;

    @Override
    public SendCertificateToRecipientResponseType sendCertificate(String intygsId, String personId, String recipient, String logicalAddress) {

        validateArgument(intygsId, "Cannot send certificate, argument 'intygsId' is null or empty.");
        validateArgument(personId, "Cannot send certificate, argument 'personId' is null or empty.");
        validateArgument(recipient, "Cannot send certificate, argument 'recipient' is null or empty.");
        validateArgument(logicalAddress, "Cannot send certificate, argument 'logicalAddress' is null or empty.");

        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setUtlatandeId(intygsId);
        request.setPersonId(personId);
        request.setMottagareId(recipient);

        SendCertificateToRecipientResponseType response = sendService.sendCertificateToRecipient(logicalAddress, request);

        return response;
    }

    private void validateArgument(String arg, String msg) {
        if (arg == null || arg.trim().length() == 0) {
            throw new IllegalArgumentException(msg);
        }
    }
}
