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
package se.inera.intyg.webcert.web.web.controller.facade.util;

import java.util.Objects;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
public class AngularClientUtil {

    private static final String REHABSTOD_LAUNCH_ORIGIN = "rs";

    public boolean useAngularClient(WebCertUser user) {
        if (isOriginDifferentThanDjupintegration(user)) {
            return false;
        }

        final var feature = user.getFeatures().get(AuthoritiesConstants.FEATURE_USE_ANGULAR_WEBCLIENT);
        if (feature == null) {
            return false;
        }

        return feature.getGlobal();
    }

    private boolean isOriginDifferentThanDjupintegration(WebCertUser user) {
        return !UserOriginType.DJUPINTEGRATION.name().equalsIgnoreCase(user.getOrigin()) && !Objects.equals(user.getLaunchFromOrigin(),
            REHABSTOD_LAUNCH_ORIGIN);
    }
}
