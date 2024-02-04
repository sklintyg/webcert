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

package se.inera.intyg.webcert.web.web.controller.testability.facade.csintegration;

import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserRole;

@Component
public class CertificateServiceUserBuilder {

    private static final String LAKARE = "LAKARE";
    private static final String VARDADMIN = "VARDADMINISTRATOR";
    private final WebcertUserDetailsService webcertUserDetailsService;

    public CertificateServiceUserBuilder(WebcertUserDetailsService webcertUserDetailsService) {
        this.webcertUserDetailsService = webcertUserDetailsService;
    }

    public CertificateServiceUserDTO build(HoSPersonal hoSPersonal) {
        final var user = webcertUserDetailsService.loadUserByHsaId(hoSPersonal.getPersonId());
        final var role = getRole(user.getRoles());
        return CertificateServiceUserDTO.create(hoSPersonal.getPersonId(), role, false);
    }

    private CertificateServiceUserRole getRole(Map<String, Role> roles) {
        if (roles.containsKey(LAKARE)) {
            return CertificateServiceUserRole.DOCTOR;
        }
        if (roles.containsKey(VARDADMIN)) {
            return CertificateServiceUserRole.CARE_ADMIN;
        }
        return CertificateServiceUserRole.UNKNOWN;
    }
}
