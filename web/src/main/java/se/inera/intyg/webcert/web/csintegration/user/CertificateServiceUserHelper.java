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

package se.inera.intyg.webcert.web.csintegration.user;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
@RequiredArgsConstructor
public class CertificateServiceUserHelper {

    private final WebCertUserService webCertUserService;
    private final AuthoritiesHelper authoritiesHelper;

    public CertificateServiceUserDTO get() {
        final var webCertUser = webCertUserService.getUser();

        return CertificateServiceUserDTO.builder()
            .id(webCertUser.getHsaId())
            .firstName(webCertUser.getFornamn())
            .lastName(webCertUser.getEfternamn())
            .fullName(webCertUser.getNamn())
            .blocked(isBlocked(webCertUser))
            .paTitles(paTitles(webCertUser.getBefattningar()))
            .specialities(webCertUser.getSpecialiseringar())
            .role(getRole(webCertUser))
            .build();
    }

    private List<PaTitleDTO> paTitles(List<String> befattningar) {
        return befattningar.stream()
            .map(befattning ->
                PaTitleDTO.builder()
                    .code(befattning)
                    .description(BefattningService.getDescriptionFromCode(befattning).orElse(befattning))
                    .build()
            )
            .collect(Collectors.toList());
    }

    private boolean isBlocked(WebCertUser webCertUser) {
        if (!webCertUser.getOrigin().equals(UserOriginType.NORMAL.name())) {
            return false;
        }

        if (hasSubscription(webCertUser)) {
            return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL);
        }

        return true;
    }

    private boolean hasSubscription(WebCertUser webCertUser) {
        final var careProviderId = webCertUser.getValdVardgivare().getId();

        return !webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().contains(careProviderId);
    }

    private CertificateServiceUserRole getRole(WebCertUser webCertUser) {
        final var roles = webCertUser.getRoles();
        if (roles == null || roles.values().isEmpty()) {
            throw new IllegalStateException("User has no roles");
        }

        return convertRole(roles.values().stream().findFirst().orElseThrow().getName());
    }

    private CertificateServiceUserRole convertRole(String role) {
        switch (role.toUpperCase()) {
            case AuthoritiesConstants.ROLE_LAKARE:
                return CertificateServiceUserRole.DOCTOR;
            case AuthoritiesConstants.ROLE_PRIVATLAKARE:
                return CertificateServiceUserRole.PRIVATE_DOCTOR;
            case AuthoritiesConstants.ROLE_TANDLAKARE:
                return CertificateServiceUserRole.DENTIST;
            case AuthoritiesConstants.ROLE_ADMIN:
                return CertificateServiceUserRole.CARE_ADMIN;
            case AuthoritiesConstants.ROLE_SJUKSKOTERSKA:
                return CertificateServiceUserRole.NURSE;
            case AuthoritiesConstants.ROLE_BARNMORSKA:
                return CertificateServiceUserRole.MIDWIFE;
            default:
                throw new IllegalArgumentException("Role is not recognized: " + role);
        }
    }
}
