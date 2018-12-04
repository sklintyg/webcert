package se.inera.intyg.webcert.web.service.auth;

import se.inera.intyg.schemas.contract.Personnummer;

public interface AuthorityValidator {
    void assertIsAuthorized(Personnummer personnummer, String authority);
}
