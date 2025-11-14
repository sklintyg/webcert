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

import static se.inera.intyg.webcert.web.auth.eleg.ElegWebCertUserDetailsService.missingSubscriptionException;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class UnauthorizedPrivatePractitionerService {

    private final PUService puService;
    private final SubscriptionService subscriptionService;
    private final HashUtility hashUtility;
    private final CommonAuthoritiesResolver authoritiesResolver;

    public WebCertUser createUnauthorizedWebCertUser(String personId, String requestOrigin) {
        redirectUnsubscribedUsers(personId);
        final var role = authoritiesResolver.getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE_OBEHORIG);
        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setOrigin(requestOrigin);
        user.setPersonId(personId);
        user.setNamn(getUserName(personId));
        return user;
    }

    private void redirectUnsubscribedUsers(String personId) {
        if (!subscriptionService.isUnregisteredElegUserMissingSubscription(personId)) {
            throw missingSubscriptionException(hashUtility.hash(personId));
        }
    }

    private String getUserName(String personId) {
        final var personNummer = Personnummer
            .createPersonnummer(personId)
            .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                String.format("Can't determine name for invalid personId %s",
                    hashUtility.hash(personId))));

        PersonSvar person = puService.getPerson(personNummer);
        if (person.getStatus() == PersonSvar.Status.FOUND) {
            var name = String.join(" ", (StringUtils.hasText(person.getPerson().fornamn()) ? person.getPerson().fornamn() : "") +
                (StringUtils.hasText(person.getPerson().efternamn()) ? person.getPerson().efternamn() : ""));
            return StringUtils.hasText(name) ? name : "Okänd användare";
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                String.format("PU replied with %s - name cannot be determined for person %s", person.getStatus(),
                    hashUtility.hash(personNummer.getPersonnummer())));
        }
    }
}
