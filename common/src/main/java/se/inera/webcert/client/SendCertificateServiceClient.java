package se.inera.webcert.client;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;

/**
 * Created by eriklupander on 2015-06-03.
 */
public interface SendCertificateServiceClient {

    SendCertificateToRecipientResponseType sendCertificate(String intygsId, String personId, String recipient, String logicalAddress);
}
