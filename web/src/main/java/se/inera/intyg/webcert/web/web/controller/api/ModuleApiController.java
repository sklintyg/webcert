/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.infra.dynamiclink.service.DynamicLinkService;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller managing module wiring.
 */
@Path("/modules")
@Api(value = "modules", description = "REST API för att läsa ut intygsmoduler", produces = MediaType.APPLICATION_JSON)
public class ModuleApiController extends AbstractApiController {

    private static final String DYNAMIC_LINK_PLACEHOLDER = "<LINK:";

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private WebcertFeatureService featureService;

    @Autowired
    private DynamicLinkService dynamicLinkService;

    /**
     * Serving module configuration for Angular bootstrapping.
     *
     * Note that IntygModule#setDetailedDescription may be invoked to replace dynamic link placeholders.
     *
     * @return a JSON object
     */
    @GET
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getModulesMap() {
        List<IntygModule> intygModules = moduleRegistry.listAllModules();
        intygModules.forEach(module -> {
            if (module.getDetailedDescription() != null) {
                module.setDetailedDescription(dynamicLinkService.apply(DYNAMIC_LINK_PLACEHOLDER, module.getDetailedDescription()));
            }
        });
        return Response.ok(moduleRegistry.listAllModules()).build();
    }

    @GET
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getActiveModules() {
        return Response.ok(moduleRegistry.listAllModules().stream()
                .filter(i -> featureService.isModuleFeatureActive(ModuleFeature.HANTERA_INTYGSUTKAST.getName(), i.getId()))
                .collect(Collectors.toList())).build();
    }
}
