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
package se.inera.intyg.webcert.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.infra.security.siths.BaseUserDetailsService;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service(value = "webcertUserDetailsService")
@RequiredArgsConstructor
public class WebcertUserDetailsService extends BaseUserDetailsService {

    private final AnvandarPreferenceRepository anvandarMetadataRepository;
    private final SubscriptionService subscriptionService;

    public WebCertUser buildFakeUserPrincipal(String hsaId) {
        return buildUserPrincipal(hsaId, AuthConstants.FAKE_AUTHENTICATION_SITHS_CONTEXT_REF, "fake_signature_provider");
    }

    public WebCertUser buildUserPrincipal(String employeeHsaId, String authenticationScheme, String identityProviderForSign) {
        final var webCertUser = buildUserPrincipal(employeeHsaId, authenticationScheme);
        webCertUser.setIdentityProviderForSign(identityProviderForSign);
        return webCertUser;
    }

    @Override
    public WebCertUser buildUserPrincipal(String employeeHsaId, String authenticationScheme) {
        final var user = super.buildUserPrincipal(employeeHsaId, authenticationScheme);
        final var  webCertUser = new WebCertUser(user);
        webCertUser.setAnvandarPreference(anvandarMetadataRepository.getAnvandarPreference(webCertUser.getHsaId()));
        subscriptionService.checkSubscriptions(webCertUser);
        return webCertUser;
    }

    /**
     * Makes sure that the default "fallback" role of Webcert is {@link AuthoritiesConstants#ROLE_ADMIN}.
     *
     * @return AuthoritiesConstants.ROLE_ADMIN as String.
     */
    @Override
    protected String getDefaultRole() {
        return AuthoritiesConstants.ROLE_ADMIN;
    }

    /**
     * Webcert overrides the default (i.e. fallback) behaviour from the base class which specifies a pre-selected
     * Vårdenhet during the authorization process:
     * <p>
     * For users with origin {@link UserOriginType#NORMAL} users will be redirected to the Vårdenhet selection page if
     * they have more than one (1) possible vårdenhet they have the requisite medarbetaruppdrag to select. (INTYG-3211)
     *
     * @param intygUser User principal.
     */
    @Override
    protected void decorateIntygUserWithDefaultVardenhet(IntygUser intygUser) {
        if (!UserOriginType.NORMAL.name().equals(intygUser.getOrigin())) {
            super.decorateIntygUserWithDefaultVardenhet(intygUser);
            return;
        }

        final var  nrUnitsToSelectFrom = intygUser.getVardgivare().stream().mapToLong(vg -> vg.getVardenheter().size()).sum();

        if (nrUnitsToSelectFrom == 1) {
            super.decorateIntygUserWithDefaultVardenhet(intygUser);
        }
    }

    @Override
    protected HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
