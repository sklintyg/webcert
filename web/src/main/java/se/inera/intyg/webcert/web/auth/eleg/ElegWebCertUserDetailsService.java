/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode.NO_ACCOUNT;
import static se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode.OK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOrigin;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.pp.services.PPRestService;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;
import se.inera.intyg.webcert.web.auth.common.BaseWebCertUserDetailsService;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.infrastructure.directory.privatepractitioner.v1.BefattningType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.LegitimeradYrkesgruppType;
import se.riv.infrastructure.directory.privatepractitioner.v1.SpecialitetType;

@Component
@Slf4j
@RequiredArgsConstructor
public class ElegWebCertUserDetailsService extends BaseWebCertUserDetailsService {

    @Value("${privatepractitioner.logicaladdress}")
    private String logicalAddress;

    private final PPService ppService;
    private final PPRestService ppRestService;
    private final PUService puService;
    private final AnvandarPreferenceRepository anvandarPreferenceRepository;
    private final Optional<UserOrigin> userOrigin;
    private final SubscriptionService subscriptionService;


    public WebCertUser buildFakeUserPrincipal(String personId) {
        return buildUserPrincipal(personId, AuthConstants.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF, AuthenticationMethod.FAKE);
    }

    @Override
    public WebCertUser buildUserPrincipal(String personId, String authenticationScheme, AuthenticationMethod authenticationMethodMethod) {

        try {
            return createUser(personId, authenticationScheme, authenticationMethodMethod);
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }

            log.error("Error building user with error message {}", e.getMessage(), e);
            throw new HsaServiceException("privatlakare, ej hsa", e);
        }
    }

    protected WebCertUser createUser(String personId, String authenticationScheme, AuthenticationMethod authenticationMethodMethod) {
        final var ppAuthStatus = ppRestService.validatePrivatePractitioner(personId).getResultCode();
        redirectUnregisteredUsers(personId, ppAuthStatus);

        final var hosPerson = getAuthorizedHosPerson(personId);
        final var requestOrigin = resolveRequestOrigin();
        final var role = lookupUserRole();
        final var webCertUser = createWebCertUser(hosPerson, requestOrigin, role);
        webCertUser.setAuthenticationScheme(authenticationScheme);
        webCertUser.setAuthenticationMethod(authenticationMethodMethod);
        assertWebCertUserIsAuthorized(webCertUser, ppAuthStatus);

        return webCertUser;
    }

    private WebCertUser createWebCertUser(HoSPersonType hosPerson, String requestOrigin, Role role) {
        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(lookupUserRole()));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setOrigin(requestOrigin);
        user.setHsaId(hosPerson.getHsaId().getExtension());
        user.setPersonId(hosPerson.getPersonId().getExtension());
        user.setNamn(getFullName(hosPerson, user));

        // Forskrivarkod should be always be seven zeros
        user.setForskrivarkod("0000000");

        decorateWebCertUserWithAvailableFeatures(user);
        decorateWebCertUserWithLegitimeradeYrkesgrupper(hosPerson, user);
        decorateWebCertUserWithSpecialiceringar(hosPerson, user);
        decorateWebCertUserWithVardgivare(hosPerson, user);
        decorateWebCertUserWithBefattningar(hosPerson, user);
        decorateWebCertUserWithDefaultVardenhet(user);
        decorateWebcertUserWithSekretessMarkering(user, hosPerson);
        decorateWebcertUserWithAnvandarPreferenser(user);
        decorateWebcertUserWithUserTermsApprovedOrSubscriptionInUse(user);
        return user;
    }

    private void redirectUnregisteredUsers(String personId, ValidatePrivatePractitionerResultCode ppAuthStatus) {
        if (ppAuthStatus == NO_ACCOUNT) {
            final var hasSubscription = !subscriptionService.isUnregisteredElegUserMissingSubscription(personId);
            if (hasSubscription) {
                throw privatePractitionerAuthorizationException(hashed(personId));
            }
            throw missingSubscriptionException(hashed(personId));
        }
    }

    private void assertWebCertUserIsAuthorized(WebCertUser webCertUser, ValidatePrivatePractitionerResultCode ppAuthStatus) {
        final var hasSubscription = subscriptionService.checkSubscriptions(webCertUser);

        if (ppAuthStatus == OK) {
            return;
        }
        if (hasSubscription) {
            throw privatePractitionerAuthorizationException(webCertUser.getHsaId());
        }
        throw missingSubscriptionException(webCertUser.getHsaId());
    }

    private String hashed(String personId) {
        return Personnummer.getPersonnummerHashSafe(Personnummer.createPersonnummer(personId).orElse(null));
    }

    private PrivatePractitionerAuthorizationException privatePractitionerAuthorizationException(String hashedPersonIdOrHsaId) {
        return new PrivatePractitionerAuthorizationException("User '" + hashedPersonIdOrHsaId + "' is not authorized to access webcert "
            + "according to private practitioner portal");
    }

    private MissingSubscriptionException missingSubscriptionException(String hashedPersonIdOrHsaId) {
        return new MissingSubscriptionException("Private practitioner '" + hashedPersonIdOrHsaId + "' was denied access to Webcert due to "
            + "missing subscription.");
    }

    private HoSPersonType getAuthorizedHosPerson(String personId) {
        HoSPersonType hosPerson = getHosPerson(personId);
        if (hosPerson == null) {
            throw new IllegalArgumentException("No HSAPerson found for personId specified in SAML ticket");
        }
        return hosPerson;
    }

    Role lookupUserRole() {
        return getAuthoritiesResolver().getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);
    }

    private String resolveRequestOrigin() {
        if (userOrigin.isEmpty()) {
            throw new IllegalStateException("No WebCertUserOrigin present, cannot login user.");
        }
        final var requestOrigin = userOrigin.get().resolveOrigin(getCurrentRequest());
        return getAuthoritiesResolver().getRequestOrigin(requestOrigin).getName();
    }



    private String getFullName(HoSPersonType hosPerson, WebCertUser user) {
        final var fullName = hosPerson.getFullstandigtNamn();
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

    private void decorateWebcertUserWithSekretessMarkering(WebCertUser webCertUser, HoSPersonType hosPerson) {
        // Make sure we have a valid personnr to work with..
        Personnummer personNummer = Personnummer
            .createPersonnummer(hosPerson.getPersonId().getExtension())
            .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                String.format("Can't determine sekretesstatus for invalid personId %s",
                    hosPerson.getPersonId().getExtension())));

        PersonSvar person = puService.getPerson(personNummer);
        if (person.getStatus() == PersonSvar.Status.FOUND) {
            webCertUser.setSekretessMarkerad(person.getPerson().isSekretessmarkering());
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                String.format("PU replied with %s - Sekretesstatus cannot be determined for person %s", person.getStatus(),
                    personNummer.getPersonnummerHash()));
        }
    }

    private void decorateWebCertUserWithDefaultVardenhet(WebCertUser webCertUser) {
        setFirstVardenhetOnFirstVardgivareAsDefault(webCertUser);
    }

    private void decorateWebCertUserWithVardgivare(HoSPersonType hosPerson, WebCertUser webCertUser) {
        String id = hosPerson.getEnhet().getVardgivare().getVardgivareId().getExtension();
        String namn = hosPerson.getEnhet().getVardgivare().getVardgivarenamn();

        Vardgivare vardgivare = new Vardgivare(id, namn);

        Vardenhet vardenhet = new Vardenhet(hosPerson.getEnhet().getEnhetsId().getExtension(), hosPerson.getEnhet().getEnhetsnamn());
        resolveArbetsplatsKod(hosPerson, vardenhet);
        vardenhet.setPostadress(hosPerson.getEnhet().getPostadress());
        vardenhet.setPostnummer(hosPerson.getEnhet().getPostnummer());
        vardenhet.setPostort(hosPerson.getEnhet().getPostort());
        vardenhet.setTelefonnummer(hosPerson.getEnhet().getTelefonnummer());
        vardenhet.setEpost(hosPerson.getEnhet().getEpost());

        List<Vardenhet> vardenhetList = new ArrayList<>();
        vardenhetList.add(vardenhet);
        vardgivare.setVardenheter(vardenhetList);

        List<Vardgivare> vardgivareList = new ArrayList<>();
        vardgivareList.add(vardgivare);
        webCertUser.setVardgivare(vardgivareList);

        webCertUser.setValdVardenhet(vardenhet);
        webCertUser.setValdVardgivare(vardgivare);

        // Since privatläkare do not have "Medarbetaruppdrag" we cannot reliably populate "miuNamnPerVardenhetsId".
        // Populate with an empty map.
        webCertUser.setMiuNamnPerEnhetsId(new HashMap<>());
    }

    private void decorateWebCertUserWithLegitimeradeYrkesgrupper(HoSPersonType hosPerson, WebCertUser webCertUser) {
        List<String> legitimeradeYrkesgrupper = new ArrayList<>();
        for (LegitimeradYrkesgruppType ly : hosPerson.getLegitimeradYrkesgrupp()) {
            legitimeradeYrkesgrupper.add(ly.getNamn());
        }
        webCertUser.setLegitimeradeYrkesgrupper(legitimeradeYrkesgrupper);
    }

    private void decorateWebCertUserWithSpecialiceringar(HoSPersonType hosPerson, WebCertUser webCertUser) {
        List<String> specialiteter = new ArrayList<>();
        for (SpecialitetType st : hosPerson.getSpecialitet()) {
            specialiteter.add(st.getNamn());
        }
        webCertUser.setSpecialiseringar(specialiteter);
    }

    private void decorateWebCertUserWithBefattningar(HoSPersonType hosPerson, WebCertUser webCertUser) {
        final var befattningar = new ArrayList<String>();
        for (BefattningType bt : hosPerson.getBefattning()) {
            befattningar.add(bt.getNamn());
        }
        webCertUser.setBefattningar(befattningar);
    }

    private HoSPersonType getHosPerson(String personId) {
        return ppService.getPrivatePractitioner(logicalAddress, null, personId);
    }

    /**
     * Arbetsplatskod is not mandatory for Privatläkare. In that case, use the HSA-ID of the practitioner.
     * (See Informationspecification Webcert, version 4.6, page 83)
     */
    private void resolveArbetsplatsKod(HoSPersonType hosPerson, Vardenhet vardenhet) {
        if (hosPerson.getEnhet().getArbetsplatskod() == null || hosPerson.getEnhet().getArbetsplatskod().getExtension() == null
            || hosPerson.getEnhet().getArbetsplatskod().getExtension().trim().isEmpty()) {
            vardenhet.setArbetsplatskod(hosPerson.getHsaId().getExtension());
        } else {
            vardenhet.setArbetsplatskod(hosPerson.getEnhet().getArbetsplatskod().getExtension());
        }
    }

    private void setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {
        final var firstVardgivare = user.getVardgivare().getFirst();
        final var firstVardenhet = firstVardgivare.getVardenheter().getFirst();
        user.setValdVardgivare(firstVardgivare);
        user.setValdVardenhet(firstVardenhet);
    }

}
