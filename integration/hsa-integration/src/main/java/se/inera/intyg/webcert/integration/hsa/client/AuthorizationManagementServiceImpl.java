/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    @Value("${infrastructure.directory.logicalAddress}")
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
