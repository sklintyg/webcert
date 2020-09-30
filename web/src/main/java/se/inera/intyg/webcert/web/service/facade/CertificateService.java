package se.inera.intyg.webcert.web.service.facade;

import se.inera.intyg.common.support.modules.support.facade.dto.CertificateDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;

public interface CertificateService {

    CertificateDTO getCertificate(String certificateId);

    long saveCertificate(CertificateDTO certificate);

    ValidationErrorDTO[] validate(CertificateDTO certificate);

    CertificateDTO signCertificate(CertificateDTO certificate);

    void deleteCertificate(String certificateId, long version);

    void revokeCertificate(String certificateId, String reason, String message);
}
