package se.inera.intyg.webcert.web.service.facade;

import se.inera.intyg.common.support.modules.support.facade.dto.CertificateDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.ValidationErrorDTO;

public interface CertificateFacadeService {

    CertificateDTO getCertificate(String certificateId);

    long saveCertificate(CertificateDTO certificate);

    ValidationErrorDTO[] validate(CertificateDTO certificate);

    CertificateDTO signCertificate(CertificateDTO certificate);

    void deleteCertificate(String certificateId, long version);

    CertificateDTO revokeCertificate(String certificateId, String reason, String message);

    String replaceCertificate(String certificateId, String certificateType, String patientId);

    String copyCertificate(String certificateId, String certificateType, String patientId);

    CertificateEventDTO[] getCertificateEvents(String certificateId);

    CertificateDTO forwardCertificate(String certificateId, long version, boolean forwarded);
}
