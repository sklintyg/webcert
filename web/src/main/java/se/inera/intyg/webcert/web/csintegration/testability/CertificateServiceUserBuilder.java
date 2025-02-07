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

package se.inera.intyg.webcert.web.csintegration.testability;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserRole;
import se.inera.intyg.webcert.web.csintegration.user.PaTitleDTO;

@Component
public class CertificateServiceUserBuilder {

    private final WebcertUserDetailsService webcertUserDetailsService;

    public CertificateServiceUserBuilder(WebcertUserDetailsService webcertUserDetailsService) {
        this.webcertUserDetailsService = webcertUserDetailsService;
    }

    public CertificateServiceUserDTO build(HoSPersonal hoSPersonal) {
        final var user = webcertUserDetailsService.buildUserPrincipal(hoSPersonal.getPersonId(), "");
        final var role = getRole(user.getRoles());
        return CertificateServiceUserDTO.builder()
            .id(hoSPersonal.getPersonId())
            .firstName(user.getFornamn())
            .lastName(user.getEfternamn())
            .fullName(user.getEfternamn())
            .specialities(user.getSpecialiseringar())
            .paTitles(paTitles(user.getBefattningar()))
            .role(role)
            .blocked(false)
            .allowCopy(true)
            .agreement(true)
            .healthCareProfessionalLicence(user.getLegitimeradeYrkesgrupper())
            .build();
    }

    private CertificateServiceUserRole getRole(Map<String, Role> roles) {
        if (roles.containsKey(AuthoritiesConstants.ROLE_LAKARE)) {
            return CertificateServiceUserRole.DOCTOR;
        }
        if (roles.containsKey(AuthoritiesConstants.ROLE_ADMIN)) {
            return CertificateServiceUserRole.CARE_ADMIN;
        }
        if (roles.containsKey(AuthoritiesConstants.ROLE_SJUKSKOTERSKA)) {
            return CertificateServiceUserRole.NURSE;
        }
        if (roles.containsKey(AuthoritiesConstants.ROLE_BARNMORSKA)) {
            return CertificateServiceUserRole.MIDWIFE;
        }
        if (roles.containsKey(AuthoritiesConstants.ROLE_PRIVATLAKARE)) {
            return CertificateServiceUserRole.PRIVATE_DOCTOR;
        }
        if (roles.containsKey(AuthoritiesConstants.ROLE_TANDLAKARE)) {
            return CertificateServiceUserRole.DENTIST;
        }
        return CertificateServiceUserRole.UNKNOWN;
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
}
