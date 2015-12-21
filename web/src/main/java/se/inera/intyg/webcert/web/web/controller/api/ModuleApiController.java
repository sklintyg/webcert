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

package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Controller managing module wiring.
 */
@Path("/modules")
@Api(value = "modules", description = "REST API för att läsa ut intygsmoduler", produces = MediaType.APPLICATION_JSON)
public class ModuleApiController extends AbstractApiController {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    /**
     * Serving module configuration for Angular bootstrapping.
     *
     * @return a JSON object
     */
    @GET
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getModulesMap() {
        return Response.ok(moduleRegistry.listAllModules()).build();
    }
}
