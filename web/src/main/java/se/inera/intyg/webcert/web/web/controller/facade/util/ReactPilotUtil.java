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

import org.springframework.stereotype.Component;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Component
public class ReactPilotUtil {

    public boolean useReactClient(WebCertUser user, String certificateType) {
        if (isOriginDifferentThanDjupintegration(user)) {
            return false;
        }

        final var feature = user.getFeatures().get(AuthoritiesConstants.FEATURE_USE_REACT_WEBCLIENT);
        if (feature == null) {
            return false;
        }

        return isFeatureActive(certificateType, feature);
    }

    public boolean useReactClientFristaende(WebCertUser user, String certificateType) {
        if (isOriginDifferentThanFristaende(user)) {
            return false;
        }

        final var feature = user.getFeatures().get(AuthoritiesConstants.FEATURE_USE_REACT_WEBCLIENT_FRISTAENDE);
        if (feature == null) {
            return false;
        }

        return isFeatureActive(certificateType, feature);
    }

    private static boolean isFeatureActive(String certificateType, Feature feature) {
        return (feature.getIntygstyper().isEmpty() || feature.getIntygstyper().contains(certificateType)) && feature.getGlobal();
    }

    private boolean isOriginDifferentThanDjupintegration(WebCertUser user) {
        return !UserOriginType.DJUPINTEGRATION.name().equalsIgnoreCase(user.getOrigin());
    }

    private boolean isOriginDifferentThanFristaende(WebCertUser user) {
        return !UserOriginType.NORMAL.name().equalsIgnoreCase(user.getOrigin());
    }
}
