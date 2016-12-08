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
package se.inera.intyg.webcert.web.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;

import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.infra.security.siths.BaseUserDetailsService;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * As each application shall implement its own UserDetailsService, we simply extend the base one and implement all the
 * abstract methods.
 *
 * Created by eriklupander on 2016-05-17.
 */
@Service(value = "webcertUserDetailsService")
public class WebcertUserDetailsService extends BaseUserDetailsService {

    @Autowired
    AnvandarPreferenceRepository anvandarMetadataRepository;

    /**
     * Calls the default super() impl. from the base class and then builds a {@link WebCertUser} which is passed upwards
     * as Principal.
     *
     * @param credential
     *            The SAMLCredential.
     * @return
     *         WebCertUser as Principal.
     */
    @Override
    protected WebCertUser buildUserPrincipal(SAMLCredential credential) {
        IntygUser user = super.buildUserPrincipal(credential);
        WebCertUser webCertUser = new WebCertUser(user);
        webCertUser.setAnvandarPreference(anvandarMetadataRepository.getAnvandarPreference(webCertUser.getHsaId()));
        return webCertUser;
    }

    /**
     * Makes sure that the default "fallback" role of Webcert is {@link AuthoritiesConstants#ROLE_ADMIN}.
     *
     * @return
     *         AuthoritiesConstants.ROLE_ADMIN as String.
     */
    @Override
    protected String getDefaultRole() {
        return AuthoritiesConstants.ROLE_ADMIN;
    }

    /**
     * Webcert overrides the default (i.e. fallback) behaviour from the base class which specifies a pre-selected
     * Vårdenhet during the
     * authorization process:
     *
     * For users with origin {@link UserOriginType#NORMAL} users will be redirected to the Vårdenhet selection page if
     * they have more than one (1) possible vårdenhet they have the requisite medarbetaruppdrag to select. (INTYG-3211)
     *
     * @param intygUser
     *            User principal.
     */
    @Override
    protected void decorateIntygUserWithDefaultVardenhet(IntygUser intygUser) {
        // This override should only apply to NORMAL origin logins. Other types of origins gets default behaviour.
        if (!UserOriginType.NORMAL.name().equals(intygUser.getOrigin())) {
            super.decorateIntygUserWithDefaultVardenhet(intygUser);
            return;
        }

        final long nrUnitsToSelectFrom = intygUser.getVardgivare().stream().flatMap(vg -> vg.getVardenheter().stream()).count();

        // If only 1 unit to select from - select it for them. Otherwise leave it unselected.
        if (nrUnitsToSelectFrom == 1) {
            super.decorateIntygUserWithDefaultVardenhet(intygUser);
        }
    }
}
