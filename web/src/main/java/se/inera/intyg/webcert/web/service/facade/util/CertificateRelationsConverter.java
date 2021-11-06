package se.inera.intyg.webcert.web.service.facade.util;

import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;

public interface CertificateRelationsConverter {

    CertificateRelations convert(String certificateId);
}
