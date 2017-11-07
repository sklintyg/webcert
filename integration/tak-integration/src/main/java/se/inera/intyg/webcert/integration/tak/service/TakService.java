package se.inera.intyg.webcert.integration.tak.service;

import se.inera.intyg.infra.integration.hsa.exception.HsaServiceCallException;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.integration.tak.model.TakResult;

public interface TakService {
    TakResult verifyTakningForCareUnit(String careUnitId, String intygsTyp, String schemaVersion, IntygUser user);
}
