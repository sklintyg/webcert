package se.inera.intyg.webcert.integration.hsa.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponderInterface;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonResponseType;
import se.riv.infrastructure.directory.authorizationmanagement.v1.GetCredentialsForPersonIncludingProtectedPersonType;
import se.riv.infrastructure.directory.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-12-04.
 */
@Service
public class AuthorizationManagementServiceImpl implements AuthorizationManagementService {

    @Autowired
    GetCredentialsForPersonIncludingProtectedPersonResponderInterface getCredentialsForPersonIncludingProtectedPersonResponderInterface;

    @Value("${infrastructure.directory.authorizationmanagement.logicalAddress}")
    String logicalAddress;

    @Override
    public GetCredentialsForPersonIncludingProtectedPersonResponseType getAuthorizationsForPerson(String personHsaId, String personalIdentityNumber,
            String searchBase) {
        GetCredentialsForPersonIncludingProtectedPersonType parameters = new GetCredentialsForPersonIncludingProtectedPersonType();
        parameters.setPersonalIdentityNumber(personalIdentityNumber);
        parameters.setPersonHsaId(personHsaId);
        parameters.setSearchBase(searchBase);
        GetCredentialsForPersonIncludingProtectedPersonResponseType response = getCredentialsForPersonIncludingProtectedPersonResponderInterface
                .getCredentialsForPersonIncludingProtectedPerson(logicalAddress, parameters);

        // TODO fix proper error handling...
        if (response.getResultCode() == ResultCodeEnum.ERROR) {
            return null;
        }
        return response;
    }
}
