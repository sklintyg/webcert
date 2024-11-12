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
package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import io.swagger.annotations.Api;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import se.inera.intyg.infra.security.common.model.UserOriginType;

/**
 * Controller to enable a landsting user to access certificates directly from a link.
 *
 * This controller inherits from the FragaSvarUthoppController which manages
 * request for UTHOPP links. This inheritance is somewhat confusing but it make
 * sense if we look at it from a functional perspective.
 */
// CHECKSTYLE:OFF LineLength
@Path("/basic-certificate")
@Api(value = "/webcert/web/user/basic-certificate", produces = MediaType.APPLICATION_JSON)
public class CertificateIntegrationController extends FragaSvarUthoppController {
    // CHECKSTYLE:ON LineLength

    @Override
    protected UserOriginType getGrantedRequestOrigin() {
        return UserOriginType.NORMAL;
    }

}
