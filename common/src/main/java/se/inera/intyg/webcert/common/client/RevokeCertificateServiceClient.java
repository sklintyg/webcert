package se.inera.intyg.webcert.common.client;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;

/**
 * Created by eriklupander on 2015-06-03.
 */
public interface RevokeCertificateServiceClient {

    /**
     * Tells IT to perform a revoke operation based on the supplied request (in XML format).
     *
     * @param xml
     *      A {@link se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType}
     *      serialized into XML format.
     * @param logicalAddress
     * @return
     */
    RevokeMedicalCertificateResponseType revokeCertificate(String xml, String logicalAddress);

}
