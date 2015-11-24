package se.inera.intyg.webcert.common.client;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;

/**
 * Created by eriklupander on 2015-06-03.
 */
public interface SendCertificateServiceClient {

    /**
     * Instructs IT to send the intyg identified by the specified intygsId and personId to the specified recipient.
     */
    SendCertificateToRecipientResponseType sendCertificate(String intygsId, String personId, String recipient, String logicalAddress);
}
