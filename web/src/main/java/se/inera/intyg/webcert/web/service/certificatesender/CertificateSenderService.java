package se.inera.intyg.webcert.web.service.certificatesender;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

/**
 * Created by eriklupander on 2015-05-20.
 */
public interface CertificateSenderService {

    void storeCertificate(String intygsId, String intygsTyp, String jsonBody) throws CertificateSenderException;
    void sendCertificate(String intygsId, Personnummer personId, String recipientId) throws CertificateSenderException;
    void revokeCertificate(String intygsId, String xmlBody) throws CertificateSenderException;
}
