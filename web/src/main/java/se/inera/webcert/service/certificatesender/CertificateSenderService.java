package se.inera.webcert.service.certificatesender;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;

/**
 * Created by eriklupander on 2015-05-20.
 */
public interface CertificateSenderService {

    void storeCertificate(String intygsId, String intygsTyp, String jsonBody) throws CertificateSenderException;
    void sendCertificate(String intygsId, String personId, String recipientId) throws CertificateSenderException;
    void revokeCertificate(String intygsId, String xmlBody) throws CertificateSenderException;
}
