package se.inera.intyg.webcert.web.service.srs;

import se.inera.intyg.infra.integration.srs.model.SrsCertificate;

public interface GetSrsCertificate {

  SrsCertificate getSrsCertificate(String certificateId);

}
