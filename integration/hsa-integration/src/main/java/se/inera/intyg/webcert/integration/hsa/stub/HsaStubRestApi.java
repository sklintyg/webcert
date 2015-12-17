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

package se.inera.intyg.webcert.integration.hsa.stub;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.integration.hsa.model.Vardgivare;

/**
 * @author johannesc
 */
public class HsaStubRestApi {

    @Autowired
    private HsaServiceStub hsaServiceStub;

    @POST
    @Path("/vardgivare")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUnit(List<Vardgivare> vardgivare) {
        hsaServiceStub.getVardgivare().addAll(vardgivare);
        return Response.ok().build();
    }

    @POST
    @Path("/medarbetaruppdrag")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUnit(Medarbetaruppdrag medarbetaruppdrag) {
        hsaServiceStub.getMedarbetaruppdrag().add(medarbetaruppdrag);
        return Response.ok().build();
    }

    @DELETE
    @Path("/medarbetaruppdrag/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMedarbetaruppdrag(@PathParam("id") String id) {
        hsaServiceStub.deleteMedarbetareuppdrag(id);
        return Response.ok().build();
    }

    @DELETE
    @Path("/vardgivare/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteVardgivare(@PathParam("id") String id) {
        hsaServiceStub.deleteVardgivare(id);
        return Response.ok().build();
    }

    @GET
    @Path("/vardgivare")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Vardgivare> getVardgivare() {
        return hsaServiceStub.getVardgivare();
    }

    @GET
    @Path("/medarbetaruppdrag")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Medarbetaruppdrag> getMedarbetaruppdrag() {
        return hsaServiceStub.getMedarbetaruppdrag();
    }
}
