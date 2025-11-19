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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.Code;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitioner;
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
@RequiredArgsConstructor
@Profile("private-practitioner-service-active")
public class AuthorizedPrivatePractitionerService {

    private final CommonAuthoritiesResolver commonAuthoritiesResolver;
    private final HashUtility hashUtility;
    private final AnvandarPreferenceRepository anvandarPreferenceRepository;
    private final PUService puService;
    private final PrivatePractitionerIntegrationService privatePractitionerIntegrationService;

    private static final String SPACE = " ";

    public WebCertUser create(String personId, String requestOrigin, String authenticationScheme,
        AuthenticationMethod authenticationMethodMethod) {
        final var privatePractitioner = getAuthorizedHosPerson(personId);
        final var webCertUser = createWebCertUser(privatePractitioner, requestOrigin);
        webCertUser.setAuthenticationScheme(authenticationScheme);
        webCertUser.setAuthenticationMethod(authenticationMethodMethod);
        return webCertUser;
    }

    private WebCertUser createWebCertUser(PrivatePractitioner privatePractitioner, String requestOrigin) {
        final var role = lookupUserRole();

        final var user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        if (!CollectionUtils.isEmpty(role.getPrivileges())) {
            user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        }
        user.setOrigin(requestOrigin);
        user.setHsaId(privatePractitioner.getHsaId());
        user.setPersonId(privatePractitioner.getPersonId());
        user.setNamn(getFullName(privatePractitioner, user));

        // Forskrivarkod should be always be seven zeros
        user.setForskrivarkod("0000000");

        decorateWebCertUserWithAvailableFeatures(user);
        decorateWebCertUserWithLegitimeradeYrkesgrupper(privatePractitioner, user);
        decorateWebCertUserWithSpecialiceringar(privatePractitioner, user);
        decorateWebCertUserWithVardgivare(privatePractitioner, user);
        decorateWebCertUserWithBefattningar(privatePractitioner, user);
        decorateWebCertUserWithDefaultVardenhet(user);
        decorateWebcertUserWithSekretessMarkering(user, privatePractitioner);
        decorateWebcertUserWithAnvandarPreferenser(user);
        decorateWebcertUserWithUserTermsApprovedOrSubscriptionInUse(user);
        return user;
    }

    private void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {
        if (webcertUser.getValdVardenhet() != null && webcertUser.getValdVardgivare() != null) {
            webcertUser.setFeatures(commonAuthoritiesResolver.getFeatures(
                Arrays.asList(webcertUser.getValdVardenhet().getId(), webcertUser.getValdVardgivare().getId())));
        } else {
            webcertUser.setFeatures(commonAuthoritiesResolver.getFeatures(Collections.emptyList()));
        }
    }

    private PrivatePractitioner getAuthorizedHosPerson(String personId) {
        final var hosPerson = privatePractitionerIntegrationService.getPrivatePractitioner(personId);
        if (hosPerson == null) {
            throw new IllegalArgumentException("No HSAPerson found for personId specified in SAML ticket");
        }
        return hosPerson;
    }

    Role lookupUserRole() {
        return commonAuthoritiesResolver.getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);
    }

    private String getFullName(PrivatePractitioner privatePractitioner, WebCertUser user) {
        final var fullName = privatePractitioner.getName();
        if (fullName != null && fullName.contains(SPACE)) {
            user.setFornamn(fullName.substring(0, fullName.indexOf(SPACE)));
            user.setEfternamn(fullName.substring(fullName.indexOf(SPACE) + 1));
        }
        return fullName;
    }

    private void decorateWebcertUserWithUserTermsApprovedOrSubscriptionInUse(WebCertUser user) {
        user.setUserTermsApprovedOrSubscriptionInUse(true);
    }

    private void decorateWebcertUserWithAnvandarPreferenser(WebCertUser user) {
        user.setAnvandarPreference(anvandarPreferenceRepository.getAnvandarPreference(user.getHsaId()));
    }

    private void decorateWebcertUserWithSekretessMarkering(WebCertUser webCertUser, PrivatePractitioner privatePractitioner) {
        // Make sure we have a valid personnr to work with..
        final var personNummer = Personnummer.createPersonnummer(privatePractitioner.getPersonId())
            .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                String.format("Can't determine sekretesstatus for invalid personId %s", privatePractitioner.getPersonId())));

        final var person = puService.getPerson(personNummer);
        if (person.getStatus() == PersonSvar.Status.FOUND) {
            webCertUser.setSekretessMarkerad(person.getPerson().sekretessmarkering());
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                String.format("PU replied with %s - Sekretesstatus cannot be determined for person %s", person.getStatus(),
                    hashUtility.hash(personNummer.getPersonnummer())));
        }
    }

    private void decorateWebCertUserWithDefaultVardenhet(WebCertUser webCertUser) {
        setFirstVardenhetOnFirstVardgivareAsDefault(webCertUser);
    }

    private void decorateWebCertUserWithVardgivare(PrivatePractitioner privatePractitioner, WebCertUser webCertUser) {
        final var id = privatePractitioner.getHsaId();
        final var namn = privatePractitioner.getCareProviderName();

        final var vardgivare = new Vardgivare(id, namn);

        final var vardenhet = new Vardenhet(privatePractitioner.getHsaId(), privatePractitioner.getCareUnitName());
        resolveArbetsplatsKod(privatePractitioner, vardenhet);
        vardenhet.setPostadress(privatePractitioner.getAddress());
        vardenhet.setPostnummer(privatePractitioner.getZipCode());
        vardenhet.setPostort(privatePractitioner.getCity());
        vardenhet.setTelefonnummer(privatePractitioner.getPhoneNumber());
        vardenhet.setEpost(privatePractitioner.getEmail());

        final var vardenhetList = new ArrayList<Vardenhet>();
        vardenhetList.add(vardenhet);
        vardgivare.setVardenheter(vardenhetList);

        final var vardgivareList = new ArrayList<Vardgivare>();
        vardgivareList.add(vardgivare);
        webCertUser.setVardgivare(vardgivareList);

        webCertUser.setValdVardenhet(vardenhet);
        webCertUser.setValdVardgivare(vardgivare);

        // Since privatläkare do not have "Medarbetaruppdrag" we cannot reliably populate "miuNamnPerVardenhetsId".
        // Populate with an empty map.
        webCertUser.setMiuNamnPerEnhetsId(new HashMap<>());
    }

    private void decorateWebCertUserWithLegitimeradeYrkesgrupper(PrivatePractitioner privatePractitioner, WebCertUser webCertUser) {
        webCertUser.setLegitimeradeYrkesgrupper(
            privatePractitioner.getLicensedHealthcareProfessions().stream()
                .map(Code::description)
                .toList()
        );
    }

    private void decorateWebCertUserWithSpecialiceringar(PrivatePractitioner privatePractitioner, WebCertUser webCertUser) {
        webCertUser.setSpecialiseringar(
            privatePractitioner.getSpecialties().stream()
                .map(Code::description)
                .toList()
        );
    }

    private void decorateWebCertUserWithBefattningar(PrivatePractitioner privatePractitioner, WebCertUser webCertUser) {
        webCertUser.setBefattningar(
            List.of(
                privatePractitioner.getPosition()
            )
        );
    }

    /**
     * Arbetsplatskod is not mandatory for Privatläkare. In that case, use the HSA-ID of the practitioner.
     * (See Informationspecification Webcert, version 4.6, page 83)
     */
    private void resolveArbetsplatsKod(PrivatePractitioner privatePractitioner, Vardenhet vardenhet) {
        if (privatePractitioner.getWorkplaceCode() == null || privatePractitioner.getWorkplaceCode().isBlank()) {
            vardenhet.setArbetsplatskod(privatePractitioner.getHsaId());
        } else {
            vardenhet.setArbetsplatskod(privatePractitioner.getWorkplaceCode());
        }
    }

    private void setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {
        final var firstVardgivare = user.getVardgivare().getFirst();
        final var firstVardenhet = firstVardgivare.getVardenheter().getFirst();
        user.setValdVardgivare(firstVardgivare);
        user.setValdVardenhet(firstVardenhet);
    }
}
