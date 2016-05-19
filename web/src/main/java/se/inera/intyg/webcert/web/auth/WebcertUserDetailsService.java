/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth;

import org.springframework.security.saml.SAMLCredential;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.security.common.model.IntygUser;
import se.inera.intyg.common.security.siths.BaseUserDetailsService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * As each application shall implement its own UserDetailsService, we simply extend the base one and implement all the
 * abstract methods.
 *
 * Created by eriklupander on 2016-05-17.
 */
@Service(value = "webcertUserDetailsService")
public class WebcertUserDetailsService extends BaseUserDetailsService {

    /**
     * Calls the default super() impl. from the base class and then builds a {@link WebCertUser} which is passed upwards
     * as Principal.
     *
     * @param credential
     *      The SAMLCredential.
     * @return
     *      WebCertUser as Principal.
     */
    @Override
    protected WebCertUser buildUserPrincipal(SAMLCredential credential) {
        IntygUser user = super.buildUserPrincipal(credential);
        return new WebCertUser(user);
    }

    /**
     * Makes sure that the default "fallback" role of Webcert is {@link AuthoritiesConstants#ROLE_ADMIN}.
     *
     * @return
     *      AuthoritiesConstants.ROLE_ADMIN as String.
     */
    @Override
    protected String getDefaultRole() {
        return AuthoritiesConstants.ROLE_ADMIN;
    }
}
