package se.inera.webcert.certificatesender.services.mock;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.webcert.client.SendCertificateServiceClient;

/**
 * Created by eriklupander on 2015-06-03.
 */
public class MockSendCertificateServiceClientImpl implements SendCertificateServiceClient {
    @Override
    public SendCertificateToRecipientResponseType sendCertificate(String s, String s1, String s2, String s3) {
        return null;
    }
}
