/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.list.config.ListDraftsConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.ListPreviousCertificatesConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.ListSignedCertificatesConfigFacadeServiceImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/list/config")
public class ListConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(ListConfigController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final ListDraftsConfigFacadeServiceImpl draftListConfigFacadeService;
    private final ListSignedCertificatesConfigFacadeServiceImpl listSignedCertificatesConfigFacadeService;
    private final ListPreviousCertificatesConfigFacadeServiceImpl listPreviousCertificatesConfigFacadeService;

    @Autowired
    public ListConfigController(ListDraftsConfigFacadeServiceImpl draftListConfigFacadeService,
                                ListSignedCertificatesConfigFacadeServiceImpl listSignedCertificatesConfigFacadeService,
                                ListPreviousCertificatesConfigFacadeServiceImpl listPreviousCertificatesConfigFacadeService) {
        this.draftListConfigFacadeService = draftListConfigFacadeService;
        this.listSignedCertificatesConfigFacadeService = listSignedCertificatesConfigFacadeService;
        this.listPreviousCertificatesConfigFacadeService = listPreviousCertificatesConfigFacadeService;
    }

    @Path("/draft")
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getListOfDraftsConfig() {
        LOG.debug("Getting config for list of drafts");
        final var config = draftListConfigFacadeService.get();
        return Response.ok(config).build();
    }

    @Path("/certificate")
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getListOfSignedCertificatesConfig() {
        LOG.debug("Getting config for list of signed certificates");
        final var config = listSignedCertificatesConfigFacadeService.get();
        return Response.ok(config).build();
    }

    @Path("/previous")
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getListOfPreviousCertificatesConfig() {
        LOG.debug("Getting config for list of previous certificates");
        final var config = listPreviousCertificatesConfigFacadeService.get();
        return Response.ok(config).build();
    }
}
