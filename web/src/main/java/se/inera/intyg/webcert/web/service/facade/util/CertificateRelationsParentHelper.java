package se.inera.intyg.webcert.web.service.facade.util;

import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;

public interface CertificateRelationsParentHelper {

    /**
     * Checks if the certificate has a parent relation and retrieves the parent information
     * from Intygstjanst (IT).
     *
     * @param certificateId Id of certificate
     * @return If parent relation exits a {@link WebcertCertificateRelation} is returned. If not it returns null.
     */
    WebcertCertificateRelation getParentFromITIfExists(String certificateId);
}
