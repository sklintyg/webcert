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

package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.integration.pu.model.PersonSvar;
import se.inera.intyg.webcert.integration.pu.services.PUService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.PersonuppgifterResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/person")
@Api(value = "person", description = "REST API för personslagningar", produces = MediaType.APPLICATION_JSON)
public class PersonApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonApiController.class);

    @Autowired
    private PUService puService;

    @Autowired
    private MonitoringLogService monitoringService;

    @GET
    @Path("/{personnummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getPersonuppgifter(@PathParam("personnummer") String personnummerIn) {
        Personnummer personnummer = new Personnummer(personnummerIn);
        LOG.debug("Hämtar personuppgifter för: {}", personnummer.getPnrHash());

        PersonSvar personSvar = puService.getPerson(personnummer);

        monitoringService.logPULookup(personnummer, personSvar.getStatus().name());

        return Response.ok(new PersonuppgifterResponse(personSvar)).build();
    }
}
