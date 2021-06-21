/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOrigin;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResponse;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.pp.services.PPRestService;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.auth.common.BaseWebCertUserDetailsService;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.infrastructure.directory.privatepractitioner.v1.BefattningType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.LegitimeradYrkesgruppType;
import se.riv.infrastructure.directory.privatepractitioner.v1.SpecialitetType;

/**
 * Created by eriklupander on 2015-06-16.
 *
 * Note that privatlakare must accept webcert terms in order to use the software. However, that's
 * handled separately in the TermsFilter.
 */
@Component
public class ElegWebCertUserDetailsService extends BaseWebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(ElegWebCertUserDetailsService.class);

    @Value("${privatepractitioner.logicaladdress}")
    private String logicalAddress;

    @Autowired
    private PPService ppService;

    @Autowired
    private PPRestService ppRestService;

    @Autowired
    private PUService puService;

    @Autowired
    private AvtalService avtalService;

    @Autowired
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @Autowired
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;

    @Autowired
    private AnvandarPreferenceRepository anvandarPreferenceRepository;

    @Autowired
    private FeaturesHelper featuresHelper;

    @Autowired(required = false)
    private Optional<UserOrigin> userOrigin;

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    public Object loadUserBySAML(SAMLCredential samlCredential) {

        try {
            return createUser(samlCredential);
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }

            LOG.error("Error building user with error message {}", e.getMessage());
            throw new HsaServiceException("privatlakare, ej hsa", e);
        }
    }

    // - - - - - Default scope - - - - -

    protected WebCertUser createUser(SAMLCredential samlCredential) {
        final var personId = elegAuthenticationAttributeHelper.getAttribute(samlCredential, CgiElegAssertion.PERSON_ID_ATTRIBUTE);
        assertHosPersonIsAuthorized(personId);

        final var hosPerson = getAuthorizedHosPerson(personId);
        final var requestOrigin = resolveRequestOrigin();
        final var role = lookupUserRole();
        return createWebCertUser(hosPerson, requestOrigin, role, samlCredential);
    }

    private HoSPersonType getAuthorizedHosPerson(String personId) {
        HoSPersonType hosPerson = getHosPerson(personId);
        if (hosPerson == null) {
            throw new IllegalArgumentException("No HSAPerson found for personId specified in SAML ticket");
        }
        return hosPerson;
    }

    /*
     * This method only handles privatläkare for now.
     * In a future there might be more logic here to decide user role.
     */
    Role lookupUserRole() {
        return getAuthoritiesResolver().getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);
    }

    private void assertHosPersonIsAuthorized(String personId) {
        final var validationResponse = ppRestService.validatePrivatePractitioner(personId);
        if (validationResponse.getResultCode() == ValidatePrivatePractitionerResultCode.OK) {
            return;
        }

        if (subscriptionService.isAnySubscriptionFeatureActive() && isUnregisteredElegUser(validationResponse)
            && isMissingSubscription(personId)) {
            final var pnr = Personnummer.getPersonnummerHashSafe(Personnummer.createPersonnummer(personId).orElse(null));
            throw new MissingSubscriptionException("Private practitioner '" + pnr + "' has no active subscription.");
        }

        throw new PrivatePractitionerAuthorizationException("User is not authorized to access webcert according to private "
            + "practitioner portal");
    }

    private boolean isMissingSubscription(String personId) {
        return subscriptionService.checkSubscriptionUnregisteredElegUser(personId);
    }

    private boolean isUnregisteredElegUser(ValidatePrivatePractitionerResponse validationResponse) {
        return validationResponse.getResultCode() == ValidatePrivatePractitionerResultCode.ERROR_NO_ACCOUNT;
    }

    private String resolveRequestOrigin() {
        if (userOrigin.isEmpty()) {
            throw new IllegalStateException("No WebCertUserOrigin present, cannot login user.");
        }
        final var requestOrigin = userOrigin.get().resolveOrigin(getCurrentRequest());
        return getAuthoritiesResolver().getRequestOrigin(requestOrigin).getName();
    }

    private WebCertUser createWebCertUser(HoSPersonType hosPerson, String requestOrigin, Role role, SAMLCredential samlCredential) {
        WebCertUser user = new WebCertUser();

        user.setRoles(AuthoritiesResolverUtil.toMap(lookupUserRole()));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));

        // Set application mode / request origin
        user.setOrigin(requestOrigin);

        user.setHsaId(hosPerson.getHsaId().getExtension());
        user.setPersonId(hosPerson.getPersonId().getExtension());
        user.setNamn(hosPerson.getFullstandigtNamn());

        // Forskrivarkod should be always be seven zeros
        user.setForskrivarkod("0000000");

        decorateWebCertUserWithAuthenticationScheme(samlCredential, user);
        decorateWebCertUserWithAuthenticationMethod(samlCredential, user);
        decorateWebCertUserWithAvailableFeatures(user);
        decorateWebCertUserWithLegitimeradeYrkesgrupper(hosPerson, user);
        decorateWebCertUserWithSpecialiceringar(hosPerson, user);
        decorateWebCertUserWithVardgivare(hosPerson, user);
        decorateWebCertUserWithBefattningar(hosPerson, user);
        decorateWebCertUserWithDefaultVardenhet(user);
        decorateWebcertUserWithSekretessMarkering(user, hosPerson);
        decorateWebcertUserWithAnvandarPreferenser(user);
        decorateWebcertUserWithPrivatLakareAvtalGodkand(hosPerson, user);
        decorateWebcertUserWithSubscriptionInfo(user);

        return user;
    }

    private void decorateWebcertUserWithSubscriptionInfo(WebCertUser user) {
        final var subscriptionInfo = subscriptionService.checkSubscriptions(user);
        user.setSubscriptionInfo(subscriptionInfo);
    }

    private void decorateWebcertUserWithPrivatLakareAvtalGodkand(HoSPersonType hosPerson, WebCertUser user) {
            user.setPrivatLakareAvtalGodkand(avtalService.userHasApprovedLatestAvtal(hosPerson.getHsaId().getExtension()));
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

    private void decorateWebCertUserWithAuthenticationMethod(SAMLCredential samlCredential, WebCertUser webCertUser) {
        if (!webCertUser.getAuthenticationScheme().endsWith(":fake")) {
            webCertUser.setAuthenticationMethod(elegAuthenticationMethodResolver.resolveAuthenticationMethod(samlCredential));
        } else {
            webCertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
        }
    }

    private void decorateWebCertUserWithAuthenticationScheme(SAMLCredential samlCredential, WebCertUser webCertUser) {
        if (samlCredential.getAuthenticationAssertion() != null) {
            String authnContextClassRef = samlCredential.getAuthenticationAssertion().getAuthnStatements().get(0).getAuthnContext()
                .getAuthnContextClassRef().getAuthnContextClassRef();
            webCertUser.setAuthenticationScheme(authnContextClassRef);
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

        // Since privatläkare doesn't have "Medarbetaruppdrag" we cannot reliably populate "miuNamnPerVardenhetsId".
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
        List<String> befattningar = new ArrayList<>();
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
            || hosPerson.getEnhet().getArbetsplatskod().getExtension().trim().length() == 0) {
            vardenhet.setArbetsplatskod(hosPerson.getHsaId().getExtension());
        } else {
            vardenhet.setArbetsplatskod(hosPerson.getEnhet().getArbetsplatskod().getExtension());
        }
    }

    private boolean setFirstVardenhetOnFirstVardgivareAsDefault(WebCertUser user) {

        Vardgivare firstVardgivare = user.getVardgivare().get(0);
        user.setValdVardgivare(firstVardgivare);

        Vardenhet firstVardenhet = firstVardgivare.getVardenheter().get(0);
        user.setValdVardenhet(firstVardenhet);

        return true;
    }

}
