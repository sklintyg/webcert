/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.annotations.Api;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.dynamiclink.service.DynamicLinkService;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygModuleDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

/**
 * Controller managing module wiring.
 */
@Path("/modules")
@Api(value = "modules", description = "REST API för att läsa ut intygsmoduler", produces = MediaType.APPLICATION_JSON)
public class ModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleApiController.class);

    private static final String DYNAMIC_LINK_PLACEHOLDER = "<LINK:";

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private DynamicLinkService dynamicLinkService;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    @Autowired
    private ResourceLinkHelper resourceLinkHelper;

    /**
     * Serving module configuration for Angular bootstrapping.
     * <p>
     * Note that IntygModule#setDetailedDescription may be invoked to replace dynamic link placeholders.
     *
     * @return a JSON object
     */
    @GET
    @Path("/map")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getModulesMap() {
        return Response.ok(moduleRegistry.listAllModules()).build();
    }

    /**
     * Serving module configuration populating selectors based on user.
     *
     * @return a JSON object
     */
    @GET
    @Path("/map/{patientId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getModulesMap(@PathParam("patientId") String patientId) {

        try {
            Personnummer personnummer = createPnr(patientId);

            SekretessStatus sekretessmarkering = patientDetailsResolver.getSekretessStatus(personnummer);
            List<IntygModule> intygModules = moduleRegistry.listAllModules();

            final List<IntygModuleDTO> intygModuleDTOS = intygModules.stream()
                    .map(intygModule -> new IntygModuleDTO(intygModule))
                    .collect(Collectors.toList());

            resourceLinkHelper.decorateWithValidActionLinks(intygModuleDTOS, personnummer);

            return Response.ok(intygModuleDTOS).build();

        } catch (InvalidPersonNummerException e) {
            LOG.error(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getActiveModules() {
        // Cannot use user as this is used before login
        return Response.ok(moduleRegistry.listAllModules().stream()
                .filter(i -> authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST, i.getId()))
                .filter(m -> !m.getId().equals(Fk7263EntryPoint.MODULE_ID)) // Special case for fk7263
                .collect(Collectors.toList())).build();
    }

    private Personnummer createPnr(String personId) throws InvalidPersonNummerException {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new InvalidPersonNummerException("Could not parse personnummer: " + personId));
    }

}
