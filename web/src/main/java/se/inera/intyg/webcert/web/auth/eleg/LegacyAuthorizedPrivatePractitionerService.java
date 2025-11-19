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
import org.springframework.beans.factory.annotation.Value;
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
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.infrastructure.directory.privatepractitioner.v1.BefattningType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.LegitimeradYrkesgruppType;
import se.riv.infrastructure.directory.privatepractitioner.v1.SpecialitetType;

@Service
@RequiredArgsConstructor
public class LegacyAuthorizedPrivatePractitionerService {

    @Value("${privatepractitioner.logicaladdress}")
    private String logicalAddress;

    private final CommonAuthoritiesResolver commonAuthoritiesResolver;
    private final HashUtility hashUtility;
    private final PPService ppService;
    private final AnvandarPreferenceRepository anvandarPreferenceRepository;
    private final PUService puService;

    private static final String SPACE = " ";

    public WebCertUser create(String personId, String requestOrigin, String authenticationScheme,
        AuthenticationMethod authenticationMethodMethod) {
        final var hosPerson = getAuthorizedHosPerson(personId);
        final var webCertUser = createWebCertUser(hosPerson, requestOrigin);
        webCertUser.setAuthenticationScheme(authenticationScheme);
        webCertUser.setAuthenticationMethod(authenticationMethodMethod);
        return webCertUser;
    }

    private WebCertUser createWebCertUser(HoSPersonType hosPerson, String requestOrigin) {
        final var role = lookupUserRole();

        final var user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        if (!CollectionUtils.isEmpty(role.getPrivileges())) {
            user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        }
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

    private void decorateWebCertUserWithAvailableFeatures(WebCertUser webcertUser) {
        if (webcertUser.getValdVardenhet() != null && webcertUser.getValdVardgivare() != null) {
            webcertUser.setFeatures(commonAuthoritiesResolver.getFeatures(
                Arrays.asList(webcertUser.getValdVardenhet().getId(), webcertUser.getValdVardgivare().getId())));
        } else {
            webcertUser.setFeatures(commonAuthoritiesResolver.getFeatures(Collections.emptyList()));
        }
    }

    private HoSPersonType getAuthorizedHosPerson(String personId) {
        final var hosPerson = getHosPerson(personId);
        if (hosPerson == null) {
            throw new IllegalArgumentException("No HSAPerson found for personId specified in SAML ticket");
        }
        return hosPerson;
    }

    Role lookupUserRole() {
        return commonAuthoritiesResolver.getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);
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
        final var personNummer = Personnummer.createPersonnummer(hosPerson.getPersonId().getExtension())
            .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                String.format("Can't determine sekretesstatus for invalid personId %s",
                    hosPerson.getPersonId().getExtension())));

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

    private void decorateWebCertUserWithVardgivare(HoSPersonType hosPerson, WebCertUser webCertUser) {
        final var id = hosPerson.getEnhet().getVardgivare().getVardgivareId().getExtension();
        final var namn = hosPerson.getEnhet().getVardgivare().getVardgivarenamn();

        final var vardgivare = new Vardgivare(id, namn);

        final var vardenhet = new Vardenhet(hosPerson.getEnhet().getEnhetsId().getExtension(), hosPerson.getEnhet().getEnhetsnamn());
        resolveArbetsplatsKod(hosPerson, vardenhet);
        vardenhet.setPostadress(hosPerson.getEnhet().getPostadress());
        vardenhet.setPostnummer(hosPerson.getEnhet().getPostnummer());
        vardenhet.setPostort(hosPerson.getEnhet().getPostort());
        vardenhet.setTelefonnummer(hosPerson.getEnhet().getTelefonnummer());
        vardenhet.setEpost(hosPerson.getEnhet().getEpost());

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

    private void decorateWebCertUserWithLegitimeradeYrkesgrupper(HoSPersonType hosPerson, WebCertUser webCertUser) {
        final var legitimeradeYrkesgrupper = new ArrayList<String>();
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
        if (hosPerson.getEnhet().getArbetsplatskod() == null
            || hosPerson.getEnhet().getArbetsplatskod().getExtension() == null
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
