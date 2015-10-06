package se.inera.webcert.service.certificatesender;

/**
 * Created by eriklupander on 2015-05-20.
 */
public interface CertificateSenderService {

    void storeCertificate(String intygsId, String intygsTyp, String jsonBody) throws CertificateSenderException;
    void sendCertificate(String intygsId, String personId, String recipientId) throws CertificateSenderException;
    void revokeCertificate(String intygsId, String xmlBody) throws CertificateSenderException;
}
