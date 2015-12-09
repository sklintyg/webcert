package se.inera.intyg.webcert.integration.hsa.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponderInterface;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonType;

/**
 * Created by eriklupander on 2015-12-04.
 */
@Service
public class AuthorizationManagementServiceImpl implements AuthorizationManagementService {

    @Autowired
    private GetCredentialsForPersonIncludingProtectedPersonResponderInterface getCredentialsForPersonIncludingProtectedPersonResponderInterface;

    @Value("${infrastructure.directory.authorizationmanagement.logicalAddress}")
    private String logicalAddress;

    @Override
    public GetCredentialsForPersonIncludingProtectedPersonResponseType getAuthorizationsForPerson(String personHsaId, String personalIdentityNumber,
            String searchBase) {
        GetCredentialsForPersonIncludingProtectedPersonType parameters = new GetCredentialsForPersonIncludingProtectedPersonType();
        parameters.setPersonalIdentityNumber(personalIdentityNumber);
        parameters.setPersonHsaId(personHsaId);
        parameters.setSearchBase(searchBase);
        GetCredentialsForPersonIncludingProtectedPersonResponseType response = getCredentialsForPersonIncludingProtectedPersonResponderInterface
                .getCredentialsForPersonIncludingProtectedPerson(logicalAddress, parameters);

        return response;
    }
}
