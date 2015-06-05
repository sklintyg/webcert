package se.inera.webcert.pu.services;

import com.google.common.annotations.VisibleForTesting;
import se.inera.webcert.pu.model.PersonSvar;

public interface PUService {

    PersonSvar getPerson(String personId);

    @VisibleForTesting
    void clearCache();
}
