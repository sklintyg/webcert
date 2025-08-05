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

import static se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserUtil.convertRole;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;

@Component
@RequiredArgsConstructor
public class CertificateServiceIntegrationUserHelper {

    public CertificateServiceUserDTO get(IntygUser user) {
        return CertificateServiceUserDTO.builder()
            .id(user.getHsaId())
            .firstName(user.getFornamn())
            .lastName(user.getEfternamn())
            .fullName(user.getNamn())
            .blocked(false)
            .agreement(true)
            .paTitles(paTitles(user.getBefattningar()))
            .specialities(user.getSpecialiseringar())
            .role(getRole(user))
            .accessScope(AccessScopeType.WITHIN_CARE_UNIT)
            .healthCareProfessionalLicence(user.getLegitimeradeYrkesgrupper())
            .allowCopy(true)
            .srsActive(user.getFeatures().containsKey(AuthoritiesConstants.FEATURE_SRS))
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
            .toList();
    }

    private CertificateServiceUserRole getRole(IntygUser user) {
        final var roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            throw new IllegalStateException("User has no roles");
        }

        return convertRole(roles.values().stream().findFirst().orElseThrow().getName());
    }
}