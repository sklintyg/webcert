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

import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Component
public class CertificateServiceUserHelper {

    private final UserService userService;
    private final WebCertUserService webCertUserService;
    private final AuthoritiesHelper authoritiesHelper;

    // careprovider missing subscription list contains id
    // origin should be normal to check otherwise true
    // feature blocked, global

    // subscription exists -> inte blockerad
    // normal and no subscription och blockerad (globalt) aktiv -> blockerad
    // djupintegration -> aldrig blockerad

    public CertificateServiceUserHelper(UserService userService, WebCertUserService webCertUserService,
        AuthoritiesHelper authoritiesHelper) {
        this.userService = userService;
        this.webCertUserService = webCertUserService;
        this.authoritiesHelper = authoritiesHelper;
    }

    public CertificateServiceUser get() {
        final var user = userService.getLoggedInUser();

        return CertificateServiceUser.create(
            user.getHsaId(),
            convertRole(user.getRole()),
            isBlocked()
        );
    }

    private boolean isBlocked() {
        if (!webCertUserService.getUser().getOrigin().equals("NORMAL")) {
            return false;
        }

        if (hasSubscription()) {
            return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL);
        }

        return true;
    }

    private boolean hasSubscription() {
        final var user = webCertUserService.getUser();
        final var careProviderId = user.getValdVardgivare().getId();

        return !user.getSubscriptionInfo().getCareProvidersMissingSubscription().contains(careProviderId);
    }

    private CertificateServiceUserRole convertRole(String role) {
        switch (role) {
            case "LAKARE":
            case "DOCTOR":
                return CertificateServiceUserRole.DOCTOR;
            case "TANDLAKARE":
            case "DENTIST":
                return CertificateServiceUserRole.DENTIST;
            case "SJUKSKOTERSKA":
            case "NURSE":
                return CertificateServiceUserRole.NURSE;
            case "BARNMORSKA":
            case "MIDWIFE":
                return CertificateServiceUserRole.MIDWIFE;
            case "VARDADMIN":
            case "ADMINISTRATOR":
                return CertificateServiceUserRole.CARE_ADMIN;
            default:
                return CertificateServiceUserRole.UNKNOWN;
        }
    }
}
