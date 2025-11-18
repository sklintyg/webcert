/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.auth.eleg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class UnauthorizedPrivatePractitionerService {

    private final CommonAuthoritiesResolver authoritiesResolver;
    private final HashUtility hashUtility;
    private final PUService puService;

    public WebCertUser create(String personId, String origin, String authScheme, AuthenticationMethod authMethod) {
        final var role = authoritiesResolver.getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE_OBEHORIG);
        final var user = new WebCertUser("missing");
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setPersonId(personId);
        user.setNamn(getUserName(personId));
        user.setOrigin(origin);
        user.setAuthenticationScheme(authScheme);
        user.setAuthenticationMethod(authMethod);
        return user;
    }

    private String getUserName(String personId) {
        final var personNummer = Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new WebCertServiceException(
                    WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "Can't determine name for invalid personId %s".formatted(hashUtility.hash(personId))
                )
            );

        final var person = puService.getPerson(personNummer);
        if (person.getStatus() == PersonSvar.Status.FOUND) {
            return person.getPerson().fornamn() + " " + person.getPerson().efternamn();
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                "PU replied with %s - name cannot be determined for person %s"
                    .formatted(person.getStatus(), hashUtility.hash(personNummer.getPersonnummer()))
            );
        }
    }
}
