/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;

/**
 * Controller to enable a landsting user to access certificates directly from a link.
 */
@Path("/basic-certificate")
@Api(value = "/webcert/web/user/basic-certificate", description = "REST API för djupintegration", produces = MediaType.APPLICATION_JSON)
public class CertificateIntegrationController extends LegacyIntygIntegrationController {

    private static final String[] GRANTED_ROLES = new String[] { UserRole.ROLE_LAKARE.name(), UserRole.ROLE_VARDADMINISTRATOR.name() };

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

}
