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
package se.inera.intyg.webcert.web.web.controller.internalapi;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.inera.intyg.webcert.web.service.erase.EraseService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

@Path("/v1/certificates")
public class EraseApiController extends AbstractApiController {

    @Value("${erase.certificates.page.size:1000}")
    private int eraseCertificatesPageSize;

    @Autowired
    private EraseService eraseService;

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void eraseDataForCareProvider(@PathParam("id") String careProviderId) {
        eraseService.eraseCertificates(careProviderId, eraseCertificatesPageSize);
    }
}
