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
package se.inera.intyg.webcert.web.web.controller.facade;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.list.config.ListDraftsConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.ListPreviousCertificatesConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.ListQuestionsConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.config.ListSignedCertificatesConfigFacadeServiceImpl;
import se.inera.intyg.webcert.web.web.controller.facade.dto.UpdateListConfigRequestDTO;

@Path("/list/config")
public class ListConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(ListConfigController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final ListDraftsConfigFacadeServiceImpl draftListConfigFacadeService;
    private final ListSignedCertificatesConfigFacadeServiceImpl listSignedCertificatesConfigFacadeService;
    private final ListPreviousCertificatesConfigFacadeServiceImpl listPreviousCertificatesConfigFacadeService;
    private final ListQuestionsConfigFacadeServiceImpl listQuestionsConfigFacadeService;

    @Autowired
    public ListConfigController(ListDraftsConfigFacadeServiceImpl draftListConfigFacadeService,
        ListSignedCertificatesConfigFacadeServiceImpl listSignedCertificatesConfigFacadeService,
        ListPreviousCertificatesConfigFacadeServiceImpl listPreviousCertificatesConfigFacadeService,
        ListQuestionsConfigFacadeServiceImpl listQuestionsConfigFacadeService) {
        this.draftListConfigFacadeService = draftListConfigFacadeService;
        this.listSignedCertificatesConfigFacadeService = listSignedCertificatesConfigFacadeService;
        this.listPreviousCertificatesConfigFacadeService = listPreviousCertificatesConfigFacadeService;
        this.listQuestionsConfigFacadeService = listQuestionsConfigFacadeService;
    }

    @Path("/draft")
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "list-config-get-list-or-drafts-config", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getListOfDraftsConfig() {
        LOG.debug("Getting config for list of drafts");
        final var config = draftListConfigFacadeService.get();
        return Response.ok(config).build();
    }

    @Path("/certificate")
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "list-config-get-list-of-signed-certificates-config", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getListOfSignedCertificatesConfig() {
        LOG.debug("Getting config for list of signed certificates");
        final var config = listSignedCertificatesConfigFacadeService.get();
        return Response.ok(config).build();
    }

    @Path("/previous")
    @GET
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "list-config-get-list-of-previous-certificates-config", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getListOfPreviousCertificatesConfig() {
        LOG.debug("Getting config for list of previous certificates");
        final var config = listPreviousCertificatesConfigFacadeService.get();
        return Response.ok(config).build();
    }

    @Path("/question")
    @POST
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "list-config-get-list-of-questions", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getListOfQuestions(String unitId) {
        LOG.debug("Getting config for list of unhandled questions");
        final var config = listQuestionsConfigFacadeService.get(unitId);
        return Response.ok(config).build();
    }

    @Path("/question/update")
    @POST
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "list-config-update-list-of-questions-config", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response updateListOfQuestionsConfig(UpdateListConfigRequestDTO request) {
        LOG.debug("Getting updated config for list of unhandled questions");
        final var updatedConfig = listQuestionsConfigFacadeService.update(request.getConfig(), request.getUnitId());
        return Response.ok(updatedConfig).build();
    }
}
