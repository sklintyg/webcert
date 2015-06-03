package se.inera.webcert.client;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;

/**
 * Created by eriklupander on 2015-06-03.
 */
public interface RevokeCertificateServiceClient {

    RevokeMedicalCertificateResponseType revokeCertificate(String xml, String logicalAddress);

}
