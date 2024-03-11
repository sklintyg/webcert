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
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
@RequiredArgsConstructor
public class CertificateServiceUserHelper {

    private final UserService userService;
    private final WebCertUserService webCertUserService;
    private final AuthoritiesHelper authoritiesHelper;

    public CertificateServiceUserDTO get() {
        final var user = userService.getLoggedInUser();
        final var webCertUser = webCertUserService.getUser();

        return CertificateServiceUserDTO.builder()
            .id(user.getHsaId())
            .firstName(webCertUser.getFornamn())
            .lastName(webCertUser.getEfternamn())
            .fullName(webCertUser.getNamn())
            .role(convertRole(user.getRole()))
            .blocked(isBlocked(webCertUser))
            .paTitles(paTitles(webCertUser.getBefattningar()))
            .specialities(webCertUser.getSpecialiseringar())
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

    private CertificateServiceUserRole convertRole(String role) {
        switch (role.toUpperCase()) {
            case "LAKARE":
            case "LÄKARE":
            case "DOCTOR":
                return CertificateServiceUserRole.DOCTOR;
            case "TANDLAKARE":
            case "TANDLÄKARE":
            case "DENTIST":
                return CertificateServiceUserRole.DENTIST;
            case "SJUKSKOTERSKA":
            case "SJUKSKÖTERSKA":
            case "NURSE":
                return CertificateServiceUserRole.NURSE;
            case "BARNMORSKA":
            case "MIDWIFE":
                return CertificateServiceUserRole.MIDWIFE;
            case "VARDADMIN":
            case "VÅRDADMINISTRATÖR":
            case "ADMINISTRATOR":
                return CertificateServiceUserRole.CARE_ADMIN;
            default:
                return CertificateServiceUserRole.UNKNOWN;
        }
    }
}
