package se.inera.intyg.webcert.web.service.underskrift;

import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

public interface CommonUnderskriftService {
    SignaturBiljett skapaSigneringsBiljettMedDigest(String intygsId, String intygsTyp, long version, String intygJson);
}
