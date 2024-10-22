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
package se.inera.intyg.webcert.web.web.controller.testability;

import io.swagger.annotations.Api;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.security.authorities.bootstrap.SecurityConfigurationLoader;
import se.inera.intyg.infra.security.common.model.Feature;

@Api(value = "testability configuration")
@Path("/config")
public class ConfigurationResource {

    @Autowired
    private SecurityConfigurationLoader configLoader;

    private final HashMap<String, Feature> replacedFeatures = new HashMap<>();

    @POST
    @Path("/setfeatures")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setFeatures(List<Feature> features) {
        final var currentFeatures = getCurrentFeatures();
        for (var feature : features) {
            try {
                final var replacedFeature = switchFeature(feature, currentFeatures);
                replacedFeatures.putIfAbsent(replacedFeature.getName(), replacedFeature);
            } catch (NoSuchElementException e) {
                return Response.status(Status.BAD_REQUEST.getStatusCode(), "Feature " + feature.getName() + " does not exist.")
                    .build();
            }
        }
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("/resetfeatures")
    public Response resetFeatures() {
        final var currentFeatures = getCurrentFeatures();
        for (var feature : replacedFeatures.entrySet()) {
            switchFeature(feature.getValue(), currentFeatures);
        }
        replacedFeatures.clear();
        return Response.status(Status.OK).build();
    }

    private Feature switchFeature(Feature feature, List<Feature> currentFeatures) {
        final var featureToSwitch = currentFeatures.stream().filter(f -> f.getName().equals(feature.getName()))
            .findFirst().orElseThrow();
        final var indexToSwitch = currentFeatures.indexOf(featureToSwitch);
        currentFeatures.set(indexToSwitch, feature);
        return featureToSwitch;
    }

    private List<Feature> getCurrentFeatures() {
        return configLoader.getFeaturesConfiguration().getFeatures();
    }
}
