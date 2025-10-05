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

package se.inera.intyg.webcert.web.service.user;

import java.util.Map;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUser;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
public class LoggedInWebcertUserFactory {

    public LoggedInWebcertUser create(IntygUser user) {
        return LoggedInWebcertUser.builder()
            .staffId(user.getHsaId())
            .role(role(user.getRoles()))
            .unitId(selectedUnitId(user))
            .careProviderId(selectedCareProviderId(user))
            .build();
    }

    public LoggedInWebcertUser create(WebCertUser user) {
        return LoggedInWebcertUser.builder()
            .staffId(user.getHsaId())
            .role(role(user.getRoles()))
            .unitId(selectedUnitId(user))
            .careProviderId(selectedCareProviderId(user))
            .origin(user.getOrigin())
            .build();
    }

    private static String selectedUnitId(IntygUser user) {
        final var valdVardenhet = user.getValdVardenhet();
        return valdVardenhet == null ? null : valdVardenhet.getId();
    }

    private static String selectedCareProviderId(IntygUser user) {
        final var valdVardgivare = user.getValdVardgivare();
        return valdVardgivare == null ? null : valdVardgivare.getId();
    }

    private String role(Map<String, Role> roles) {
        return roles != null && roles.size() == 1 ? roles.keySet().iterator().next() : null;
    }
}