package se.inera.intyg.webcert.web.service.underskrift.fake;

import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public interface FakeUnderskriftService {
    SignaturBiljett finalizeFakeSignature(String ticketId, Utkast utkast, WebCertUser user);
}
