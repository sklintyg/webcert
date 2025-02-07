/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.persistence.referens.model.Referens;
import se.inera.intyg.webcert.persistence.referens.repository.ReferensRepository;

@Path("referens")
public class ReferensResource {

    public static final Logger LOG = LoggerFactory.getLogger(ReferensResource.class);

    @Autowired
    private ReferensRepository referensRepository;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertReferens(Referens referens) {
        Referens savedReferens = referensRepository.save(referens);
        LOG.info("Created Referens with id {} using testability API", savedReferens.getId());
        return Response.ok().build();
    }

    @GET
    @Path("/referensCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getEventCountForCertificateIds(List<String> certificateIds) {
        final var referensList = (List<Referens>) referensRepository.findAll();
        return referensList.stream().filter(referens -> certificateIds.contains(referens.getIntygsId())).count();
    }

    @DELETE
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletReferenserByCertificateIds(List<String> certificateIds) {
        final var referensList = (List<Referens>) referensRepository.findAll();
        final var referensForDeletion = referensList.stream()
            .filter(referens -> certificateIds.contains(referens.getIntygsId()))
            .collect(Collectors.toList());
        referensRepository.deleteAll(referensForDeletion);
        LOG.info("Deleted {} referenser based on certificateIds using testability API", referensForDeletion.size());

        return Response.ok().build();
    }

}
