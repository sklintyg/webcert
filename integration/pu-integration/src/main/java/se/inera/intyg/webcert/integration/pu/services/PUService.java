package se.inera.intyg.webcert.integration.pu.services;

import com.google.common.annotations.VisibleForTesting;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.integration.pu.model.PersonSvar;

public interface PUService {

    PersonSvar getPerson(Personnummer personId);

    @VisibleForTesting
    void clearCache();
}
