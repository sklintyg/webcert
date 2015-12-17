package se.inera.intyg.webcert.integration.hsa.client;

import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponseType;

/**
 * Created by eriklupander on 2015-12-04.
 */
public interface AuthorizationManagementService {

    GetCredentialsForPersonIncludingProtectedPersonResponseType getAuthorizationsForPerson(String personHsaId, String personalIdentityNumber, String searchBase);
}
