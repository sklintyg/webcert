/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.underskrift.dss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DssSignMessageIdpProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DssSignMessageIdpProvider.class);

    private final String defaultIdpUrl;
    private final boolean useSameAsAuth;

    public DssSignMessageIdpProvider(
        @Value("${dss.service.idpurl}") String defaultIdpUrl,
        @Value("${dss.service.idpurl.sameAsAuth}") boolean useSameAsAuth) {
        this.defaultIdpUrl = defaultIdpUrl;
        this.useSameAsAuth = useSameAsAuth;
    }

    public String get(String identityProviderForSign) {
        if (useIdentityProviderForSign(identityProviderForSign)) {
            return identityProviderForSign;
        }
        return defaultIdpUrl;
    }

    private boolean useIdentityProviderForSign(String identityProviderForSign) {
        if (!useSameAsAuth) {
            return false;
        }
        if (identityProviderForSign == null || identityProviderForSign.isEmpty()) {
            LOG.warn("IdentityProviderForSign-attribute is missing! Default idp is used instead.");
            return false;
        }
        return true;
    }
}
