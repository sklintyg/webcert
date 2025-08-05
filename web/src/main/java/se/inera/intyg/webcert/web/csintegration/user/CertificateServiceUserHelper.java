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

package se.inera.intyg.webcert.web.csintegration.user;

import static java.util.Optional.ofNullable;
import static se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserUtil.convertRole;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
@RequiredArgsConstructor
public class CertificateServiceUserHelper {

    private static final String INTEGRATED = "DJUPINTEGRATION";
    private final WebCertUserService webCertUserService;

    public CertificateServiceUserDTO get() {
        final var user = webCertUserService.getUser();
        return CertificateServiceUserDTO.builder()
            .id(user.getHsaId())
            .firstName(user.getFornamn())
            .lastName(user.getEfternamn())
            .fullName(user.getNamn())
            .blocked(isBlocked(user))
            .agreement(hasAgreement(user))
            .paTitles(paTitles(user.getBefattningar()))
            .specialities(user.getSpecialiseringar())
            .accessScope(getAccessScope(user))
            .allowCopy(user.getParameters() == null || user.getParameters().isFornyaOk())
            .healthCareProfessionalLicence(user.getLegitimeradeYrkesgrupper())
            .role(getRole(user))
            .responsibleHospName(user.getParameters() == null ? null : user.getParameters().getResponsibleHospName())
            .srsActive(user.getFeatures().containsKey(AuthoritiesConstants.FEATURE_SRS))
            .build();
    }

    private AccessScopeType getAccessScope(WebCertUser user) {
        if (user.getOrigin().equals(INTEGRATED) && user.isSjfActive()) {
            return AccessScopeType.ALL_CARE_PROVIDERS;
        }
        if (user.getOrigin().equals(INTEGRATED)) {
            return AccessScopeType.WITHIN_CARE_PROVIDER;
        }

        return AccessScopeType.WITHIN_CARE_UNIT;
    }

    private List<PaTitleDTO> paTitles(List<String> befattningar) {
        return befattningar.stream()
            .map(befattning ->
                PaTitleDTO.builder()
                    .code(befattning)
                    .description(BefattningService.getDescriptionFromCode(befattning).orElse(befattning))
                    .build()
            )
            .toList();
    }

    private boolean isBlocked(WebCertUser webCertUser) {
        if (!webCertUser.getOrigin().equals(UserOriginType.NORMAL.name())) {
            return false;
        }

        return ofNullable(webCertUser.getFeatures().get(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL))
            .filter(Feature::getGlobal)
            .isPresent();
    }

    private boolean hasAgreement(WebCertUser webCertUser) {
        if (!webCertUser.getOrigin().equals(UserOriginType.NORMAL.name())) {
            return true;
        }

        return hasSubscription(webCertUser);
    }

    private boolean hasSubscription(WebCertUser webCertUser) {
        if (ifUserHasntSelectedLoggedInUnitYet(webCertUser)) {
            return true;
        }
        final var careProviderId = webCertUser.getValdVardgivare().getId();

        return !webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().contains(careProviderId);
    }

    private static boolean ifUserHasntSelectedLoggedInUnitYet(WebCertUser webCertUser) {
        return webCertUser.getValdVardgivare() == null;
    }

    private CertificateServiceUserRole getRole(WebCertUser webCertUser) {
        final var roles = webCertUser.getRoles();
        if (roles == null || roles.isEmpty()) {
            throw new IllegalStateException("User has no roles");
        }

        return convertRole(roles.values().stream().findFirst().orElseThrow().getName());
    }
}