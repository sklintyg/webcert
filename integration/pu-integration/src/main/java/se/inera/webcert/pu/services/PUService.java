package se.inera.webcert.pu.services;

import com.google.common.annotations.VisibleForTesting;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.webcert.pu.model.PersonSvar;

public interface PUService {

    PersonSvar getPerson(Personnummer personId);

    @VisibleForTesting
    void clearCache();
}
