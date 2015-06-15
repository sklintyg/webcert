package se.inera.webcert.certificatesender.services.mock;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.webcert.client.RevokeCertificateServiceClient;

/**
 * Created by eriklupander on 2015-06-03.
 */
public class MockRevokeCertificateServiceClientImpl implements RevokeCertificateServiceClient {
    @Override
    public RevokeMedicalCertificateResponseType revokeCertificate(String s, String s1) {
        return null;
    }
}
